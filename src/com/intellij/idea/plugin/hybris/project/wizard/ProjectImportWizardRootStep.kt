/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.project.wizard

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.project.AbstractHybrisProjectImportBuilder
import com.intellij.idea.plugin.hybris.project.tasks.SearchHybrisDistributionDirectoryTaskModalWindow
import com.intellij.idea.plugin.hybris.project.utils.FileUtils
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings
import com.intellij.idea.plugin.hybris.ui.CRUDListPanel
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.selected
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.util.maximumHeight
import com.intellij.ui.util.preferredHeight
import org.apache.commons.lang3.StringUtils
import org.intellij.images.fileTypes.impl.SvgFileType
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import javax.swing.JTextField
import javax.swing.ScrollPaneConstants

class ProjectImportWizardRootStep(context: WizardContext) : ProjectImportWizardStep(context), OpenSupport, RefreshSupport {

    private val logger = Logger.getInstance(ProjectImportWizardRootStep::class.java)

    private lateinit var projectNameTextField: JTextField
    private lateinit var hybrisVersionTextField: JTextField
    private lateinit var javadocUrlTextField: JTextField
    private lateinit var hybrisDistributionDirectoryFilesInChooser: TextFieldWithBrowseButton
    private lateinit var sourceCodeFilesInChooser: TextFieldWithBrowseButton
    private lateinit var storeModuleFilesInChooser: TextFieldWithBrowseButton
    private lateinit var customProjectIconChooser: TextFieldWithBrowseButton
    private lateinit var overrideCustomDirChooser: TextFieldWithBrowseButton
    private lateinit var overrideConfigDirChooser: TextFieldWithBrowseButton
    private lateinit var overrideDBDriverDirChooser: TextFieldWithBrowseButton
    private lateinit var ignoreNonExistingSourceDirectories: JBCheckBox
    private lateinit var withStandardProvidedSources: JBCheckBox
    private lateinit var importCustomAntBuildFilesCheckBox: JBCheckBox
    private lateinit var importOotbModulesInReadOnlyModeCheckBox: JBCheckBox
    private lateinit var excludeTestSourcesCheckBox: JBCheckBox
    private lateinit var scanThroughExternalModuleCheckbox: JBCheckBox
    private lateinit var followSymlinkCheckbox: JBCheckBox
    private lateinit var overrideDBDriverDirCheckBox: JBCheckBox
    private lateinit var sourceCodeCheckBox: JBCheckBox
    private lateinit var storeModuleFilesInCheckBox: JBCheckBox
    private lateinit var excludedFromScanningCheckBox: JBCheckBox
    private lateinit var useFakeOutputPathForCustomExtensionsCheckbox: JBCheckBox

    private val excludedFromScanningList = CRUDListPanel(
        "hybris.import.settings.excludedFromScanning.directory.popup.add.title",
        "hybris.import.settings.excludedFromScanning.directory.popup.add.text",
        "hybris.import.settings.excludedFromScanning.directory.popup.edit.title",
        "hybris.import.settings.excludedFromScanning.directory.popup.edit.text",
    )

