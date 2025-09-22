/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package sap.commerce.toolset.ant.configurator

import com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.lang.ant.config.AntBuildFileBase
import com.intellij.lang.ant.config.AntConfigurationBase
import com.intellij.lang.ant.config.AntNoFileException
import com.intellij.lang.ant.config.execution.AntRunConfiguration
import com.intellij.lang.ant.config.execution.AntRunConfigurationType
import com.intellij.lang.ant.config.impl.*
import com.intellij.lang.ant.config.impl.AntBuildFileImpl.*
import com.intellij.lang.ant.config.impl.configuration.EditPropertyContainer
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ant.AntConstants
import sap.commerce.toolset.project.configurator.ProjectPostImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectRefreshConfigurator
import sap.commerce.toolset.project.descriptor.ConfigModuleDescriptor
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCustomRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YPlatformExtModuleDescriptor
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class AntConfigurator : ProjectPostImportConfigurator, ProjectRefreshConfigurator {

    override val name: String
        get() = "Ant"

    override fun beforeRefresh(project: Project) {
        val antConfiguration = AntConfigurationBase.getInstance(project) ?: return

        for (antBuildFile in antConfiguration.buildFiles) {
            antConfiguration.removeBuildFile(antBuildFile)
        }
    }

    override suspend fun postImport(hybrisProjectDescriptor: HybrisProjectDescriptor) {
        val project = hybrisProjectDescriptor.project ?: return
        val platformDescriptor = hybrisProjectDescriptor.platformHybrisModuleDescriptor
        val extHybrisModuleDescriptors = mutableListOf<ModuleDescriptor>()
        val customHybrisModuleDescriptors = mutableListOf<ModuleDescriptor>()

        for (descriptor in hybrisProjectDescriptor.chosenModuleDescriptors) {
            when (descriptor) {
                is YPlatformExtModuleDescriptor -> extHybrisModuleDescriptors.add(descriptor)
                is YCustomRegularModuleDescriptor -> customHybrisModuleDescriptors.add(descriptor)
            }
        }

        val antInstallation = createAntInstallation(platformDescriptor)
            ?: return

        val classPaths = createAntClassPath(platformDescriptor, extHybrisModuleDescriptors)
        val antConfiguration = AntConfigurationBase.getInstance(project).apply {
            isFilterTargets = true
        }

        val platformAntBuildVirtualFile = getBuildVirtualFile(platformDescriptor)
        val customAntBuildVirtualFiles = if (hybrisProjectDescriptor.isImportCustomAntBuildFiles) customHybrisModuleDescriptors
            .mapNotNull { getBuildVirtualFile(it) }
        else emptyList()

        val antBuildFiles = mutableListOf<Pair<AntBuildFileBase, List<String>>>()

        platformAntBuildVirtualFile
            ?.let { findBuildFile(antConfiguration, it) }
            ?.apply {
                AntConstants.META_TARGETS
                    .map { ExecuteCompositeTargetEvent(it) }
                    .filter { readAction { antConfiguration.getTargetForEvent(it) } == null }
                    .forEach { antConfiguration.setTargetForEvent(this, it.metaTargetName, it) }
            }
            ?.let { antBuildFiles.add(it to AntConstants.DESIRABLE_PLATFORM_TARGETS) }

        customAntBuildVirtualFiles
            .mapNotNull { findBuildFile(antConfiguration, it) }
            .forEach { antBuildFiles.add(it to AntConstants.DESIRABLE_CUSTOM_TARGETS) }

        val editPropertyContainers = antBuildFiles
            .map { (antBuildFile, desirableTargets) ->
                registerAntInstallation(hybrisProjectDescriptor, antInstallation, classPaths, antBuildFile)
                val allOptions = antBuildFile.allOptions

                EditPropertyContainer(allOptions).apply {
                    TARGET_FILTERS[this] = getFilteredTargets(antConfiguration, antBuildFile, desirableTargets)
                }
            }

        edtWriteAction {
            editPropertyContainers.forEach { it.apply() }

            saveAntInstallation(antInstallation)
            removeMake(project)
        }
    }

    private suspend fun getBuildVirtualFile(descriptor: ModuleDescriptor) = readAction {
        File(descriptor.moduleRootDirectory, HybrisConstants.ANT_BUILD_XML)
            .takeIf { it.exists() }
            ?.let { VfsUtil.findFileByIoFile(it, true) }
    }

    private fun registerAntInstallation(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        antInstallation: AntInstallation,
        classPaths: List<AntClasspathEntry>,
        antBuildFile: AntBuildFileBase
    ) {
        val platformDir = hybrisProjectDescriptor.platformHybrisModuleDescriptor.moduleRootDirectory
        val externalConfigDirectory = hybrisProjectDescriptor.externalConfigDirectory
        val configDescriptor = hybrisProjectDescriptor.configHybrisModuleDescriptor
        val allOptions = antBuildFile.allOptions

        with(EditPropertyContainer(allOptions)) {
            ADDITIONAL_CLASSPATH[this] = classPaths
            TREE_VIEW[this] = true
            TREE_VIEW_ANSI_COLOR[this] = true
            TREE_VIEW_COLLAPSE_TARGETS[this] = false
            ANT_INSTALLATION[this] = antInstallation
            ANT_REFERENCE[this] = antInstallation.reference
            RUN_WITH_ANT[this] = antInstallation
            MAX_HEAP_SIZE[this] = HybrisConstants.ANT_HEAP_SIZE_MB
            MAX_STACK_SIZE[this] = HybrisConstants.ANT_STACK_SIZE_MB
            RUN_IN_BACKGROUND[this] = false
            VERBOSE[this] = false

            val properties = ANT_PROPERTIES
            properties.getModifiableList(this).clear()

            externalConfigDirectory
                ?.absolutePath
                ?.let { HybrisConstants.ANT_HYBRIS_CONFIG_DIR + it }
                ?.let { ANT_COMMAND_LINE_PARAMETERS[this] = it }

            val platformHomeProperty = BuildFileProperty().apply {
                this.propertyName = HybrisConstants.ANT_PLATFORM_HOME
                this.propertyValue = platformDir.absolutePath
            }

            val antHomeProperty = BuildFileProperty().apply {
                this.propertyName = HybrisConstants.ANT_HOME
                this.propertyValue = antInstallation.homeDir
            }

            val antOptsProperty = BuildFileProperty().apply {
                this.propertyName = HybrisConstants.ANT_OPTS
                this.propertyValue = getAntOpts(configDescriptor)
            }

            val buildFileProperties = mutableListOf(
                platformHomeProperty,
                antHomeProperty,
                antOptsProperty
            )

            ANT_PROPERTIES[this] = buildFileProperties

            apply()
        }
    }

    private suspend fun getFilteredTargets(
        antConfiguration: AntConfigurationBase,
        antBuildFile: AntBuildFileBase,
        desirableTargets: List<String>
    ) = readAction { antConfiguration.getModel(antBuildFile).targets }
        .map { TargetFilter.fromTarget(it) }
        .onEach { it.isVisible = desirableTargets.contains(it.targetName) }

    private fun getAntOpts(configDescriptor: ConfigModuleDescriptor?): String {
        if (configDescriptor != null) {
            val propertiesFile = File(configDescriptor.moduleRootDirectory, HybrisConstants.IMPORT_OVERRIDE_FILENAME)
            if (propertiesFile.exists()) {
                val properties = Properties()
                try {
                    FileInputStream(propertiesFile).use { fis ->
                        properties.load(fis)
                        val antOptsText = properties.getProperty(HybrisConstants.ANT_OPTS)
                        if (antOptsText != null && antOptsText.trim { it <= ' ' }.isNotEmpty()) {
                            return antOptsText.trim { it <= ' ' }
                        }
                    }
                } catch (_: IOException) {
                    thisLogger().error("Cannot read ", HybrisConstants.IMPORT_OVERRIDE_FILENAME)
                }
            }
        }
        return HybrisConstants.ANT_XMX + HybrisConstants.ANT_HEAP_SIZE_MB + "m " + HybrisConstants.ANT_ENCODING
    }

    private fun createAntClassPath(platformDescriptor: PlatformModuleDescriptor, extHybrisModuleDescriptors: List<ModuleDescriptor>): List<AntClasspathEntry> {
        val directory = platformDescriptor.moduleRootDirectory
        val classPaths = ArrayList<AntClasspathEntry>()
        val libDir = File(directory, HybrisConstants.ANT_LIB_DIR)
        val platformLibDir = File(directory, HybrisConstants.LIB_DIRECTORY)
        val entries = extHybrisModuleDescriptors
            .map { it.moduleRootDirectory }
            .map { File(it, HybrisConstants.LIB_DIRECTORY) }
            .map { AllJarsUnderDirEntry(it) }

        classPaths.add(AllJarsUnderDirEntry(platformLibDir))
        classPaths.add(AllJarsUnderDirEntry(libDir))
        classPaths.addAll(entries)

        return classPaths
    }

    private fun findBuildFile(antConfiguration: AntConfigurationBase, buildFile: VirtualFile) = try {
        antConfiguration.buildFiles.find { it.virtualFile == buildFile }
            ?: antConfiguration.addBuildFile(buildFile)
    } catch (e: AntNoFileException) {
        thisLogger().warn(e)
        null
    }
        ?.asSafely<AntBuildFileBase>()

    private fun createAntInstallation(platformDescriptor: PlatformModuleDescriptor): AntInstallation? {
        try {
            val directory = Paths.get(platformDescriptor.moduleRootDirectory.absolutePath)
            val antFolderUrl = Files
                .find(
                    directory,
                    1,
                    { path: Path, _ -> Files.isDirectory(path) && AntConstants.PATTERN_APACHE_ANT.matcher(path.toFile().name).matches() })
                .map { it.toFile() }
                .map { it.absolutePath }
                .findFirst()
                .orElse(null)
                ?: return null

            return AntInstallation.fromHome(antFolderUrl)
        } catch (_: IOException) {
        } catch (_: AntInstallation.ConfigurationException) {
        }

        return null
    }

    private fun saveAntInstallation(antInstallation: AntInstallation) = GlobalAntConfiguration.getInstance()
        ?.let { globalAntConfiguration ->
            with(globalAntConfiguration) {
                globalAntConfiguration.configuredAnts[antInstallation.reference]
                    ?.let { this.removeConfiguration(it) }
                this.addConfiguration(antInstallation)
            }
        }

    private fun removeMake(project: Project) {
        val runManager = RunManagerImpl.getInstanceImpl(project)

        val antRunConfigurationType = findConfigurationType(AntRunConfigurationType::class.java)
        val configurationFactory = antRunConfigurationType.configurationFactories[0]
        val template = runManager.getConfigurationTemplate(configurationFactory)
        val runConfiguration = template.configuration as AntRunConfiguration

        runManager.setBeforeRunTasks(runConfiguration, emptyList())
    }

}
