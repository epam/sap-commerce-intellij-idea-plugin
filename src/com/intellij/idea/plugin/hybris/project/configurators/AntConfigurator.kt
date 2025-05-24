/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.ConfigModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.PlatformModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YCustomRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformExtModuleDescriptor
import com.intellij.lang.ant.config.AntBuildFileBase
import com.intellij.lang.ant.config.AntConfigurationBase
import com.intellij.lang.ant.config.AntNoFileException
import com.intellij.lang.ant.config.execution.AntRunConfiguration
import com.intellij.lang.ant.config.execution.AntRunConfigurationType
import com.intellij.lang.ant.config.impl.*
import com.intellij.lang.ant.config.impl.AntBuildFileImpl.*
import com.intellij.lang.ant.config.impl.configuration.EditPropertyContainer
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.asSafely
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern

class AntConfigurator {

    private val desirablePlatformTargets = listOf(
        "clean",
        "build",
        "all",
        "addonclean",
        "alltests",
        "allwebtests",
        "apidoc",
        "bugprooftests",
        "classpathgen",
        "cleanMavenDependencies",
        "cleanear",
        "clearAdministrationLock",
        "clearOrphanedTypes",
        "codequality",
        "commonwebclean",
        "copyFromTemplate",
        "createConfig",
        "createPlatformImageStructure",
        "createtypesystem",
        "customize",
        "demotests",
        "deploy",
        "deployDist",
        "deployDistWithSources",
        "dist",
        "distWithSources",
        "droptypesystem",
        "ear",
        "executeScript",
        "executesql",
        "extensionsxml",
        "extgen",
        "generateLicenseOverview",
        "gradle",
        "importImpex",
        "initialize",
        "initializetenantdb",
        "integrationtests",
        "localizationtest",
        "localproperties",
        "manualtests",
        "metadata",
        "modulegen",
        "performancetests",
        "production",
        "runcronjob",
        "sanitycheck",
        "sassclean",
        "sasscompile",
        "server",
        "sonarcheck",
        "sourcezip",
        "startAdminServer",
        "startHybrisServer",
        "syncaddons",
        "testMavenDependencies",
        "typecodetest",
        "unittests",
        "updateMavenDependencies",
        "updateSpringXsd",
        "updatesystem",
        "webservice_nature",
        "yunitinit",
        "yunitupdate"
    )

    private val desirableCustomTargets = listOf(
        "clean",
        "build",
        "deploy",
        "all",
        "gensource",
        "dist"
    )
    private val metaTargets = listOf(
        listOf("clean", "all"),
        listOf("clean", "customize", "all", "initialize"),
        listOf("clean", "customize", "all", "production")
    )

    fun configureAfterImport(
        project: Project,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        allModules: List<ModuleDescriptor>
    ): List<() -> Unit> {
        val platformDescriptor = hybrisProjectDescriptor.platformHybrisModuleDescriptor
        val extHybrisModuleDescriptors = mutableListOf<ModuleDescriptor>()
        val customHybrisModuleDescriptors = mutableListOf<ModuleDescriptor>()

        for (descriptor in allModules) {
            when (descriptor) {
                is YPlatformExtModuleDescriptor -> extHybrisModuleDescriptors.add(descriptor)
                is YCustomRegularModuleDescriptor -> customHybrisModuleDescriptors.add(descriptor)
            }
        }

        val antInstallation = createAntInstallation(platformDescriptor)
            ?: return emptyList()

        val classPaths = createAntClassPath(platformDescriptor, extHybrisModuleDescriptors)
        val antConfiguration = AntConfigurationBase.getInstance(project).apply {
            isFilterTargets = true
        }

        return listOf {
            val antBuildFiles = mutableListOf<Pair<AntBuildFileBase, List<String>>>()

            findBuildFile(antConfiguration, platformDescriptor)
                ?.apply {
                    metaTargets
                        .map { ExecuteCompositeTargetEvent(it) }
                        .filter { antConfiguration.getTargetForEvent(it) == null }
                        .forEach { antConfiguration.setTargetForEvent(this, it.metaTargetName, it) }
                }
                ?.let { it to desirablePlatformTargets }
                ?.let { antBuildFiles.add(it) }

            if (hybrisProjectDescriptor.isImportCustomAntBuildFiles) {
                customHybrisModuleDescriptors
                    .mapNotNull { findBuildFile(antConfiguration, it) }
                    .map { it to desirableCustomTargets }
                    .forEach { antBuildFiles.add(it) }
            }

            antBuildFiles.forEach { (antBuildFile, desirabTargets) ->
                registerAntInstallation(hybrisProjectDescriptor, antInstallation, classPaths, antBuildFile)

                ReadAction
                    .nonBlocking<EditPropertyContainer> {
                        val allOptions = antBuildFile.allOptions

                        EditPropertyContainer(allOptions).apply {
                            TARGET_FILTERS[this] = getFilteredTargets(antConfiguration, antBuildFile, desirabTargets)
                        }
                    }
                    .finishOnUiThread(ModalityState.defaultModalityState()) {
                        it.apply()
                    }
                    .submit(AppExecutorUtil.getAppExecutorService())
            }

            saveAntInstallation(antInstallation)
            removeMake(project)
        }
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

    /*
    Slow Operation - must be invoked from the BGT
     */
    private fun getFilteredTargets(
        antConfiguration: AntConfigurationBase,
        antBuildFile: AntBuildFileBase,
        desirableTargets: List<String>
    ) = antConfiguration.getModel(antBuildFile).targets
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
                } catch (e: IOException) {
                    LOG.error("Cannot read ", HybrisConstants.IMPORT_OVERRIDE_FILENAME)
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

    private fun findBuildFile(antConfiguration: AntConfigurationBase, moduleDescriptor: ModuleDescriptor): AntBuildFileBase? {
        val dir = moduleDescriptor.moduleRootDirectory
        val buildFile = File(dir, HybrisConstants.ANT_BUILD_XML)
            .takeIf { it.exists() }
            ?.let { VfsUtil.findFileByIoFile(it, true) }
            ?: return null

        try {
            return antConfiguration.addBuildFile(buildFile)
                ?.asSafely<AntBuildFileBase>()
        } catch (ignored: AntNoFileException) {
        }

        return null
    }

    private fun createAntInstallation(platformDescriptor: PlatformModuleDescriptor): AntInstallation? {
        try {
            val directory = Paths.get(platformDescriptor.moduleRootDirectory.absolutePath)
            val antFolderUrl = Files
                .find(
                    directory,
                    1,
                    { path: Path, _ -> Files.isDirectory(path) && PATTERN_APACHE_ANT.matcher(path.toFile().name).matches() })
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

    fun clearAntSettings(project: Project) {
        val antConfiguration = AntConfigurationBase.getInstance(project) ?: return

        for (antBuildFile in antConfiguration.buildFiles) {
            antConfiguration.removeBuildFile(antBuildFile)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(AntConfigurator::class.java)
        private val PATTERN_APACHE_ANT: Pattern = Pattern.compile("apache-ant.*")
    }
}
