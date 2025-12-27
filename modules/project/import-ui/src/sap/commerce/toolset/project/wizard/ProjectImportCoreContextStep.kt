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

package sap.commerce.toolset.project.wizard

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.scale.JBUIScale
import kotlinx.collections.immutable.toImmutableSet
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.HybrisProjectImportBuilder
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectImportCoreContext
import sap.commerce.toolset.project.context.ProjectImportSettings
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.tasks.SearchHybrisDistributionDirectoryTaskModal
import sap.commerce.toolset.project.tasks.SearchModulesRootsTaskModal
import sap.commerce.toolset.project.ui.ui
import sap.commerce.toolset.project.utils.FileUtils
import sap.commerce.toolset.settings.ApplicationSettings
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import java.util.*
import javax.swing.ScrollPaneConstants
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class ProjectImportCoreContextStep(context: WizardContext) : ProjectImportWizardStep(context), RefreshSupport {

    private val importCoreContext by lazy {
        ProjectImportCoreContext(
            importSettings = ProjectImportSettings.of(ApplicationSettings.getInstance()).mutable()
        )
    }
    private val _ui by lazy { ui(importCoreContext) }

    override fun getComponent() = JBScrollPane(_ui).apply {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        preferredSize = Dimension(preferredSize.width, JBUIScale.scale(600))

        CCv2ProjectSettings.getInstance().loadDefaultCCv2Token { ccv2Token ->
            if (ccv2Token != null) importCoreContext.ccv2Token.set(ccv2Token)
        }
    }

    override fun updateDataModel() {
        _ui.apply()

        val importSettings = importCoreContext.importSettings.immutable()
        val importContext = importBuilder().initContext(importSettings)

        wizardContext.projectName = importCoreContext.projectName.get()

        with(importContext) {
            this.platformVersion = importCoreContext.platformVersion.get()
            this.platformDirectory = FileUtils.toFile(importCoreContext.platformDirectory.get())
            this.javadocUrl = importCoreContext.javadocUrl.get()
            this.excludedFromScanning = importCoreContext.excludedFromScanningDirectories.get().toImmutableSet()

            this.externalExtensionsDirectory = importCoreContext.customDirectory.takeIf { importCoreContext.customDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.externalConfigDirectory = importCoreContext.configDirectory.takeIf { importCoreContext.configDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.externalDbDriversDirectory = importCoreContext.dbDriverDirectory.takeIf { importCoreContext.dbDriverDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.projectIconFile = importCoreContext.projectIconFile.takeIf { importCoreContext.projectIcon.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.modulesFilesDirectory = importCoreContext.moduleFilesStorageDirectory.takeIf { importCoreContext.moduleFilesStorage.get() }
                ?.let { FileUtils.toFile(it.get()) }

            this.sourceCodeFile = importCoreContext.sourceCodeDirectory.takeIf { importCoreContext.sourceCodeDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
                ?.let {
                    if (it.isDirectory && it.absolutePath == platformDirectory?.absolutePath) null
                    else it
                }

            this.ccv2Token = importCoreContext.ccv2Token.get()

            thisLogger().info("importing a project with the following settings: $this")
        }

        searchModuleRoots(importContext)
    }

    override fun updateStep() {
        if (importCoreContext.moduleFilesStorageDirectory.get().isBlank()) {
            importCoreContext.moduleFilesStorageDirectory.set(
                Path(builder.fileToImport)
                    .resolve(ProjectConstants.Directory.PATH_IDEA_MODULES)
                    .absolutePathString()
            )
        }
        if (importCoreContext.projectName.get().isBlank()) {
            importCoreContext.projectName.set(wizardContext.projectName)
        }

        if (importCoreContext.platformDirectory.get().isBlank()) {
            val rootProjectDirectory = File(builder.fileToImport)
            val task = SearchHybrisDistributionDirectoryTaskModal(rootProjectDirectory) {
                importCoreContext.platformDirectory.set(it)
            }
            ProgressManager.getInstance().run(task)
        }

        val platformPath = importCoreContext.platformDirectory.get()
            .takeIf { it.isNotBlank() }
            ?.let { Path(it) }
            ?.takeIf { it.exists() }
            ?: return

        val hybrisApiVersion = getPlatformVersion(platformPath, true)
            ?.also { importCoreContext.platformVersion.set(it) }
        val hybrisVersion = getPlatformVersion(platformPath, false)
            ?.also { importCoreContext.platformVersion.set(it) }

        if (importCoreContext.customDirectory.get().isBlank()) {
            importCoreContext.customDirectory.set(
                platformPath
                    .resolve(ProjectConstants.Directory.PATH_BIN_CUSTOM)
                    .absolutePathString()
            )
        }

        if (importCoreContext.configDirectory.get().isBlank()) {
            importCoreContext.configDirectory.set(
                platformPath
                    .resolve(EiConstants.Extension.CONFIG)
                    .absolutePathString()
            )
        }

        if (importCoreContext.dbDriverDirectory.get().isBlank()) {
            val dbDriversDirAbsolutePath = platformPath
                .resolve(ProjectConstants.Directory.PATH_BIN_PLATFORM)
                .resolve(ProjectConstants.Directory.PATH_LIB_DB_DRIVER)

            importCoreContext.dbDriverDirectory.set(dbDriversDirAbsolutePath.absolutePathString())
        }

        if (importCoreContext.javadocUrl.get().isBlank()) {
            getPlatformJavadocUrl(hybrisApiVersion)
                .takeIf { it.isNotBlank() }
                ?.let { importCoreContext.javadocUrl.set(it) }
        }

        if (importCoreContext.sourceCodeDirectory.get().isBlank()) {
            val sourceCodeFile = findSourceZip(platformPath, hybrisVersion)
                ?.absolutePath
                ?: platformPath.absolutePathString()

            importCoreContext.sourceCodeDirectory.set(sourceCodeFile)
        }
    }

    override fun refresh(refreshContext: ProjectRefreshContext) {
        val importSettings = refreshContext.importSettings
        val projectSettings = refreshContext.project.ySettings
        val importContext = importBuilder().initContext(importSettings)

        with(importContext) {
            val platformPath = refreshContext.projectPath.resolve("hybris")

            this.platformVersion = getPlatformVersion(platformPath, false)
                ?: projectSettings.hybrisVersion
            this.javadocUrl = getPlatformVersion(platformPath, true)
                ?.let { getPlatformJavadocUrl(it) }
                ?.takeIf { it.isNotBlank() }
                ?: projectSettings.javadocUrl
            this.sourceCodeFile = FileUtils.toFile(projectSettings.sourceCodeFile, true)
            this.externalExtensionsDirectory = FileUtils.toFile(projectSettings.externalExtensionsDirectory, true)
            this.externalConfigDirectory = FileUtils.toFile(projectSettings.externalConfigDirectory, true)
            this.externalDbDriversDirectory = FileUtils.toFile(projectSettings.externalDbDriversDirectory, true)

            this.modulesFilesDirectory = projectSettings.ideModulesFilesDirectory
                ?.let { File(it) }
                ?: File(
                    builder.fileToImport,
                    HybrisConstants.DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES
                )

            this.ccv2Token = CCv2ProjectSettings.getInstance().getCCv2Token()

            val hybrisDirectory = projectSettings.hybrisDirectory
            if (hybrisDirectory != null) {
                this.platformDirectory = FileUtils.toFile(
                    builder.fileToImport,
                    projectSettings.hybrisDirectory
                )
            }

            this.excludedFromScanning = projectSettings.excludedFromScanning

            // TODO: order of scans may be incorrect
            searchModuleRoots(importContext)

            if (hybrisDirectory == null) {
                val rootProjectDirectory = FileUtils.toFile(builder.fileToImport)!!
                // refreshing a project which was never imported by this plugin
                val task = SearchHybrisDistributionDirectoryTaskModal(rootProjectDirectory) {
                    this.platformDirectory = FileUtils.toFile(it)
                }

                ProgressManager.getInstance().run(task)
            }

            thisLogger().info("Refreshing a project with the following settings: $this")
        }
    }

    override fun validate(): Boolean {
        if (!super.validate()) return false

        importCoreContext.projectIconFile.takeIf { importCoreContext.projectIcon.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isFile ?: false }
            ?.let { throw ConfigurationException("Custom project icon should point to an existing SVG file.") }

        importCoreContext.customDirectory.takeIf { importCoreContext.customDirectoryOverride.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isDirectory ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.custom.extensions.directory.does.not.exist")) }

        importCoreContext.configDirectory.takeIf { importCoreContext.configDirectoryOverride.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isDirectory ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.config.directory.does.not.exist")) }

        importCoreContext.dbDriverDirectory.takeIf { importCoreContext.dbDriverDirectoryOverride.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isDirectory ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.dbdriver.directory.does.not.exist")) }

        val platformDirectory = importCoreContext.platformDirectory.get()
        if (platformDirectory.isBlank()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.empty"))
        if (!Path(platformDirectory).isDirectory()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.does.not.exist"))

        return true
    }

    private fun findSourceZip(sourceCodePath: Path, hybrisApiVersion: String?): File? {
        hybrisApiVersion ?: return null
        if (hybrisApiVersion.isBlank()) return null

        if (!sourceCodePath.isDirectory()) return null

        val sourceZipList = sourceCodePath.toFile().listFiles()
            ?.filter { it.name.endsWith(".zip") }
            ?.filter { it.name.contains(hybrisApiVersion) }
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return if (sourceZipList.size > 1) sourceZipList.maxByOrNull { getPlatformPatchVersion(it, hybrisApiVersion) }
        else sourceZipList[0]
    }

    private fun getPlatformVersion(hybrisRootDir: Path, apiOnly: Boolean): String? {
        val buildInfoFile = hybrisRootDir.resolve(ProjectConstants.Directory.PATH_BIN_PLATFORM_BUILD_NUMBER)
            .toFile()
        val buildProperties = Properties()

        try {
            FileInputStream(buildInfoFile).use { fis ->
                buildProperties.load(fis)
            }
        } catch (_: IOException) {
            return null
        }

        if (!apiOnly) {
            val version = buildProperties.getProperty(HybrisConstants.HYBRIS_VERSION_KEY)
            if (version != null) return version
        }

        return buildProperties.getProperty(HybrisConstants.HYBRIS_API_VERSION_KEY)
    }

    private fun getPlatformPatchVersion(sourceZip: File, hybrisApiVersion: String): Int {
        val name = sourceZip.name
        val index = name.indexOf(hybrisApiVersion)
        val firstDigit = name[index + hybrisApiVersion.length + 1]
        val secondDigit = name[index + hybrisApiVersion.length + 2]

        return if (Character.isDigit(secondDigit)) Character.getNumericValue(firstDigit) * 10 + Character.getNumericValue(secondDigit)
        else Character.getNumericValue(firstDigit)
    }

    private fun getPlatformJavadocUrl(hybrisApiVersion: String?) = if (hybrisApiVersion?.isNotEmpty() == true) String.format(HybrisConstants.URL_HELP_JAVADOC, hybrisApiVersion)
    else HybrisConstants.URL_HELP_JAVADOC_FALLBACK

    private fun searchModuleRoots(importContext: ProjectImportContext.Mutable) {
        thisLogger().info("Setting RootProjectDirectory to ${importContext.rootDirectory}")
        val task = SearchModulesRootsTaskModal(importContext)

        ProgressManager.getInstance().run(task)
    }

    private fun importBuilder() = builder as HybrisProjectImportBuilder

}