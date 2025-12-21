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
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.DefaultHybrisProjectImportBuilder
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.ProjectImportRootContext
import sap.commerce.toolset.project.descriptor.ProjectImportSettings
import sap.commerce.toolset.project.refresh.ProjectRefreshContext
import sap.commerce.toolset.project.tasks.SearchHybrisDistributionDirectoryTaskModalWindow
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

class ProjectImportWizardRootStep(wizardContext: WizardContext) : ProjectImportWizardStep(wizardContext), RefreshSupport {

    private val context by lazy {
        ProjectImportRootContext(
            settings = ProjectImportSettings.of(ApplicationSettings.getInstance()).mutable()
        )
    }
    private val _ui by lazy { ui(context) }

    override fun getComponent() = JBScrollPane(_ui).apply {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        preferredSize = Dimension(preferredSize.width, JBUIScale.scale(600))

        CCv2ProjectSettings.getInstance().loadDefaultCCv2Token { ccv2Token ->
            if (ccv2Token != null) context.ccv2Token.set(ccv2Token)
        }
    }

    override fun updateDataModel() {
        _ui.apply()

        val importBuilder = importBuilder()
        val importContext = context.settings.immutable()
        val hybrisProjectDescriptor = importBuilder.createHybrisProjectDescriptor(importContext)

        wizardContext.projectName = context.projectName.get()

        with(hybrisProjectDescriptor) {
            this.hybrisVersion = context.platformVersion.get()
            this.hybrisDistributionDirectory = FileUtils.toFile(context.platformDirectory.get())
            this.javadocUrl = context.javadocUrl.get()
            this.excludedFromScanning = context.excludedFromScanningDirectories.get().toImmutableSet()

            this.externalExtensionsDirectory = context.customDirectory.takeIf { context.customDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.externalConfigDirectory = context.configDirectory.takeIf { context.customDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.externalDbDriversDirectory = context.dbDriverDirectory.takeIf { context.dbDriverDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.projectIconFile = context.projectIconFile.takeIf { context.projectIcon.get() }
                ?.let { FileUtils.toFile(it.get()) }
            this.modulesFilesDirectory = context.moduleFilesStorageDirectory.takeIf { context.moduleFilesStorage.get() }
                ?.let { FileUtils.toFile(it.get()) }

            this.sourceCodeFile = context.sourceCodeDirectory.takeIf { context.sourceCodeDirectoryOverride.get() }
                ?.let { FileUtils.toFile(it.get()) }
                ?.let {
                    if (it.isDirectory && it.absolutePath == hybrisDistributionDirectory?.absolutePath) null
                    else it
                }

            this.ccv2Token = context.ccv2Token.get()

            thisLogger().info("importing a project with the following settings: $this")
        }

        FileUtils.toFile(importBuilder.fileToImport)
            ?.let { importBuilder.setRootProjectDirectory(it) }
    }

    override fun updateStep() {
        if (context.moduleFilesStorageDirectory.get().isBlank()) {
            context.moduleFilesStorageDirectory.set(
                Path(builder.fileToImport)
                    .resolve(ProjectConstants.Directory.PATH_IDEA_MODULES)
                    .absolutePathString()
            )
        }
        if (context.projectName.get().isBlank()) {
            context.projectName.set(wizardContext.projectName)
        }

        if (context.platformDirectory.get().isBlank()) {
            val rootProjectDirectory = File(builder.fileToImport)
            val task = SearchHybrisDistributionDirectoryTaskModalWindow(rootProjectDirectory) {
                context.platformDirectory.set(it)
            }
            ProgressManager.getInstance().run(task)
        }

        val platformPath = context.platformDirectory.get()
            .takeIf { it.isNotBlank() }
            ?.let { Path(it) }
            ?.takeIf { it.exists()  }
            ?: return

        val hybrisApiVersion = getHybrisVersion(platformPath, true)
            ?.also { context.platformVersion.set(it) }
        val hybrisVersion = getHybrisVersion(platformPath, false)
            ?.also { context.platformVersion.set(it) }

        if (context.customDirectory.get().isBlank()) {
            context.customDirectory.set(
                platformPath
                    .resolve(ProjectConstants.Directory.PATH_BIN_CUSTOM)
                    .absolutePathString()
            )
        }

        if (context.configDirectory.get().isBlank()) {
            context.configDirectory.set(
                platformPath
                    .resolve(ProjectConstants.Extension.CONFIG)
                    .absolutePathString()
            )
        }

        if (context.dbDriverDirectory.get().isBlank()) {
            val dbDriversDirAbsolutePath = platformPath
                .resolve(ProjectConstants.Directory.PATH_BIN_PLATFORM)
                .resolve(ProjectConstants.Directory.PATH_LIB_DB_DRIVER)

            context.dbDriverDirectory.set(dbDriversDirAbsolutePath.absolutePathString())
        }

        if (context.javadocUrl.get().isBlank()) {
            getPlatformJavadocUrl(hybrisApiVersion)
                .takeIf { it.isNotBlank() }
                ?.let { context.javadocUrl.set(it) }
        }

        if (context.sourceCodeDirectory.get().isBlank()) {
            val sourceCodeFile = findSourceZip(platformPath, hybrisVersion)
                ?.absolutePath
                ?: platformPath.absolutePathString()

            context.sourceCodeDirectory.set(sourceCodeFile)
        }
    }

    override fun refresh(refreshContext: ProjectRefreshContext) {
        val importBuilder = importBuilder()
        val projectDescriptor = importBuilder.createHybrisProjectDescriptor(refreshContext.importContext)
        val projectSettings = refreshContext.projectSettings

        with(projectDescriptor) {
            val platformPath = refreshContext.projectPath.resolve("hybris")

            this.hybrisVersion = getHybrisVersion(platformPath, false)
                ?: projectSettings.hybrisVersion
            this.javadocUrl = getHybrisVersion(platformPath, true)
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
                this.hybrisDistributionDirectory = FileUtils.toFile(
                    builder.fileToImport,
                    projectSettings.hybrisDirectory
                )
            }

            this.excludedFromScanning = projectSettings.excludedFromScanning.toMutableSet()
            val rootProjectDirectory = FileUtils.toFile(builder.fileToImport)!!
            importBuilder.setRootProjectDirectory(rootProjectDirectory)

            if (hybrisDirectory == null) {
                // refreshing a project which was never imported by this plugin
                val task = SearchHybrisDistributionDirectoryTaskModalWindow(rootProjectDirectory) {
                    this.hybrisDistributionDirectory = FileUtils.toFile(it)
                }

                ProgressManager.getInstance().run(task)
            }

            thisLogger().info("Refreshing a project with the following settings: $this")
        }
    }

    override fun validate(): Boolean {
        if (!super.validate()) {
            return false
        }

        context.projectIconFile.takeIf { context.projectIcon.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isFile ?: false }
            ?.let { throw ConfigurationException("Custom project icon should point to an existing SVG file.") }

        context.customDirectory.takeIf { context.customDirectoryOverride.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isDirectory ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.custom.extensions.directory.does.not.exist")) }

        context.configDirectory.takeIf { context.configDirectoryOverride.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isDirectory ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.config.directory.does.not.exist")) }

        context.dbDriverDirectory.takeIf { context.dbDriverDirectoryOverride.get() }
            ?.takeUnless { FileUtils.toFile(it.get())?.isDirectory ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.dbdriver.directory.does.not.exist")) }

        val platformDirectory = context.platformDirectory.get()
        if (platformDirectory.isBlank()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.empty"))
        if (!Path(platformDirectory).isDirectory()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.does.not.exist"))

        return true
    }

    private fun getHybrisVersion(hybrisRootDir: Path, apiOnly: Boolean): String? {
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

    private fun findSourceZip(sourceCodePath: Path, hybrisApiVersion: String?): File? {
        hybrisApiVersion ?: return null
        if (hybrisApiVersion.isBlank()) return null

        if (!sourceCodePath.isDirectory()) return null

        val sourceZipList = sourceCodePath.toFile().listFiles()
            ?.filter { it.name.endsWith(".zip") }
            ?.filter { it.name.contains(hybrisApiVersion) }
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return if (sourceZipList.size > 1) sourceZipList.maxByOrNull { getPatchVersion(it, hybrisApiVersion) }
        else sourceZipList[0]
    }

    private fun getPatchVersion(sourceZip: File, hybrisApiVersion: String): Int {
        val name = sourceZip.name
        val index = name.indexOf(hybrisApiVersion)
        val firstDigit = name[index + hybrisApiVersion.length + 1]
        val secondDigit = name[index + hybrisApiVersion.length + 2]

        return if (Character.isDigit(secondDigit)) Character.getNumericValue(firstDigit) * 10 + Character.getNumericValue(secondDigit)
        else Character.getNumericValue(firstDigit)
    }

    private fun getPlatformJavadocUrl(hybrisApiVersion: String?) = if (hybrisApiVersion?.isNotEmpty() == true) String.format(HybrisConstants.URL_HELP_JAVADOC, hybrisApiVersion)
    else HybrisConstants.URL_HELP_JAVADOC_FALLBACK

    private fun importBuilder() = builder as DefaultHybrisProjectImportBuilder

}