/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.impex.ui.components.ImpExQuoteStringExclusionsListPanel
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.state.ImpExQuoteStringExclusion
import sap.commerce.toolset.settings.yDeveloperSettings
import sap.commerce.toolset.ui.previewEditor
import javax.swing.JCheckBox

class ImpExProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.impex.title"), "hybris.impex.settings"
    ) {

        private val developerSettings = project.yDeveloperSettings
        private val mutable = developerSettings.impexSettings.mutable()
        private var originalGroupLocalizedFiles = mutable.groupLocalizedFiles
        private val originalQuoteStringExclusions = mutableListOf<ImpExQuoteStringExclusion>()
        private val wrapStringExclusionsListPanel = ImpExQuoteStringExclusionsListPanel(project)
        private lateinit var documentationEnableCheckBox: JCheckBox
        private val _ui = panel {
            row {
                checkBox("Group localized ImpEx files")
                    .bindSelected(mutable::groupLocalizedFiles)
            }

            group("Data Edit Mode") {
                row {
                    checkBox("First row is header")
                        .bindSelected(mutable.editMode::firstRowIsHeader)
                }
                row {
                    checkBox("Trim whitespace")
                        .bindSelected(mutable.editMode::trimWhitespace)
                }
            }.rowComment("This functionality relies and expects that 'intellij.grid.plugin' is available and enabled.")

            group("Code Completion") {
                row {
                    checkBox("Show inline type for reference header parameter")
                        .comment(
                            """
                                    When enabled, parameter Type and all its extends will be available as suggestions.<br>
                                    Sample: <code>principal(<strong>Principal.</strong>uid)</code>
                                    """.trimIndent()
                        )
                        .bindSelected(mutable.completion::showInlineTypes)
                }
                row {
                    checkBox("Automatically add '.' char after inline type")
                        .comment(
                            """
                                    When enabled and '.' char is not present, it will be injected automatically
                                    """.trimIndent()
                        )
                        .bindSelected(mutable.completion::addCommaAfterInlineType)
                }
                row {
                    checkBox("Automatically add '=' char after type and attribute modifier")
                        .comment(
                            """
                                    When enabled and '=' char is not present, it will be injected automatically.<br>
                                    In addition to that, code completion will be automatically triggered for modifier values.
                                    """.trimIndent()
                        )
                        .bindSelected(mutable.completion::addEqualsAfterModifier)
                }
            }
            group("Documentation") {
                row {
                    documentationEnableCheckBox = checkBox("Enable documentation")
                        .bindSelected(mutable.documentation::enabled)
                        .component
                }
                row {
                    checkBox("Show documentation for type")
                        .comment(
                            """
                                    When enabled short description of the type will be shown on-hover as a tooltip for type in the header or sub-type in the value line.
                                """.trimIndent()
                        )
                        .bindSelected(mutable.documentation::showTypeDocumentation)
                        .enabledIf(documentationEnableCheckBox.selected)
                }
                row {
                    checkBox("Show documentation for modifier")
                        .comment(
                            """
                                    When enabled short description of the modifier will be shown on-hover as a tooltip for type or attribute modifier in the header.
                                """.trimIndent()
                        )
                        .bindSelected(mutable.documentation::showModifierDocumentation)
                        .enabledIf(documentationEnableCheckBox.selected)
                }
            }

            group("Wrap Value Strings", false) {
                row {
                    val text = """
                        UPDATE Title; code[unique = true]; name[lang = en]
                        # Before inspection rule
                        ; test ; Not quoted name
                        # After inspection rule
                        ; test ; "Not quoted name"
                    """.trimIndent()
                    previewEditor(project, ImpExFileType, text)
                        .comment(
                            """
                                    It is preferable to wrap any values in quotes for non-unique <code>java.lang.String</code> and <code>localized:java.lang.String</code> item attributes.<br>
                                    This setting extends <code>[y] Quote value strings</code> inspection rule with exclusion of specific item attributes.<br>
                                    New exclusion rules can be managed here or added from an ImpEx file via quick-fix on a specific attribute.
                            """.trimIndent()
                        )
                        .align(AlignX.FILL)
                }

                row {
                    checkBox("Do not quote string values matching the Regex:")
                        .bindSelected(mutable::quoteStringWhitelist)

                    textField()
                        .bindText(mutable::quoteStringWhitelistPattern)
                        .addValidationRule("Please enter a valid Regex.") {
                            runCatching { Regex(it.text) }.isFailure
                        }
                }

                row {
                    label("Type & attribute specific exclusion rules:")
                }
                row {
                    cell(wrapStringExclusionsListPanel)
                        .onIsModified { originalQuoteStringExclusions != wrapStringExclusionsListPanel.data }
                        .onApply {
                            mutable.quoteStringExclusions.apply {
                                clear()
                                addAll(wrapStringExclusionsListPanel.data)
                            }
                            originalQuoteStringExclusions.apply {
                                clear()
                                addAll(wrapStringExclusionsListPanel.data)
                            }
                        }
                        .onReset {
                            originalQuoteStringExclusions.apply {
                                clear()
                                mutable.quoteStringExclusions.forEach { add(it.copy()) }
                            }
                            wrapStringExclusionsListPanel.data = originalQuoteStringExclusions
                                .map { it.copy() }
                        }
                        .align(AlignX.FILL)
                }
            }
        }


        override fun createPanel() = _ui

        override fun apply() {
            if (_ui.validateAll().none { it.component?.isVisible ?: false }) {
                super.apply()

                developerSettings.impexSettings = mutable.immutable()

                if (mutable.groupLocalizedFiles != originalGroupLocalizedFiles) {
                    // TODO: do we need this?
                    originalGroupLocalizedFiles = mutable.groupLocalizedFiles

                    ProjectView.getInstance(project).refresh()
                }
            }
        }
    }
}