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

package sap.commerce.toolset.flexibleSearch.options

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.layout.selected
import sap.commerce.toolset.flexibleSearch.editor.FxSReservedWordsCaseEditorNotificationProvider
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.settings.state.ReservedWordsCase
import sap.commerce.toolset.settings.yDeveloperSettings
import javax.swing.JCheckBox

class FlexibleSearchProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = project.isHybrisProject
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        i18n("hybris.settings.project.fxs.title"), "hybris.fxs.settings"
    ) {

        private val developerSettings = project.yDeveloperSettings
        private val mutableSettings = developerSettings.flexibleSearchSettings.mutable()

        private lateinit var verifyCaseCheckBox: JCheckBox

        private val reservedWordsModel = EnumComboBoxModel(ReservedWordsCase::class.java)
        private val tableAliasSeparatorsModel = CollectionComboBoxModel(listOf(".", ":"))
        private lateinit var documentationEnableCheckBox: JCheckBox

        override fun createPanel() = panel {
            group("Language") {
                row {
                    checkBox("Resolve non-aliased [y] column by root aliased Type in the where clause")
                        .bindSelected(mutableSettings::fallbackToTableNameIfNoAliasProvided)
                        .comment("When table has alias, [y] column can be resolved without corresponding alias. Such fallback will target only first Type specified in the where clause.")
                }
                row {
                    checkBox("Verify used table alias separator")
                        .bindSelected(mutableSettings::verifyUsedTableAliasSeparator)
                        .comment("Usage of the default table alias separator will be verified when the file is being opened for the first time")
                }
                row {
                    verifyCaseCheckBox =
                        checkBox("Verify case of the reserved words")
                            .bindSelected(mutableSettings::verifyCaseForReservedWords)
                            .comment("Case will be verified when the file is being opened for the first time")
                            .component
                }
                row {
                    comboBox(
                        reservedWordsModel,
                        renderer = SimpleListCellRenderer.create("?") { i18n("hybris.fxs.notification.provider.keywords.case.$it") }
                    )
                        .label("Default case for reserved words")
                        .bindItem(mutableSettings::defaultCaseForReservedWords.toNullableProperty())
                        .enabledIf(verifyCaseCheckBox.selected)
                }.rowComment("Existing case-related notifications will be closed for all related editors.<br>Verification of the case will be re-triggered on the next re-opening of the file")

            }
            group("Code Completion") {
                row {
                    checkBox("Automatically inject separator after table alias")
                        .bindSelected(mutableSettings.completion::injectTableAliasSeparator)
                }
                row {
                    checkBox("Automatically inject comma after expression")
                        .bindSelected(mutableSettings.completion::injectCommaAfterExpression)
                }
                row {
                    checkBox("Automatically inject space after keywords")
                        .bindSelected(mutableSettings.completion::injectSpaceAfterKeywords)
                }
                row {
                    checkBox("Suggest table alias name after AS keyword")
                        .bindSelected(mutableSettings.completion::suggestTableAliasNames)
                }
                row {
                    comboBox(
                        tableAliasSeparatorsModel,
                        renderer = SimpleListCellRenderer.create("?") {
                            when (it) {
                                "." -> i18n("hybris.settings.project.fxs.code.completion.separator.dot")
                                ":" -> i18n("hybris.settings.project.fxs.code.completion.separator.colon")
                                else -> it
                            }
                        }
                    )
                        .label("Default [y] separator")
                        .bindItem(mutableSettings.completion::defaultTableAliasSeparator.toNullableProperty())
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
                            When enabled short description of the type will be shown on-hover as a tooltip for type used in the <code>FROM {}</code> clause.
                        """.trimIndent()
                        )
                        .bindSelected(mutableSettings.documentation::showTypeDocumentation)
                        .enabledIf(documentationEnableCheckBox.selected)
                }
            }
        }

        override fun apply() {
            super.apply()

            developerSettings.flexibleSearchSettings = mutableSettings.immutable()

            EditorNotificationProvider.EP_NAME.findExtension(FxSReservedWordsCaseEditorNotificationProvider::class.java, project)
                ?.let { EditorNotifications.getInstance(project).updateAllNotifications() }
        }
    }
}