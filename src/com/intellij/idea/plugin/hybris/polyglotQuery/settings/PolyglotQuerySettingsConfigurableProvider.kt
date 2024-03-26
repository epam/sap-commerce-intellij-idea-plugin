/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.polyglotQuery.ui.PolyglotQueryEditorNotificationProvider
import com.intellij.idea.plugin.hybris.settings.ReservedWordsCase
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.layout.selected
import javax.swing.JCheckBox

class PolyglotQuerySettingsConfigurableProvider(val project: Project) : ConfigurableProvider() {

    override fun canCreateConfigurable() = ProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun createConfigurable() = SettingsConfigurable(project)

    class SettingsConfigurable(private val project: Project) : BoundSearchableConfigurable(
        message("hybris.settings.project.pgq.title"), "hybris.pgq.settings"
    ) {

        private val state = DeveloperSettingsComponent.getInstance(project).state.polyglotQuerySettings

        private lateinit var verifyCaseCheckBox: JCheckBox
        private lateinit var foldingEnableCheckBox: JCheckBox

        private val reservedWordsModel = EnumComboBoxModel(ReservedWordsCase::class.java)

        override fun apply() {
            super.apply()

            EditorNotificationProvider.EP_NAME.findExtension(PolyglotQueryEditorNotificationProvider::class.java, project)
                ?.let { EditorNotifications.getInstance(project).updateNotifications(it) }
        }

        override fun createPanel() = panel {
            group("Language") {
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
                        renderer = SimpleListCellRenderer.create("?") { message("hybris.pgq.notification.provider.keywords.case.$it") }
                    )
                        .label("Default case for reserved words")
                        .bindItem(state::defaultCaseForReservedWords.toNullableProperty())
                        .enabledIf(verifyCaseCheckBox.selected)
                }.rowComment("Existing case-related notifications will be closed for all related editors.<br>Verification of the case will be re-triggered on the next re-opening of the file")
            }
            group("Code Folding") {
                row {
                    foldingEnableCheckBox = checkBox("Enable code folding")
                        .bindSelected(state.folding::enabled)
                        .component
                }
                row {
                    checkBox("Show language for folded attribute")
                        .bindSelected(state.folding::showLanguage)
                        .enabledIf(foldingEnableCheckBox.selected)
                        .comment("If checked localized attribute <code>{name[en]}</code> will be represented as <code>name:en</code>")
                }
            }
        }
    }
}