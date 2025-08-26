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

package sap.commerce.toolset.impex.options

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.yDeveloperSettings
import javax.swing.JCheckBox

class ImpExProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.impex.title"), "hybris.impex.settings"
    ) {

        private val developerSettings = project.yDeveloperSettings
        private val mutableSettings = developerSettings.impexSettings.mutable()
        private var originalGroupLocalizedFiles = mutableSettings.groupLocalizedFiles

        private lateinit var foldingEnableCheckBox: JCheckBox
        private lateinit var documentationEnableCheckBox: JCheckBox

        override fun createPanel() = panel {
            row {
                checkBox("Group localized ImpEx files")
                    .bindSelected(mutableSettings::groupLocalizedFiles)
            }

            group("Data Edit Mode") {
                row {
                    checkBox("First row is header")
                        .bindSelected(mutableSettings.editMode::firstRowIsHeader)
                }
                row {
                    checkBox("Trim whitespace")
                        .bindSelected(mutableSettings.editMode::trimWhitespace)
                }
            }.rowComment("This functionality relies and expects that 'intellij.grid.plugin' is available and enabled.")

            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(mutableSettings.folding::enabled)
                        .component
                }
                row {
                    checkBox("Use smart folding")
                        .bindSelected(mutableSettings.folding::useSmartFolding)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
                row {
                    checkBox("Fold macro usages in the parameters")
                        .bindSelected(mutableSettings.folding::foldMacroInParameters)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
            }

            group("Code Completion") {
                row {
                    checkBox("Show inline type for reference header parameter")
                        .comment(
                            """
                            When enabled, parameter Type and all its extends will be available as suggestions.<br>
                            Sample: <code>principal(<strong>Principal.</strong>uid)</code>
                            """.trimIndent()
                        )
                        .bindSelected(mutableSettings.completion::showInlineTypes)
                }
                row {
                    checkBox("Automatically add '.' char after inline type")
                        .comment(
                            """
                            When enabled and '.' char is not present, it will be injected automatically
                            """.trimIndent()
                        )
                        .bindSelected(mutableSettings.completion::addCommaAfterInlineType)
                }
                row {
                    checkBox("Automatically add '=' char after type and attribute modifier")
                        .comment(
                            """
                            When enabled and '=' char is not present, it will be injected automatically.<br>
                            In addition to that, code completion will be automatically triggered for modifier values.
                            """.trimIndent()
                        )
                        .bindSelected(mutableSettings.completion::addEqualsAfterModifier)
                }
            }
            group("Documentation") {
                row {
                    documentationEnableCheckBox = checkBox("Enable documentation")
                        .bindSelected(mutableSettings.documentation::enabled)
                        .component
                }
                row {
                    checkBox("Show documentation for type")
                        .comment(
                            """
                            When enabled short description of the type will be shown on-hover as a tooltip for type in the header or sub-type in the value line.
                        """.trimIndent()
                        )
                        .bindSelected(mutableSettings.documentation::showTypeDocumentation)
                        .enabledIf(documentationEnableCheckBox.selected)
                }
                row {
                    checkBox("Show documentation for modifier")
                        .comment(
                            """
                            When enabled short description of the modifier will be shown on-hover as a tooltip for type or attribute modifier in the header.
                        """.trimIndent()
                        )
                        .bindSelected(mutableSettings.documentation::showModifierDocumentation)
                        .enabledIf(documentationEnableCheckBox.selected)
                }
            }
        }

        override fun apply() {
            super.apply()

            developerSettings.impexSettings = mutableSettings.immutable()

            if (mutableSettings.groupLocalizedFiles != originalGroupLocalizedFiles) {
                // TODO: do we need this?
                originalGroupLocalizedFiles = mutableSettings.groupLocalizedFiles

                ProjectView.getInstance(project).refresh()
            }
        }
    }
}