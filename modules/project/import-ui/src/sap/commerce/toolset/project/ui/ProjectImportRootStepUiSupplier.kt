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
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.*
import org.intellij.images.fileTypes.impl.SvgFileType
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.ProjectImportRootContext
import sap.commerce.toolset.ui.CRUDListPanel

object ProjectImportRootStepUiSupplier {

    fun ui(context: ProjectImportRootContext) = panel {
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

            label("SAP CX version:")
                .align(AlignX.RIGHT)
                .gap(RightGap.SMALL)
            label("")
                .bindText(context.platformVersion)
                .bold()
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Custom project icon:")
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
        }.layout(RowLayout.PARENT_GRID)

        row {
            checkBox("Store IDEA module files in:")
                .bindSelected(context.moduleFilesStorage)
                .comment("If unchecked, .iml file will be stored in the root directory of the module.")

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
        }.layout(RowLayout.PARENT_GRID)

        collapsibleGroup("CCv2") {
            row {}.comment(
                """
                    These settings are non-project specific and shared across all Projects and IntelliJ IDEA instances.<br>
                """.trimIndent()
            )

            row {
                passwordField()
                    .comment(
                        """
                        Specify developer specific Token for CCv2 API, it will be stored in the OS specific secure storage under <strong>SAP CX CCv2 Token</strong> alias.<br>
                        Official documentation <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/b5d4d851cbd54469906a089bb8dd58d8.html">help.sap.com - Generating API Tokens</a>.
                    """.trimIndent()
                    )
                    .label("CCv2 token:")
                    .bindText(context.ccv2Token)
                    .align(AlignX.FILL)
            }
                .layout(RowLayout.PARENT_GRID)
        }
            .expanded = true

        collapsibleGroup("Scanning Settings") {
            row {
                checkBox("Scan for SAP CX modules even in external modules")
                    .comment("Eclipse, Gradle and Maven projects. (slower import/refresh)")
                    .bindSelected(context.importSettings.scanThroughExternalModule)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox("Include symbolic links for a project import")
                    .bindSelected(context.importSettings.followSymlink)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox("Directories excluded from the project scanning")
                    .comment("Specify directories related to the project root, use '/' separator for sub-directories.")
                    .bindSelected(context.isExcludedFromScanning)
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
            .expanded = true

        collapsibleGroup("Import Settings") {
            row {
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.hybris.distribution.directory"))
                    )
                )
                    .label("SAP CX directory:")
                    .bindText(context.platformDirectory)
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            row {
                textField()
                    .label("SAP CX javadoc url:")
                    .bindText(context.javadocUrl)
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox("SAP CX source code:")
                    .bindSelected(context.sourceCodeDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.hybris.src.file"))
                    )
                )
                    .align(AlignX.FILL)
                    .bindText(context.sourceCodeDirectory)
                    .enabledIf(context.sourceCodeDirectoryOverride)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions"))
                    .comment(i18n("hybris.project.import.useFakeOutputPathForCustomExtensions.tooltip"))
                    .bindSelected(context.importSettings.useFakeOutputPathForCustomExtensions)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.import.wizard.import.ootb.modules.read.only.label"))
                    .comment(i18n("hybris.import.wizard.import.ootb.modules.read.only.tooltip"))
                    .bindSelected(context.platformReadOnlyMode)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.project.import.excludeTestSources"))
                    .bindSelected(context.importSettings.excludeTestSources)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.project.import.ignoreNonExistingSourceDirectories"))
                    .bindSelected(context.importSettings.ignoreNonExistingSourceDirectories)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.project.import.withStandardProvidedSources"))
                    .comment(i18n("hybris.project.import.withStandardProvidedSources.tooltip"))
                    .bindSelected(context.importSettings.withStandardProvidedSources)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.project.import.withExternalLibrarySources"))
                    .comment(i18n("hybris.project.import.withExternalLibrarySources.tooltip"))
                    .bindSelected(context.importSettings.withExternalLibrarySources)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.project.import.withExternalLibraryJavadocs"))
                    .comment(i18n("hybris.project.import.withExternalLibraryJavadocs.tooltip"))
                    .bindSelected(context.importSettings.withExternalLibraryJavadocs)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox(i18n("hybris.project.import.importCustomAntBuildFiles"))
                    .comment(i18n("hybris.project.import.importCustomAntBuildFiles.tooltip"))
                    .bindSelected(context.importSettings.importCustomAntBuildFiles)
            }.layout(RowLayout.PARENT_GRID)
        }
            .expanded = true

        collapsibleGroup("Override Settings") {
            row {
                checkBox("Override custom directory:")
                    .comment("If your custom directory is in bin/ext-* directory or is outside the project root directory.")
                    .bindSelected(context.customDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.custom.extensions.directory"))
                    )
                )
                    .align(AlignX.FILL)
                    .bindText(context.customDirectory)
                    .enabledIf(context.customDirectoryOverride)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox("Override config directory:")
                    .comment(
                        """
                    The config directory that will be used for import.<br>
                    This is equivalent of environment parameter HYBRIS_CONFIG_DIR.
                    """.trimIndent()
                    )
                    .bindSelected(context.configDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.config.extensions.directory"))
                    )
                )
                    .align(AlignX.FILL)
                    .bindText(context.configDirectory)
                    .enabledIf(context.configDirectoryOverride)
            }.layout(RowLayout.PARENT_GRID)

            row {
                checkBox("Override DB driver directory:")
                    .comment("The DB driver directory that contains DB driver jar files (used to execute Integration tests from IDE).")
                    .bindSelected(context.dbDriverDirectoryOverride)
                cell(
                    textFieldWithBrowseButton(
                        null,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(i18n("hybris.import.label.select.dbdriver.extensions.directory"))
                    )
                )
                    .align(AlignX.FILL)
                    .bindText(context.dbDriverDirectory)
                    .enabledIf(context.dbDriverDirectoryOverride)
            }.layout(RowLayout.PARENT_GRID)
        }
            .expanded = true
    }
}