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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.flexibleSearchExecutionSettings
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionSettings
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.intellij.util.application
import com.intellij.util.ui.JBUI

class FlexibleSearchExecutionSettingsAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val settings = e.flexibleSearchExecutionSettings() ?: return

        e.presentation.text = """
            Execution Context Settings<br>
              rows:   ${settings.maxCount}<br>
              user:   ${settings.user}<br>
              locale: ${settings.locale}<br>
              tenant: ${settings.dataSource}<br>
        """.trimIndent()
        e.presentation.icon = HybrisIcons.Connection.CONTEXT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val inputEvent = e.inputEvent ?: return
        val project = e.project ?: return
        val settings = e.flexibleSearchExecutionSettings() ?: return
        val dataSources = application.runReadAction<List<String>> {
            PropertyService.getInstance(project)
                ?.findProperty("installed.tenants")
                ?.split(",")
                ?: listOf()
        }.toSortedSet().apply { add(FlexibleSearchExecutionSettings.DEFAULT_DATA_SOURCE) }

        val settingsPanel = panel {
            row {
                spinner(0..1000000)
                    .align(AlignX.FILL)
                    .label("Rows:")
                    .bindIntValue({ settings.maxCount }, { value -> settings.maxCount = value })
            }.layout(RowLayout.PARENT_GRID)

            row {
                textField()
                    .align(AlignX.FILL)
                    .label("User:")
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
            }

        JBPopupFactory.getInstance().createComponentPopupBuilder(settingsPanel, null)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(true)
            .createPopup()
            .also {
                it.addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) = settingsPanel.apply()
                })
                it.showUnderneathOf(inputEvent.component)
            }
    }

}
