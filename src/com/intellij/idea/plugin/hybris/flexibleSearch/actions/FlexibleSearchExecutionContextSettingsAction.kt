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
package com.intellij.idea.plugin.hybris.flexibleSearch.actions

import com.intellij.idea.plugin.hybris.actions.ExecutionContextSettingsAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.flexibleSearchExecutionSettings
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionContextSettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.*
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import javax.swing.LayoutFocusTraversalPolicy

class FlexibleSearchExecutionContextSettingsAction : ExecutionContextSettingsAction<FlexibleSearchExecutionContextSettings>() {

    override fun previewSettings(e: AnActionEvent): String = e.flexibleSearchExecutionSettings(false)
        ?.let {
            """
                rows:   ${it.maxCount}<br>
                user:   ${it.user}<br>
                locale: ${it.locale}<br>
                tenant: ${it.dataSource}
                """.trimIndent()
        }
        ?: """
                rows:   ${FlexibleSearchExecutionContextSettings.DEFAULT_MAX_COUNT}<br>
                user:   from active connection<br>
                locale: ${FlexibleSearchExecutionContextSettings.DEFAULT_LOCALE}<br>
                tenant: ${FlexibleSearchExecutionContextSettings.DEFAULT_DATA_SOURCE}
                """.trimIndent()

    override fun settingsPanel(e: AnActionEvent): DialogPanel? {
        val project = e.project ?: return null
        val settings = e.flexibleSearchExecutionSettings() ?: return null

        val dataSources = application.runReadAction<List<String>> {
            PropertyService.getInstance(project)
                ?.findProperty("installed.tenants")
                ?.split(",")
                ?: listOf()
        }.toSortedSet().apply { add(FlexibleSearchExecutionContextSettings.DEFAULT_DATA_SOURCE) }
        return panel {
            row {
                textField()
                    .align(AlignX.FILL)
                    .label("Rows:")
                    .validationOnInput {
                        if (it.text.toIntOrNull() == null) error(UIBundle.message("please.enter.a.number.from.0.to.1", 1, Int.MAX_VALUE))
                        else null
                    }
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
                    .validationOnApply {
                        if (it.text.isBlank()) error("Please enter a user name")
                        else null
                    }
                    .bindText({ settings.user }, { value -> settings.user = value })
            }.layout(RowLayout.PARENT_GRID)

            row {
                comboBox(
                    HybrisConstants.Locales.LOCALES_CODES,
                    renderer = SimpleListCellRenderer.create("?") {
                        it
                    }
                )
                    .label("Locale:")
                    .align(AlignX.FILL)
                    .bindItem({ settings.locale }, { value -> settings.locale = value ?: "en" })
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
        }
            .apply {
                border = JBUI.Borders.empty(8, 16)
                focusTraversalPolicy = LayoutFocusTraversalPolicy()
                isFocusCycleRoot = true
            }
    }

}
