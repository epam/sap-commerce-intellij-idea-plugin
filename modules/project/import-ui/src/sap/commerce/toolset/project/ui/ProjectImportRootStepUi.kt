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
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import org.intellij.images.fileTypes.impl.SvgFileType
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.ProjectImportRootContext
import sap.commerce.toolset.ui.CRUDListPanel

internal fun ui(context: ProjectImportRootContext): DialogPanel {
    val rightGaps = UnscaledGaps(0, 0, 0, 16)

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
                .bindText(context.platformDirectory)
                .align(AlignX.FILL)

            contextHelp(i18n("hybris.import.wizard.hybris.distribution.directory.tooltip"))
                .customize(rightGaps)
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .label(i18n("hybris.import.wizard.javadoc.url.label"))
                .bindText(context.javadocUrl)
                .align(AlignX.FILL)
                .resizableColumn()

            contextHelp(i18n("hybris.import.wizard.javadoc.url.tooltip"))
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

            contextHelp(i18n("hybris.import.label.select.hybris.project.icon.file"))
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

            contextHelp(i18n("hybris.settings.application.ccv2Token.comment"))
                .customize(rightGaps)
        }.layout(RowLayout.PARENT_GRID)

        group(i18n("hybris.project.import.projectImportSettings.title")) {
            row {
                checkBox(i18n("hybris.project.import.scanExternalModules"))
                    .bindSelected(context.settings.scanThroughExternalModule)
                contextHelp(i18n("hybris.project.import.scanExternalModules.tooltip"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.followSymlink"))
                    .bindSelected(context.settings.followSymlink)
                    .component
            }

            row {
                checkBox(i18n("hybris.import.wizard.import.ootb.modules.read.only.label"))
                    .bindSelected(context.settings.importOOTBModulesInReadOnlyMode)
                contextHelp(i18n("hybris.import.wizard.import.ootb.modules.read.only.tooltip"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                    .bindSelected(context.settings.importCustomAntBuildFiles)
                contextHelp(i18n("hybris.project.import.importCustomAntBuildFiles.tooltip"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.withStandardProvidedSources"))
                    .bindSelected(context.settings.withStandardProvidedSources)
                contextHelp(i18n("hybris.project.import.withStandardProvidedSources.tooltip"))
                    .customize(rightGaps)
            }

            row {
                label(i18n("hybris.project.import.downloadAndAttachLibraryResources.title"))

                checkBox(i18n("hybris.project.import.withExternalLibrarySources"))
                    .bindSelected(context.settings.withExternalLibrarySources)

                checkBox(i18n("hybris.project.import.withExternalLibraryJavadocs"))
                    .bindSelected(context.settings.withExternalLibraryJavadocs)

                contextHelp(i18n("hybris.project.import.withExternalLibrarySources.tooltip"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.isExcludedFromScanning"))
                    .bindSelected(context.isExcludedFromScanning)
                contextHelp(i18n("hybris.project.import.isExcludedFromScanning.tooltip"))
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
                checkBox(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions"))
                    .bindSelected(context.settings.useFakeOutputPathForCustomExtensions)
                contextHelp(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions.tooltip"))
                    .customize(rightGaps)
            }

            row {
                checkBox(i18n("hybris.project.import.excludeTestSources"))
                    .bindSelected(context.settings.excludeTestSources)
            }

            row {
                checkBox(i18n("hybris.project.import.ignoreNonExistingSourceDirectories"))
                    .bindSelected(context.settings.ignoreNonExistingSourceDirectories)
            }

            row {
                checkBox(i18n("hybris.project.view.tree.hide.empty.middle.folders"))
                    .bindSelected(context.settings.hideEmptyMiddleFolders)
            }
        }

        group("Override") {
            row {
                checkBox("Platform source code:")
                    .bindSelected(context.sourceCodeDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.hybris.src.file"))
                    )
                )
                    .align(AlignX.FILL)
                    .resizableColumn()
                    .bindText(context.sourceCodeDirectory)
                    .enabledIf(context.sourceCodeDirectoryOverride)
                contextHelp("Select platform source code zip file or directory")
                    .customize(rightGaps)
            }.layout(RowLayout.PARENT_GRID)

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
    }
}
