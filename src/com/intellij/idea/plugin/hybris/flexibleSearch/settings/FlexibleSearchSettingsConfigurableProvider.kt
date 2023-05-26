/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.settings

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.flexibleSearch.ui.FxSReservedWordsCaseEditorNotificationProvider
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.ReservedWordsCase
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class FlexibleSearchSettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.fxs.title"), "hybris.fxs.settings"
    ) {

        private val state = HybrisProjectSettingsComponent.getInstance(project).state.flexibleSearchSettings

        private lateinit var verifyCaseCheckBox: JCheckBox

        private val reservedWordsModel = EnumComboBoxModel(ReservedWordsCase::class.java)
        private val tableAliasSeparatorsModel = CollectionComboBoxModel(listOf(".", ":"))
        private lateinit var foldingEnableCheckBox: JCheckBox

        override fun apply() {
            super.apply()

            EditorNotificationProvider.EP_NAME.findExtension(FxSReservedWordsCaseEditorNotificationProvider::class.java, project)
                ?.let { EditorNotifications.getInstance(project).updateNotifications(it) }
        }

        override fun createPanel() = panel {
            group("Language") {
                row {
                    checkBox("Resolve non-aliased [y] column by root aliased Type in the where clause")
                        .bindSelected(state::fallbackToTableNameIfNoAliasProvided)
                        .comment("When table has alias, [y] column can be resolved without corresponding alias. Such fallback will target only first Type specified in the where clause.")
                }
                row {
                    checkBox("Verify used table alias separator")
                        .bindSelected(state::verifyUsedTableAliasSeparator)
                        .comment("Usage of the default table alias separator will be verified when the file is being opened for the first time")
                }
                row {
                    verifyCaseCheckBox =
                        checkBox("Verify case of the reserved words")
                            .bindSelected(state::verifyCaseForReservedWords)
                            .comment("Case will be verified when the file is being opened for the first time")
                            .component
                }
                row {
                    comboBox(
                        reservedWordsModel,
                        renderer = SimpleListCellRenderer.create("?") { message("hybris.fxs.notification.provider.keywords.case.$it") }
                    )
                        .label("Default case for reserved words")
                        .bindItem(state::defaultCaseForReservedWords.toNullableProperty())
                        .enabledIf(verifyCaseCheckBox.selected)
                }.rowComment("Existing case-related notifications will be closed for all related editors.<br>Verification of the case will be re-triggered on the next re-opening of the file")

            }
            group("Code Completion") {
                row {
                    checkBox("Automatically inject separator after table alias")
                        .bindSelected(state.completion::injectTableAliasSeparator)
                }
                row {
                    checkBox("Automatically inject comma after expression")
                        .bindSelected(state.completion::injectCommaAfterExpression)
                }
                row {
                    checkBox("Automatically inject space after keywords")
                        .bindSelected(state.completion::injectSpaceAfterKeywords)
                }
                row {
                    checkBox("Suggest table alias name after AS keyword")
                        .bindSelected(state.completion::suggestTableAliasNames)
                }
                row {
                    comboBox(
                        tableAliasSeparatorsModel,
                        renderer = SimpleListCellRenderer.create("?") {
                            when (it) {
                                "." -> message("hybris.settings.project.fxs.code.completion.separator.dot")
                                ":" -> message("hybris.settings.project.fxs.code.completion.separator.colon")
                                else -> it
                            }
                        }
                    )
                        .label("Default [y] separator")
                        .bindItem(state.completion::defaultTableAliasSeparator.toNullableProperty())
                }
            }
            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(state.folding::enabled)
                        .component
                }
                row {
                    checkBox("Show table alias for folded [y] attributes")
                        .comment("If checked attribute <code>{alias.name[en]}</code> will be represented as <code>alias.name</code>")
                        .bindSelected(state.folding::showSelectedTableNameForYColumn)
                        .enabledIf(foldingEnableCheckBox.selected)
                }
                row {
                    checkBox("Show language for folded [y] attribute")
                        .bindSelected(state.folding::showLanguageForYColumn)
                        .enabledIf(foldingEnableCheckBox.selected)
                        .comment("If checked localized attribute <code>{name[en]}</code> will be represented as <code>name:en</code>")
                }
            }
        }
    }
}