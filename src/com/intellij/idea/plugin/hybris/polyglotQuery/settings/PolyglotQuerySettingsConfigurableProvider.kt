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

package com.intellij.idea.plugin.hybris.polyglotQuery.settings

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.ReservedWordsCase
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class PolyglotQuerySettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.pgq.title"), "hybris.pgq.settings"
    ) {

        private val state = HybrisProjectSettingsComponent.getInstance(project).state.polyglotQuerySettings

        private lateinit var verifyCaseCheckBox: JCheckBox
        private lateinit var foldingEnableCheckBox: JCheckBox

        private val reservedWordsModel = EnumComboBoxModel(ReservedWordsCase::class.java)

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
                        renderer = SimpleListCellRenderer.create("?") { message("hybris.pgq.notification.provider.keywords.case.$it") }
                    )
                        .label("Default case for reserved words")
                        .bindItem(state::defaultCaseForReservedWords.toNullableProperty())
                        .enabledIf(verifyCaseCheckBox.selected)
                        .component
                }
            }
            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(state.folding::enabled)
                        .component
                }
                row {
                    checkBox("Include language in the folded attribute")
                        .bindSelected(state.folding::showLanguage)
                        .enabledIf(foldingEnableCheckBox.selected)
                        .component
                }
            }
        }
    }
}