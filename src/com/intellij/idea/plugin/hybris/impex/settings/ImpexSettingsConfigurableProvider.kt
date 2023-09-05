/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.settings

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class ImpexSettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.impex.title"), "hybris.impex.settings"
    ) {

        private val state = HybrisProjectSettingsComponent.getInstance(project).state.impexSettings

        private lateinit var foldingEnableCheckBox: JCheckBox
        private lateinit var documentationEnableCheckBox: JCheckBox

        override fun createPanel() = panel {
            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(state.folding::enabled)
                        .component
                }
                group {
                    row {
                        checkBox("Use smart folding")
                            .bindSelected(state.folding::useSmartFolding)
                            .enabledIf(foldingEnableCheckBox.selected)
                    }
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
                        .bindSelected(state.completion::showInlineTypes)
                }
                row {
                    checkBox("Automatically add '.' char after inline type")
                        .comment(
                            """
                            When enabled and '.' char is not present, it will be injected automatically
                            """.trimIndent()
                        )
                        .bindSelected(state.completion::addCommaAfterInlineType)
                }
                row {
                    checkBox("Automatically add '=' char after type and attribute modifier")
                        .comment(
                            """
                            When enabled and '=' char is not present, it will be injected automatically.<br>
                            In addition to that, code completion will be automatically triggered for modifier values.
                            """.trimIndent()
                        )
                        .bindSelected(state.completion::addEqualsAfterModifier)
                }
            }
            group("Documentation") {
                row {
                    documentationEnableCheckBox = checkBox("Enable documentation")
                        .bindSelected(state.documentation::enabled)
                        .component
                }
                row {
                    checkBox("Show documentation for type")
                        .comment(
                            """
                            When enabled short description of the type will be shown on-hover as a tooltip for type in the header or sub-type in the value line.
                        """.trimIndent()
                        )
                        .bindSelected(state.documentation::showTypeDocumentation)
                        .enabledIf(documentationEnableCheckBox.selected)
                }
                row {
                    checkBox("Show documentation for modifier")
                        .comment(
                            """
                            When enabled short description of the modifier will be shown on-hover as a tooltip for type or attribute modifier in the header.
                        """.trimIndent()
                        )
                        .bindSelected(state.documentation::showModifierDocumentation)
                        .enabledIf(documentationEnableCheckBox.selected)
                }
            }
        }
    }
}