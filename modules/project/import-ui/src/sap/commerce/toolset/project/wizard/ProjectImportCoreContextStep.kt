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
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.scale.JBUIScale
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.HybrisProjectImportBuilder
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectImportCoreContext
import sap.commerce.toolset.project.context.ProjectImportSettings
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.tasks.LookupModuleDescriptorsTask
import sap.commerce.toolset.project.tasks.LookupPlatformDirectoryTask
import sap.commerce.toolset.project.ui.uiCoreStep
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import java.util.*
import javax.swing.ScrollPaneConstants
import kotlin.io.path.*

class ProjectImportCoreContextStep(context: WizardContext) : ProjectImportWizardStep(context), RefreshSupport {

    private val disposable = Disposer.newDisposable()
    private val importCoreContext by lazy {
        ProjectImportCoreContext(
            importSettings = ProjectImportSettings.of(ApplicationSettings.getInstance()).mutable()
        )
    }
    private val _ui by lazy {
        uiCoreStep(importCoreContext)
            .also { it.registerValidators(disposable) }
    }

    override fun getComponent() = JBScrollPane(_ui).apply {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        preferredSize = Dimension(preferredSize.width, JBUIScale.scale(600))

        CCv2ProjectSettings.getInstance().loadDefaultCCv2Token { ccv2Token ->
            if (ccv2Token != null) importCoreContext.ccv2Token.set(ccv2Token)
        }
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
        disposable.dispose()
    }

    override fun updateDataModel() {
        _ui.apply()

        val importSettings = importCoreContext.importSettings.immutable()
        val importContext = importBuilder().initContext(importSettings)

        wizardContext.projectName = importCoreContext.projectName.get()

        with(importContext) {
            this.platformVersion = importCoreContext.platformVersion.get()
            this.platformDirectory = importCoreContext.platformDirectory.get().toNioPathOrNull()
            this.javadocUrl = importCoreContext.javadocUrl.get()
            this.excludedFromScanning = importCoreContext.excludedFromScanningDirectories.get().toImmutableSet()

            this.externalExtensionsDirectory = importCoreContext.customDirectory.takeIf { importCoreContext.customDirectoryOverride.get() }
                ?.get()?.toNioPathOrNull()
            this.externalConfigDirectory = importCoreContext.configDirectory.takeIf { importCoreContext.configDirectoryOverride.get() }
                ?.get()?.toNioPathOrNull()
            this.externalDbDriversDirectory = importCoreContext.dbDriverDirectory.takeIf { importCoreContext.dbDriverDirectoryOverride.get() }
                ?.get()?.toNioPathOrNull()
            this.projectIconFile = importCoreContext.projectIconFile.takeIf { importCoreContext.projectIcon.get() }
                ?.get()?.toNioPathOrNull()
            this.modulesFilesDirectory = importCoreContext.moduleFilesStorageDirectory.takeIf { importCoreContext.moduleFilesStorage.get() }
                ?.get()?.toNioPathOrNull()

            this.sourceCodeFile = importCoreContext.sourceCodeDirectory.takeIf { importCoreContext.sourceCodeDirectoryOverride.get() }
                ?.get()
                ?.toNioPathOrNull()
                ?.let {
                    // TODO: detect exact suitable file as an observable property on directory selection
                    // also respect on project refresh
                    if (it.directoryExists && it.pathString == platformDirectory?.pathString) null
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
                    .resolve(ProjectConstants.Paths.IDEA_MODULES)
                    .absolutePathString()
            )
        }
        if (importCoreContext.projectName.get().isBlank()) {
            importCoreContext.projectName.set(wizardContext.projectName)
        }

        if (importCoreContext.platformDirectory.get().isBlank()) {
            findPlatformDirectory()
                ?.let { importCoreContext.platformDirectory.set(it.normalize().pathString) }
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
                    .resolve(ProjectConstants.Paths.BIN_CUSTOM)
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
                .resolve(ProjectConstants.Paths.BIN_PLATFORM)
                .resolve(ProjectConstants.Paths.LIB_DB_DRIVER)

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
            this.sourceCodeFile = projectSettings.sourceCodeFile?.toNioPathOrNull()
            this.externalExtensionsDirectory = projectSettings.externalExtensionsDirectory?.toNioPathOrNull()
            this.externalConfigDirectory = projectSettings.externalConfigDirectory?.toNioPathOrNull()
            this.externalDbDriversDirectory = projectSettings.externalDbDriversDirectory?.toNioPathOrNull()

            this.modulesFilesDirectory = projectSettings.ideModulesFilesDirectory?.toNioPathOrNull()
                ?: builder.fileToImport.toNioPathOrNull()?.resolve(ProjectConstants.Paths.IDEA_MODULES)

            this.ccv2Token = CCv2ProjectSettings.getInstance().getCCv2Token()
            this.excludedFromScanning = projectSettings.excludedFromScanning

            this.platformDirectory = projectSettings.hybrisDirectory
                ?.let { builder.fileToImport.toNioPathOrNull()?.resolve(it) }
                // refreshing a project which was never imported by this plugin
                ?: findPlatformDirectory()
        }

        thisLogger().info("Refreshing a project with the following settings: $importContext")
        searchModuleRoots(importContext)
    }

    override fun validate(): Boolean {
        if (!super.validate()) return false

        val firstValidationInfo = _ui.validateAll()
            .filter { it.component?.isVisible ?: false }
            .onEach { it.okEnabled = true }
            .firstOrNull()

        if (firstValidationInfo != null) {
            throw ConfigurationException(firstValidationInfo.message)
        }

        importCoreContext.projectIconFile.takeIf { importCoreContext.projectIcon.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.fileExists ?: false }
            ?.let { throw ConfigurationException("Custom project icon should point to an existing SVG file.") }

        importCoreContext.customDirectory.takeIf { importCoreContext.customDirectoryOverride.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.directoryExists ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.custom.extensions.directory.does.not.exist")) }

        importCoreContext.configDirectory.takeIf { importCoreContext.configDirectoryOverride.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.directoryExists ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.config.directory.does.not.exist")) }

        importCoreContext.dbDriverDirectory.takeIf { importCoreContext.dbDriverDirectoryOverride.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.directoryExists ?: false }
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
        val buildInfoFile = hybrisRootDir.resolve(ProjectConstants.Paths.BIN_PLATFORM_BUILD_NUMBER)
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
            val version = buildProperties.getProperty(ProjectImportConstants.HYBRIS_VERSION_KEY)
            if (version != null) return version
        }

        return buildProperties.getProperty(ProjectImportConstants.HYBRIS_API_VERSION_KEY)
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
        try {
            thisLogger().info("Setting RootProjectDirectory to ${importContext.rootDirectory}")

            LookupModuleDescriptorsTask.getInstance().execute(importContext)
        } catch (e: Exception) {
            importContext.clear()
            thisLogger().error(e.message, e)

            Messages.showErrorDialog(
                e.message,
                "Project Import"
            )
        }
    }

    private fun findPlatformDirectory(): Path? {
        try {
            val rootProjectDirectory = Path(builder.fileToImport)

            return LookupPlatformDirectoryTask.getInstance().execute(rootProjectDirectory)
        } catch (_: CancellationException) {
            // noop
        } catch (e: Exception) {
            thisLogger().error(e)
        }
        return null
    }

    private fun importBuilder() = builder as HybrisProjectImportBuilder

}