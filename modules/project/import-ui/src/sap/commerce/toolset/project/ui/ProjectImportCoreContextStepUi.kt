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

package sap.commerce.toolset.project.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.observable.util.and
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import org.intellij.images.fileTypes.impl.SvgFileType
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.context.ProjectImportCoreContext
import sap.commerce.toolset.project.context.findSourceCodeFile
import sap.commerce.toolset.settings.LibrarySourcesFetchMode
import sap.commerce.toolset.ui.CRUDListPanel
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import javax.swing.Icon
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.pathString

internal fun uiCoreStep(context: ProjectImportCoreContext): DialogPanel {
    val rightGaps = UnscaledGaps(0, 0, 0, 16)
    val sourceCodeInfoToggle = AtomicBooleanProperty(false)
    val sourceCodeInfoLabel = AtomicProperty("")

    return panel {
        val excludedFromScanningList = CRUDListPanel(
            "hybris.import.settings.excludedFromScanning.directory.popup.add.title",
            "hybris.import.settings.excludedFromScanning.directory.popup.add.text",
            "hybris.import.settings.excludedFromScanning.directory.popup.edit.title",
            "hybris.import.settings.excludedFromScanning.directory.popup.edit.text",
        )

        row {
            label("Project name:")
                .bold()
            textField()
                .bindText(context.projectName)
                .align(AlignX.FILL)

            label("Platform version:")
                .align(AlignX.RIGHT)
                .customize(rightGaps)
            label("")
                .bindText(context.platformVersion)
                .bold()
        }

        separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)

        row {
            cell(
                textFieldWithBrowseButton(
                    null,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                        .withTitle(i18n("hybris.import.label.select.hybris.distribution.directory"))
                )
            )
                .label(i18n("hybris.import.wizard.hybris.distribution.directory.label"))
                .bindText(context.platformDistributionPath)
                .align(AlignX.FILL)

            contextHelp(i18n("hybris.import.wizard.hybris.distribution.directory.help.description"))
                .customize(rightGaps)
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .label(i18n("hybris.import.wizard.javadoc.url.label"))
                .bindText(context.javadocUrl)
                .align(AlignX.FILL)
                .resizableColumn()

            contextHelp(i18n("hybris.import.wizard.javadoc.url.help.description"))
                .customize(rightGaps)
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Project icon:")
                .bindSelected(context.projectIcon)

            cell(
                textFieldWithBrowseButton(
                    null,
                    FileChooserDescriptorFactory.createSingleFileDescriptor(SvgFileType.INSTANCE)
                        .withTitle("Select Custom Project SVG Icon.")
                )
            )
                .bindText(context.projectIconFile)
                .enabledIf(context.projectIcon)
                .align(AlignX.FILL)
                .resizableColumn()

            contextHelp(i18n("hybris.import.label.select.hybris.project.icon.file.help.description"))
                .customize(rightGaps)
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Store .iml files in:")
                .bindSelected(context.moduleFilesStorage)

            cell(
                textFieldWithBrowseButton(
                    null,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
                        .withTitle(i18n("hybris.project.import.select.directory.where.new.idea.module.files.will.be.stored"))
                )
            )
                .bindText(context.moduleFilesStorageDirectory)
                .enabledIf(context.moduleFilesStorage)
                .align(AlignX.FILL)
                .resizableColumn()

            contextHelp("If unchecked, .iml file will be stored in the root directory of the module.")
                .customize(rightGaps)
        }.layout(RowLayout.PARENT_GRID)

        row {
            passwordField()
                .label(i18n("hybris.settings.application.ccv2Token"))
                .bindText(context.ccv2Token)
                .align(AlignX.FILL)
                .comment(i18n("hybris.settings.application.ccv2Token.tooltip"))
                .resizableColumn()

            contextHelp(i18n("hybris.settings.application.ccv2Token.help.description"))
                .customize(rightGaps)
        }.layout(RowLayout.PARENT_GRID)

        group("Override") groupPanel@{
            row {
                checkBox("Platform source code:")
                    .bindSelected(context.sourceCodePathOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                            .withExtensionFilter("zip")
                            .withTitle(i18n("hybris.import.label.select.hybris.src.file"))
                    )
                )
                    .onChanged { textField ->
                        val path = Path(textField.text)
                        context.sourceCodeFile.set("")

                        when {
                            path.fileExists -> {
                                context.sourceCodeFile.set(textField.text)
                                sourceCodeInfoToggle.set(true)
                                sourceCodeInfoLabel.set("The selected file will be used as a source in the Bootstrap Library.")
                            }

                            path.directoryExists -> {
                                // 2211.42 (with patch)
                                val platformVersion = context.platformVersion.get().takeIf { it.isNotBlank() }
                                // 2211 (without patch)
                                val platformApiVersion = context.platformApiVersion.get().takeIf { it.isNotBlank() }

                                val infoText = path.findSourceCodeFile(platformVersion, platformApiVersion)
                                    ?.also { context.sourceCodeFile.set(it.pathString) }
                                    ?.let { "Best-matching source code file detected: ${it.name}." }
                                    ?: "No source code file matching the specified platform version was found in the directory."

                                sourceCodeInfoLabel.set(infoText)
                                sourceCodeInfoToggle.set(true)
                            }

                            else -> {
                                sourceCodeInfoLabel.set("")
                                sourceCodeInfoToggle.set(false)
                            }
                        }
                    }
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .bindText(context.sourceCodePath)
                    .enabledIf(context.sourceCodePathOverride)

                contextHelp("Select platform source code zip file or directory")
                    .customize(rightGaps)
            }.layout(RowLayout.PARENT_GRID)

            indent {
                row {
                    label("")
                        .bindText(sourceCodeInfoLabel)
                        .applyToComponent { foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND }
                }
                    .topGap(TopGap.SMALL)
                    .layout(RowLayout.PARENT_GRID)
                    .visibleIf(context.sourceCodePathOverride.and(sourceCodeInfoToggle))
            }

            row {
                checkBox("Custom directory:")
                    .bindSelected(context.customDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.custom.extensions.directory"))
                    )
                )
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .bindText(context.customDirectory)
                    .enabledIf(context.customDirectoryOverride)

                contextHelp("If your custom directory is in bin/ext-* directory or is outside the project root directory.")
                    .customize(rightGaps)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox("Config directory:")
                    .bindSelected(context.configDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.config.extensions.directory"))
                    )
                )
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .bindText(context.configDirectory)
                    .enabledIf(context.configDirectoryOverride)

                contextHelp(
                    """
                    The config directory that will be used for import.<br>
                    This is equivalent of environment parameter HYBRIS_CONFIG_DIR.
                    """.trimIndent()
                )
                    .customize(rightGaps)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox("DB driver directory:")
                    .bindSelected(context.dbDriverDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.dbdriver.extensions.directory"))
                    )
                )
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .bindText(context.dbDriverDirectory)
                    .enabledIf(context.dbDriverDirectoryOverride)

                contextHelp("The DB driver directory that contains DB driver jar files (used to execute Integration tests from IDE).")
                    .customize(rightGaps)
            }.layout(RowLayout.PARENT_GRID)
        }

        group(i18n("hybris.project.import.projectImportSettings.title")) {
            row {
                checkBox(i18n("hybris.import.wizard.import.ootb.modules.read.only.label"))
                    .bindSelected(context.importSettings.importOOTBModulesInReadOnlyMode)
                contextHelp(i18n("hybris.import.wizard.import.ootb.modules.read.only.help.description"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions"))
                    .bindSelected(context.importSettings.useFakeOutputPathForCustomExtensions)
                contextHelp(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions.help.description"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.ignoreNonExistingSourceDirectories"))
                    .bindSelected(context.importSettings.ignoreNonExistingSourceDirectories)
                contextHelp(i18n("hybris.project.import.ignoreNonExistingSourceDirectories.help.description"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                    .bindSelected(context.importSettings.importCustomAntBuildFiles)
                contextHelp(i18n("hybris.project.import.importCustomAntBuildFiles.help.description"))
                    .customize(rightGaps)
            }

            row {
                comboBox(
                    model = EnumComboBoxModel(LibrarySourcesFetchMode::class.java),
                    renderer = SimpleListCellRenderer.create("?") { it.presentationText }
                )
                    .bindItem(context.importSettings.librarySourcesFetchMode)

                label(i18n("hybris.project.import.downloadAndAttachLibraryResources.title"))

                checkBox(i18n("hybris.project.import.withExternalLibrarySources"))
                    .bindSelected(context.importSettings.withExternalLibrarySources)

                checkBox(i18n("hybris.project.import.withExternalLibraryJavadocs"))
                    .bindSelected(context.importSettings.withExternalLibraryJavadocs)

                contextHelp(i18n("hybris.project.import.withExternalLibrarySources.help.description"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.withDecompiledOotbSources"))
                    .bindSelected(context.importSettings.withDecompiledOotbSources)
                contextHelp(i18n("hybris.project.import.withDecompiledOotbSources.help.description"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.isExcludedFromScanning"))
                    .bindSelected(context.isExcludedFromScanning)
                contextHelp(i18n("hybris.project.import.isExcludedFromScanning.help.description"))
                    .customize(rightGaps)
            }

            row {
                cell(excludedFromScanningList)
                    .bind(
                        { listPanel -> listPanel.data },
                        { _, values -> context.excludedFromScanningDirectories.set(values) },
                        context.excludedFromScanningDirectories
                    )
                    .visibleIf(context.isExcludedFromScanning)
                    .align(AlignX.FILL)
            }
        }

        group(i18n("hybris.project.import.projectStructure.title")) {
            row {
                checkBox(i18n("hybris.project.view.tree.hide.empty.middle.folders"))
                    .bindSelected(context.importSettings.hideEmptyMiddleFolders)
            }

            row {
                checkBox(i18n("hybris.import.settings.group.modules"))
                    .bindSelected(context.importSettings.groupModules)
            }

            indent {
                row {
                    comment(i18n("hybris.import.settings.group.modules.help"))
                }

                groupProperties(
                    HybrisIcons.Extension.CUSTOM,
                    i18n("hybris.import.settings.group.custom"),
                    i18n("hybris.import.settings.group.unused"),
                    context.importSettings.groupCustom,
                    context.importSettings.groupOtherCustom,
                    context.importSettings.groupModules
                )
                groupProperties(
                    HybrisIcons.Extension.OOTB,
                    i18n("hybris.import.settings.group.hybris"),
                    i18n("hybris.import.settings.group.unused"),
                    context.importSettings.groupHybris,
                    context.importSettings.groupOtherHybris,
                    context.importSettings.groupModules
                )
                groupProperties(
                    HybrisIcons.Extension.PLATFORM,
                    i18n("hybris.import.settings.group.platform"),
                    i18n("hybris.import.settings.group.nonhybris"),
                    context.importSettings.groupPlatform,
                    context.importSettings.groupNonHybris,
                    context.importSettings.groupModules
                )

                row {
                    icon(HybrisIcons.Module.CCV2_GROUP)
                    groupProperty(
                        i18n("hybris.import.settings.group.ccv2"),
                        context.importSettings.groupCCv2,
                        context.importSettings.groupModules
                    )
                }.layout(RowLayout.PARENT_GRID)
            }.visibleIf(context.importSettings.groupModules)

            row {
                checkBox("Group external modules")
                    .bindSelected(context.importSettings.groupExternalModules)
                contextHelp(i18n("hybris.project.view.external.module.help.description"))
            }

            indent {
                row {
                    icon(HybrisIcons.Module.EXTERNAL_GROUP)
                    groupProperty(
                        i18n("hybris.import.settings.group.externalModules"),
                        context.importSettings.groupNameExternalModules,
                        context.importSettings.groupExternalModules
                    )
                }
            }.visibleIf(context.importSettings.groupExternalModules)
        }
    }
}

private fun Panel.groupProperties(
    icon: Icon,
    groupLabel: String,
    groupOtherLabel: String,
    groupProperty: ObservableMutableProperty<String>,
    groupOtherProperty: ObservableMutableProperty<String>,
    enabledIf: ObservableProperty<Boolean>
) {
    row {
        icon(icon)
        groupProperty(groupLabel, groupProperty, enabledIf)
        groupProperty(groupOtherLabel, groupOtherProperty, enabledIf)
    }.layout(RowLayout.PARENT_GRID)
}

private fun Row.groupProperty(
    groupLabel: String,
    groupProperty: ObservableMutableProperty<String>,
    enabledIf: ObservableProperty<Boolean>
) {
    label(groupLabel)
    textField()
        .bindText(groupProperty)
        .addValidationRule(i18n("hybris.settings.validations.notBlank")) { it.text.isBlank() }
        .enabledIf(enabledIf)
        .applyIfEnabled()
}