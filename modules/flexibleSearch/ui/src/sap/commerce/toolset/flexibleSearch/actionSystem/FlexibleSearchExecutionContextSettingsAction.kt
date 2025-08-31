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
package sap.commerce.toolset.flexibleSearch.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.*
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchExecutionContextSettings
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.hac.actionSystem.ExecutionContextSettingsAction
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.ui.GroupedComboBoxItem
import sap.commerce.toolset.ui.GroupedComboBoxModel
import sap.commerce.toolset.ui.GroupedComboBoxRenderer
import javax.swing.LayoutFocusTraversalPolicy

class FlexibleSearchExecutionContextSettingsAction : ExecutionContextSettingsAction<FlexibleSearchExecContext.Settings.Mutable>() {

    override fun previewSettings(e: AnActionEvent, project: Project): String = e.flexibleSearchExecutionContextSettings { FlexibleSearchExecContext.defaultSettings() }
        .let {
            """<pre>
 · rows:    ${it.maxCount}
 · user:    ${it.user}
 · locale:  ${it.locale}
 · tenant:  ${it.dataSource}
 · timeout: ${it.timeout} ms</pre>
                """.trimIndent()
        }

    override fun settings(e: AnActionEvent, project: Project): FlexibleSearchExecContext.Settings.Mutable {
        val settings = e.flexibleSearchExecutionContextSettings {
            val connectionSettings = HacExecConnectionService.getInstance(project).activeConnection
            FlexibleSearchExecContext.defaultSettings(connectionSettings)
        }

        return settings.mutable()
    }

    override fun applySettings(editor: Editor, settings: FlexibleSearchExecContext.Settings.Mutable) {
        editor.putUserData(FlexibleSearchExecContext.KEY_EXECUTION_SETTINGS, settings.immutable())
    }

    override fun settingsPanel(e: AnActionEvent, project: Project, settings: FlexibleSearchExecContext.Settings.Mutable): DialogPanel {
        val dataSources = application.runReadAction<Collection<String>> {
            PropertyService.getInstance(project)
                .findProperty(HybrisConstants.PROPERTY_INSTALLED_TENANTS)
                ?.split(",")
                ?: emptyList()
        }
            .toSortedSet()
            .apply {
                val connectionSettings = HacExecConnectionService.getInstance(project).activeConnection

                add(FlexibleSearchExecContext.defaultSettings(connectionSettings).dataSource)
            }

        return panel {
            row {
                textField()
                    .align(AlignX.FILL)
                    .label("Rows:")
                    .validationOnInput {
                        if (it.text.toIntOrNull() == null) error(UIBundle.message("please.enter.a.number.from.0.to.1", 1, Int.MAX_VALUE))
                        else null
                    }
                    .focused()
                    .bindIntText({ settings.maxCount }, { value -> settings.maxCount = value })
            }.layout(RowLayout.PARENT_GRID)

            row {
                textField()
                    .align(AlignX.FILL)
                    .label("User:")
                    .validationOnInput {
                        if (it.text.isBlank()) error("Please enter a user name")
                        else null
                    }
                    .bindText({ settings.user }, { value -> settings.user = value })
            }.layout(RowLayout.PARENT_GRID)

            row {
                comboBox(
                    model = GroupedComboBoxModel(computeLocales(project)),
                    renderer = GroupedComboBoxRenderer()
                )
                    .label("Locale:")
                    .align(AlignX.FILL)
                    .bindItem({ GroupedComboBoxItem.Option(settings.locale) }, { value -> settings.locale = value.asSafely<GroupedComboBoxItem.Option>()?.value ?: "en" })
            }.layout(RowLayout.PARENT_GRID)

            row {
                comboBox(
                    dataSources,
                    renderer = SimpleListCellRenderer.create("?") { it }
                )
                    .label("Tenant:")
                    .align(AlignX.FILL)
                    .bindItem({ settings.dataSource }, { value -> settings.dataSource = value ?: "master" })
            }.layout(RowLayout.PARENT_GRID)

            row {
                textField()
                    .align(AlignX.FILL)
                    .label("Timeout (ms):")
                    .validationOnInput {
                        if (it.text.toIntOrNull() == null) error(UIBundle.message("please.enter.a.number.from.0.to.1", 1, Int.MAX_VALUE))
                        else null
                    }
                    .bindIntText(settings::timeout)
            }.layout(RowLayout.PARENT_GRID)
        }
            .apply {
                border = JBUI.Borders.empty(8, 16)
                focusTraversalPolicy = LayoutFocusTraversalPolicy()
                isFocusCycleRoot = true
            }
    }

    private fun computeLocales(project: Project): List<GroupedComboBoxItem> {
        val langPacks = application.runReadAction<Collection<String>> {
            PropertyService.getInstance(project).getLanguages()
        }
            .map { GroupedComboBoxItem.Option(it) }
        val locales = HybrisConstants.Locales.LOCALES_CODES
            .map { GroupedComboBoxItem.Option(it) }

        return listOf(
            listOf(GroupedComboBoxItem.Group("Language Packs")),
            langPacks,
            listOf(GroupedComboBoxItem.Group("All Locales")),
            locales
        )
            .flatten()
    }
}