    private var panel = panel {
        row {
            label("Project name:")
                .bold()
            projectNameTextField = textField()
                .align(AlignX.FILL)
                .component
            hybrisVersionTextField = textField()
                .label("SAP CX version:")
                .align(AlignX.RIGHT)
                .enabled(false)
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            val projectIconCheckBox = checkBox("Custom project icon:").component
            customProjectIconChooser = cell(
                textFieldWithBrowseButton(
                    null,
                    "Select Custom Project SVG Icon.",
                    FileChooserDescriptorFactory.createSingleFileDescriptor(SvgFileType.INSTANCE)
                )
            )
                .align(AlignX.FILL)
                .enabledIf(projectIconCheckBox.selected)
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            storeModuleFilesInCheckBox = checkBox("Store IDEA module files in:")
                .selected(true)
                .comment("If unchecked, .iml file will be stored in the root directory of the module.")
                .component
            storeModuleFilesInChooser = cell(
                textFieldWithBrowseButton(
                    null,
                    message("hybris.project.import.select.directory.where.new.idea.module.files.will.be.stored"),
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                )
            )
                .enabledIf(storeModuleFilesInCheckBox.selected)
                .align(AlignX.FILL)
                .component
        }.layout(RowLayout.PARENT_GRID)

        collapsibleGroup("Scanning Settings") {
            row {
                scanThroughExternalModuleCheckbox = checkBox("Scan for SAP CX modules even in external modules")
                    .comment("Eclipse, Gradle and Maven projects. (slower import/refresh)")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                followSymlinkCheckbox = checkBox("Include symbolic links for a project import")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                excludedFromScanningCheckBox = checkBox("Directories excluded from the project scanning")
                    .comment("Specify directories related to the project root, use '/' separator for sub-directories.")
                    .component
            }
            row {
                cell(excludedFromScanningList)
                    .enabledIf(excludedFromScanningCheckBox.selected)
                    .visibleIf(excludedFromScanningCheckBox.selected)
                    .align(AlignX.FILL)
            }
        }
            .expanded = true

        collapsibleGroup("Import Settings") {
            row {
                label("SAP CX installation directory:")
                hybrisDistributionDirectoryFilesInChooser = cell(
                    textFieldWithBrowseButton(
                        null,
                        message("hybris.import.label.select.hybris.distribution.directory"),
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                )
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("SAP CX javadoc url:")
                javadocUrlTextField = textField()
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                sourceCodeCheckBox = checkBox("SAP CX source code:").component
                sourceCodeFilesInChooser = cell(
                    textFieldWithBrowseButton(
                        null,
                        message("hybris.import.label.select.hybris.src.file"),
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                )
                    .align(AlignX.FILL)
                    .enabledIf(sourceCodeCheckBox.selected)
                    .component
            }.layout(RowLayout.PARENT_GRID)
            row {
                useFakeOutputPathForCustomExtensionsCheckbox = checkBox("Use fake output path for custom extensions")
                    .comment("When enabled the `eclipsebin’ folder will be used as an output path for both custom and OOTB extensions")
                    .component
            }.layout(RowLayout.PARENT_GRID)
            row {
                importOotbModulesInReadOnlyModeCheckBox = checkBox("Import OOTB modules in read-only mode test")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                excludeTestSourcesCheckBox = checkBox("Exclude test sources for OOTB modules")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                ignoreNonExistingSourceDirectories = checkBox("Ignore non-existing source directories")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                withStandardProvidedSources = checkBox("Attach standard sources")
                    .comment("(e.g. backoffice), platformservices module sources will be always attached.")
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                importCustomAntBuildFilesCheckBox = checkBox("Import Ant build files for custom modules")
                    .comment("Due nature of the Ant plugin may negatively affect project import/refresh performance.")
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
            .expanded = true

        collapsibleGroup("Override Settings") {
            row {
                val overrideCustomDirCheckBox = checkBox("Override custom directory:")
                    .comment("If your custom directory is in bin/ext-* directory or is outside the project root directory.")
                    .component
                overrideCustomDirChooser = cell(
                    textFieldWithBrowseButton(
                        null,
                        message("hybris.import.label.select.custom.extensions.directory"),
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                )
                    .align(AlignX.FILL)
                    .enabledIf(overrideCustomDirCheckBox.selected)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                val overrideConfigDirCheckBox = checkBox("Override config directory:")
                    .comment(
                        """
                    The config directory that will be used for import.<br>
                    This is equivalent of environment parameter HYBRIS_CONFIG_DIR.
                    """.trimIndent()
                    )
                    .component
                overrideConfigDirChooser = cell(
                    textFieldWithBrowseButton(
                        null,
                        message("hybris.import.label.select.config.extensions.directory"),
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                )
                    .align(AlignX.FILL)
                    .enabledIf(overrideConfigDirCheckBox.selected)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                overrideDBDriverDirCheckBox = checkBox("Override DB driver directory:")
                    .comment("The DB driver directory that contains DB driver jar files (used to execute Integration tests from IDE).")
                    .component
                overrideDBDriverDirChooser = cell(
                    textFieldWithBrowseButton(
                        null,
                        message("hybris.import.label.select.dbdriver.extensions.directory"),
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                )
                    .align(AlignX.FILL)
                    .enabledIf(overrideDBDriverDirCheckBox.selected)
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
            .expanded = true
    }

    override fun getComponent() = with(JBScrollPane(panel)) {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        preferredHeight = JBUIScale.scale(600)
        maximumHeight = JBUIScale.scale(600)
        this
    }

    override fun updateDataModel() {
        val context = context()
        context.cleanup()

        wizardContext.projectName = projectNameTextField.text

        with(context.hybrisProjectDescriptor) {
            this.hybrisVersion = hybrisVersionTextField.text
            this.hybrisDistributionDirectory = FileUtils.toFile(hybrisDistributionDirectoryFilesInChooser.text)

            this.javadocUrl = javadocUrlTextField.text

            this.isIgnoreNonExistingSourceDirectories = ignoreNonExistingSourceDirectories.isSelected
            this.isWithStandardProvidedSources = withStandardProvidedSources.isSelected
            this.isImportOotbModulesInReadOnlyMode = importOotbModulesInReadOnlyModeCheckBox.isSelected
            this.isFollowSymlink = followSymlinkCheckbox.isSelected
            this.isExcludeTestSources = excludeTestSourcesCheckBox.isSelected
            this.isImportCustomAntBuildFiles = importCustomAntBuildFilesCheckBox.isSelected
            this.isScanThroughExternalModule = scanThroughExternalModuleCheckbox.isSelected
            this.isUseFakeOutputPathForCustomExtensions = useFakeOutputPathForCustomExtensionsCheckbox.isSelected

            this.setExcludedFromScanning(excludedFromScanningList.data)

            this.externalExtensionsDirectory = overrideCustomDirChooser.takeIf { it.isEnabled }
                ?.let { FileUtils.toFile(it.text) }
            this.externalExtensionsDirectory = overrideConfigDirChooser.takeIf { it.isEnabled }
                ?.let { FileUtils.toFile(it.text) }
            this.externalExtensionsDirectory = overrideDBDriverDirChooser.takeIf { it.isEnabled }
                ?.let { FileUtils.toFile(it.text) }
            this.sourceCodeFile = sourceCodeFilesInChooser.takeIf { it.isEnabled }
                ?.let { File(it.text) }
                ?.takeIf { it.exists() }
                ?.let {
                    return@let if (it.isDirectory && it.absolutePath == File(hybrisDistributionDirectoryFilesInChooser.text).absolutePath) null
                    else it
                }
            this.projectIconFile = customProjectIconChooser.takeIf { it.isEnabled }
                ?.let { File(it.text) }
                ?.takeIf { it.exists() && it.isFile }
            storeModuleFilesInChooser
                .takeIf { it.isEnabled }
                ?.let { this.modulesFilesDirectory = FileUtils.toFile(storeModuleFilesInChooser.text) }

            logger.info("importing a project with the following settings: $this")
        }

        FileUtils.toFile(context.fileToImport)
            ?.let { context.setRootProjectDirectory(it) }
    }

    override fun updateStep() {
        val appSettings = HybrisApplicationSettingsComponent.getInstance().state
        storeModuleFilesInChooser.text = File(
            builder.fileToImport, HybrisConstants.DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES
        ).absolutePath

        projectNameTextField.text = wizardContext.projectName

        with(context().getHybrisProjectDescriptor()) {
            this.isImportOotbModulesInReadOnlyMode = appSettings.defaultPlatformInReadOnly
            this.isFollowSymlink = appSettings.followSymlink
            this.isScanThroughExternalModule = appSettings.scanThroughExternalModule
            this.isExcludeTestSources = appSettings.excludeTestSources
            this.isImportCustomAntBuildFiles = appSettings.importCustomAntBuildFiles
            this.isIgnoreNonExistingSourceDirectories = appSettings.ignoreNonExistingSourceDirectories
            this.isWithStandardProvidedSources = appSettings.withStandardProvidedSources

            importOotbModulesInReadOnlyModeCheckBox.isSelected = this.isImportOotbModulesInReadOnlyMode
            scanThroughExternalModuleCheckbox.isSelected = this.isScanThroughExternalModule
            followSymlinkCheckbox.setSelected(this.isFollowSymlink)
            excludeTestSourcesCheckBox.isSelected = this.isExcludeTestSources
            importCustomAntBuildFilesCheckBox.isSelected = this.isImportCustomAntBuildFiles
        }

        if (hybrisDistributionDirectoryFilesInChooser.text.isBlank()) {
            val task = SearchHybrisDistributionDirectoryTaskModalWindow(File(builder.fileToImport)) {
                hybrisDistributionDirectoryFilesInChooser.text = it

                if (sourceCodeFilesInChooser.text.isBlank()) {
                    sourceCodeFilesInChooser.text = hybrisDistributionDirectoryFilesInChooser.text
                }
            }
            ProgressManager.getInstance().run(task)
        }

        if (hybrisDistributionDirectoryFilesInChooser.text.isNotBlank()) {
            if (overrideCustomDirChooser.getText().isBlank()) {
                overrideCustomDirChooser.text = File(
                    hybrisDistributionDirectoryFilesInChooser.text,
                    HybrisConstants.CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH
                ).absolutePath
            }

            if (StringUtils.isBlank(overrideConfigDirChooser.getText())) {
                overrideConfigDirChooser.text = File(
                    hybrisDistributionDirectoryFilesInChooser.text,
                    HybrisConstants.EXTENSION_NAME_CONFIG
                ).absolutePath
            }

            if (overrideDBDriverDirChooser.getText().isBlank()) {
                var dbDriversDirAbsPath = File(
                    hybrisDistributionDirectoryFilesInChooser.text,
                    HybrisConstants.PLATFORM_MODULE_PREFIX + HybrisConstants.PLATFORM_DB_DRIVER
                ).absolutePath

                if (appSettings.externalDbDriversDirectory.isNotEmpty()) {
                    File(appSettings.externalDbDriversDirectory)
                        .takeIf { it.isDirectory }
                        ?.let {
                            dbDriversDirAbsPath = it.absolutePath

                            if (!overrideDBDriverDirCheckBox.isSelected) {
                                overrideDBDriverDirCheckBox.doClick()
                            }
                        }
                }
                overrideDBDriverDirChooser.text = dbDriversDirAbsPath
            }

            val hybrisVersion = getHybrisVersion(hybrisDistributionDirectoryFilesInChooser.text, false)
            hybrisVersionTextField.text = hybrisVersion
            val sourceCodeDirectory = appSettings.sourceCodeDirectory
            val sourceFile = if (appSettings.sourceZipUsed
            ) {
                findSourceZip(sourceCodeDirectory, hybrisVersion)
            } else File(sourceCodeDirectory)

            if (sourceFile != null) {
                if (!sourceCodeCheckBox.isSelected) {
                    sourceCodeCheckBox.doClick()
                }
                sourceCodeFilesInChooser.text = sourceFile.path
            }

            val defaultJavadocUrl = getDefaultJavadocUrl(getHybrisVersion(hybrisDistributionDirectoryFilesInChooser.text, true))
            if (defaultJavadocUrl.isNotBlank()) {
                javadocUrlTextField.text = defaultJavadocUrl
            }
        }

        if (sourceCodeFilesInChooser.text.isBlank()) {
            sourceCodeFilesInChooser.text = hybrisDistributionDirectoryFilesInChooser.text
        }
    }

    override fun open(settings: HybrisProjectSettings) {
        updateStep()
        updateDataModel()
    }

    override fun refresh(settings: HybrisProjectSettings) {
        val context = context()
        context.cleanup()

        with(context.getHybrisProjectDescriptor()) {
            this.javadocUrl = settings.javadocUrl
            this.hybrisVersion = settings.hybrisVersion
            this.sourceCodeFile = FileUtils.toFile(settings.sourceCodeFile, true)
            this.externalExtensionsDirectory = FileUtils.toFile(settings.externalExtensionsDirectory, true)
            this.externalConfigDirectory = FileUtils.toFile(settings.externalConfigDirectory, true)
            this.externalDbDriversDirectory = FileUtils.toFile(settings.externalDbDriversDirectory, true)
            this.isImportOotbModulesInReadOnlyMode = settings.importOotbModulesInReadOnlyMode
            this.isFollowSymlink = settings.followSymlink
            this.isExcludeTestSources = settings.excludeTestSources
            this.isImportCustomAntBuildFiles = settings.importCustomAntBuildFiles
            this.isScanThroughExternalModule = settings.scanThroughExternalModule
            this.isUseFakeOutputPathForCustomExtensions = settings.useFakeOutputPathForCustomExtensions

            val appSettings = HybrisApplicationSettingsComponent.getInstance().state
            this.isIgnoreNonExistingSourceDirectories = appSettings.ignoreNonExistingSourceDirectories
            this.isWithStandardProvidedSources = appSettings.withStandardProvidedSources

            this.modulesFilesDirectory = settings.ideModulesFilesDirectory
                ?.let { File(it) }
                ?: File(
                    builder.fileToImport,
                    HybrisConstants.DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES
                )

            val hybrisDirectory = settings.hybrisDirectory
            if (hybrisDirectory != null) {
                this.hybrisDistributionDirectory = FileUtils.toFile(
                    builder.fileToImport,
                    settings.hybrisDirectory
                )
            }

            this.setExcludedFromScanning(settings.excludedFromScanning)
            val rootProjectDirectory = FileUtils.toFile(builder.fileToImport)!!
            context.setRootProjectDirectory(rootProjectDirectory)

            if (hybrisDirectory == null) {
                // refreshing a project which was never imported by this plugin
                val task = SearchHybrisDistributionDirectoryTaskModalWindow(rootProjectDirectory) {
                    this.hybrisDistributionDirectory = FileUtils.toFile(it)
                }

                ProgressManager.getInstance().run(task)
            }

            logger.info("Refreshing a project with the following settings: $this")
        }
    }

    override fun validate(): Boolean {
        if (!super.validate()) {
            return false
        }

        if (customProjectIconChooser.isEnabled && !File(customProjectIconChooser.getText()).isFile()) {
            throw ConfigurationException("Custom project icon should point to an existing SVG file.")
        }

        if (overrideCustomDirChooser.isEnabled && !File(overrideCustomDirChooser.getText()).isDirectory()) {
            throw ConfigurationException(message("hybris.import.wizard.validation.custom.extensions.directory.does.not.exist"))
        }

        if (overrideConfigDirChooser.isEnabled && !File(overrideConfigDirChooser.getText()).isDirectory()) {
            throw ConfigurationException(message("hybris.import.wizard.validation.config.directory.does.not.exist"))
        }

        if (overrideDBDriverDirChooser.isEnabled && !File(overrideDBDriverDirChooser.getText()).isDirectory()) {
            throw ConfigurationException(message("hybris.import.wizard.validation.dbdriver.directory.does.not.exist"))
        }

        if (hybrisDistributionDirectoryFilesInChooser.text.isBlank()) {
            throw ConfigurationException(message("hybris.import.wizard.validation.hybris.distribution.directory.empty"))
        }

        if (!File(hybrisDistributionDirectoryFilesInChooser.text).isDirectory) {
            throw ConfigurationException(message("hybris.import.wizard.validation.hybris.distribution.directory.does.not.exist"))
        }

        return true
    }

    private fun getHybrisVersion(hybrisRootDir: String, apiOnly: Boolean): String? {
        val buildInfoFile = File(hybrisRootDir + HybrisConstants.BUILD_NUMBER_FILE_PATH)
        val buildProperties = Properties()

        try {
            FileInputStream(buildInfoFile).use { fis ->
                buildProperties.load(fis)
            }
        } catch (e: IOException) {
            return null
        }

        if (!apiOnly) {
            val version = buildProperties.getProperty(HybrisConstants.HYBRIS_VERSION_KEY)
            if (version != null) return version
        }

        return buildProperties.getProperty(HybrisConstants.HYBRIS_API_VERSION_KEY)
    }

    private fun findSourceZip(sourceCodeDir: String, hybrisApiVersion: String?): File? {
        hybrisApiVersion ?: return null
        if (sourceCodeDir.isBlank() || hybrisApiVersion.isBlank()) return null

        val sourceCodeDirectory = File(sourceCodeDir)
        if (!sourceCodeDirectory.isDirectory) return null

        val sourceZipList = sourceCodeDirectory.listFiles()
            ?.filter { it.name.endsWith(".zip") }
            ?.filter { it.name.contains(hybrisApiVersion) }
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return if (sourceZipList.size > 1) sourceZipList.maxByOrNull { getPatch(it, hybrisApiVersion) }
        else sourceZipList[0]
    }

    private fun getPatch(sourceZip: File, hybrisApiVersion: String): Int {
        val name = sourceZip.name
        val index = name.indexOf(hybrisApiVersion)
        val firstDigit = name[index + hybrisApiVersion.length + 1]
        val secondDigit = name[index + hybrisApiVersion.length + 2]

        return if (Character.isDigit(secondDigit)) Character.getNumericValue(firstDigit) * 10 + Character.getNumericValue(secondDigit)
        else Character.getNumericValue(firstDigit)
    }

    private fun getDefaultJavadocUrl(hybrisApiVersion: String?) = if (hybrisApiVersion?.isNotEmpty() == true) String.format(HybrisConstants.JAVADOC_URL, hybrisApiVersion)
    else HybrisConstants.JAVADOC_FALLBACK_URL

    private fun context() = builder as AbstractHybrisProjectImportBuilder
}