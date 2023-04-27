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
import com.intellij.idea.plugin.hybris.flexibleSearch.ui.FlexibleSearchEditorNotificationProvider
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

class HybrisProjectFlexibleSearchSettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.fxs.title"), "hybris.fxs.settings"
    ) {

        private val state = HybrisProjectSettingsComponent.getInstance(project).state.flexibleSearchSettings

        private lateinit var verifyCaseCheckBox: JCheckBox

        private val reservedWordsModel = EnumComboBoxModel(ReservedWordsCase::class.java)
        private val tableAliasSeparatorsModel = CollectionComboBoxModel(listOf(".", ":"))

        override fun apply() {
            super.apply()

            EditorNotificationProvider.EP_NAME.findExtension(FlexibleSearchEditorNotificationProvider::class.java, project)
                ?.let { EditorNotifications.getInstance(project).updateNotifications(it) }
        }

        override fun createPanel() = panel {
            group("Language") {
                row {
                    verifyCaseCheckBox =
                        checkBox("Verify case of the reserved words")
                            .bindSelected(state::verifyCaseForReservedWords)
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
                        .component
                }
            }
            group("Code Completion") {
                row {
                    checkBox("Automatically inject separator after table alias")
                        .bindSelected(state.completion::injectTableAliasSeparator)
                        .component
                }
                row {
                    checkBox("Automatically inject comma after result column")
                        .bindSelected(state.completion::injectCommaAfterResultColumn)
                        .component
                }
                row {
                    checkBox("Suggest table alias name after AS keyword")
                        .bindSelected(state.completion::suggestTableAliasNames)
                        .component
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
                        .label("Default separator")
                        .bindItem(state.completion::defaultTableAliasSeparator.toNullableProperty())
                        .component
                }
            }
        }
    }
}