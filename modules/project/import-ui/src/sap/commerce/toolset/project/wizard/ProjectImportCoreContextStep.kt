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
import sap.commerce.toolset.project.context.*
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.tasks.LookupModuleDescriptorsTask
import sap.commerce.toolset.project.tasks.LookupPlatformDirectoryTask
import sap.commerce.toolset.project.ui.uiCoreStep
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import java.awt.Dimension
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.swing.ScrollPaneConstants
import kotlin.io.path.*

class ProjectImportCoreContextStep(context: WizardContext) : ProjectImportWizardStep(context), RefreshSupport {

    private val importCoreContext by lazy {
        ProjectImportCoreContext(
            importSettings = ProjectImportSettings.of(ApplicationSettings.getInstance()).mutable()
        )
    }
    private val _ui by lazy {
        uiCoreStep(importCoreContext)
            .also { it.registerValidators(wizardContext.disposable) }
    }

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
        val importContext = importBuilder().initContext(importSettings, removeExternalModules = false)

        wizardContext.projectName = importCoreContext.projectName.get()

        with(importContext) {
            this.platformVersion = importCoreContext.platformVersion.get()
            this.platformDistributionPath = importCoreContext.platformDistributionPath.get().toNioPathOrNull()
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

            this.sourceCodePath = importCoreContext.sourceCodePath.takeIf { importCoreContext.sourceCodePathOverride.get() }
                ?.get()
                ?.toNioPathOrNull()
                ?.takeIf { it.exists() }
            this.sourceCodeFile = importCoreContext.sourceCodeFile.takeIf { importCoreContext.sourceCodePathOverride.get() }
                ?.get()
                ?.toNioPathOrNull()
                ?.takeIf { it.fileExists }

            this.ccv2Token = importCoreContext.ccv2Token.get()
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

        if (importCoreContext.platformDistributionPath.get().isBlank()) {
            findPlatformDistributionPath()
                ?.let { importCoreContext.platformDistributionPath.set(it.normalize().pathString) }
        }

        val platformPath = importCoreContext.platformDistributionPath.get()
            .takeIf { it.isNotBlank() }
            ?.let { Path(it) }
            ?.takeIf { it.exists() }
            ?: return

        val platformApiVersion = getPlatformVersion(platformPath, true)
            ?.also {
                importCoreContext.platformApiVersion.set(it)
                importCoreContext.platformVersion.set(it)
            }
        val platformVersion = getPlatformVersion(platformPath, false)
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
            getPlatformJavadocUrl(platformApiVersion)
                .takeIf { it.isNotBlank() }
                ?.let { importCoreContext.javadocUrl.set(it) }
        }

        if (importCoreContext.sourceCodePath.get().isBlank()) {
            val sourceCodeFile = platformPath.findSourceCodeFile(platformVersion, platformApiVersion)
                ?.pathString
                ?.also { importCoreContext.sourceCodeFile.set(it) }
                ?: platformPath.pathString

            importCoreContext.sourceCodePath.set(sourceCodeFile)
        }
    }

    override fun refresh(refreshContext: ProjectRefreshContext) {
        val importSettings = refreshContext.importSettings
        val projectSettings = refreshContext.project.ySettings
        val importContext = importBuilder().initContext(importSettings, refreshContext.removeExternalModules)

        with(importContext) {
            val resolvedPlatformDistributionPath = (projectSettings.platformRelativePath
                ?.let { refreshContext.projectPath.resolve(it) }
            // refreshing a project which was never imported by this plugin
                ?: findPlatformDistributionPath())
            this.platformDistributionPath = resolvedPlatformDistributionPath

            val platformApiVersion = resolvedPlatformDistributionPath?.let { getPlatformVersion(it, true) }
            val platformVersion = resolvedPlatformDistributionPath?.let { getPlatformVersion(it, false) }

            this.platformVersion = platformVersion
                ?: projectSettings.hybrisVersion
            this.javadocUrl = platformApiVersion
                ?.let { getPlatformJavadocUrl(it) }
                ?.takeIf { it.isNotBlank() }
                ?: projectSettings.javadocUrl
            this.sourceCodePath = projectSettings.sourceCodePath?.toNioPathOrNull()
            this.sourceCodeFile = this.sourceCodePath?.findSourceCodeFile(platformVersion, platformApiVersion)

            this.externalExtensionsDirectory = projectSettings.externalExtensionsDirectory?.toNioPathOrNull()
            this.externalConfigDirectory = projectSettings.externalConfigDirectory?.toNioPathOrNull()
            this.externalDbDriversDirectory = projectSettings.externalDbDriversDirectory?.toNioPathOrNull()

            this.modulesFilesDirectory = projectSettings.ideModulesFilesDirectory?.toNioPathOrNull()
                ?: refreshContext.projectPath.resolve(ProjectConstants.Paths.IDEA_MODULES)

            this.ccv2Token = CCv2ProjectSettings.getInstance().getCCv2Token()
            this.excludedFromScanning = projectSettings.excludedFromScanning
        }

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

        val distributionPath = importCoreContext.platformDistributionPath.get()
        if (distributionPath.isBlank()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.empty"))
        if (!Path(distributionPath).isDirectory()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.does.not.exist"))

        return true
    }

    private fun getPlatformVersion(platformPath: Path, apiOnly: Boolean): String? {
        val buildInfoFile = platformPath.resolve(ProjectConstants.Paths.BIN_PLATFORM_BUILD_NUMBER)
        val buildProperties = Properties()

        try {
            Files.newBufferedReader(buildInfoFile, Charsets.UTF_8).use { fis ->
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

    private fun getPlatformJavadocUrl(platformApiVersion: String?) =
        if (platformApiVersion?.isNotEmpty() == true) String.format(HybrisConstants.URL_HELP_JAVADOC, platformApiVersion)
        else HybrisConstants.URL_HELP_JAVADOC_FALLBACK

    private fun searchModuleRoots(importContext: ProjectImportContext.Mutable) {
        try {
            thisLogger().debug("Setting RootProjectDirectory to ${importContext.rootDirectory}")

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

    private fun findPlatformDistributionPath(): Path? {
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