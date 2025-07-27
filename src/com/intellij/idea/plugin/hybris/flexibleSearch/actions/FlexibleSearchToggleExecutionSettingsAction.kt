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
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionContextSettings
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.intellij.util.application
import com.intellij.util.ui.JBUI

class FlexibleSearchToggleExecutionSettingsAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        e.presentation.text = "Execution Context Settings"
        e.presentation.icon = HybrisIcons.Connection.CONTEXT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val inputEvent = e.inputEvent ?: return
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val user = RemoteConnectionService.getInstance(project)
            .getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
            .username
        val tenants = application.runReadAction<List<String>> {
            PropertyService.getInstance(project)
                ?.findProperty("installed.tenants")
                ?.split(",")
                ?: listOf()
        }.toMutableSet().apply { add("admin") }

        val executionSettings = editor.getOrCreateUserDataUnsafe(HybrisConstants.KEY_FXS_EXECUTION_SETTINGS) {
            FlexibleSearchExecutionContextSettings(
                user = user,
            )
        }

        val settingsPanel = panel {
            row {
                spinner(0..1000000)
                    .align(AlignX.FILL)
                    .label("Rows:")
                    .bindIntValue({ executionSettings.maxCount }, { value -> executionSettings.maxCount = value })
            }.layout(RowLayout.PARENT_GRID)

            row {
                textField()
                    .align(AlignX.FILL)
                    .label("User:")
                    .bindText({ executionSettings.user }, { value -> executionSettings.user = value })
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
                    .bindItem({ executionSettings.locale }, { value -> executionSettings.locale = value ?: "en" })
            }.layout(RowLayout.PARENT_GRID)

            row {
                comboBox(
                    tenants,
                    renderer = SimpleListCellRenderer.create("?") { it }
                )
                    .label("Tenant:")
                    .align(AlignX.FILL)
                    .bindItem({ executionSettings.dataSource }, { value -> executionSettings.dataSource = value ?: "master" })
            }.layout(RowLayout.PARENT_GRID)
        }
            .apply {
                border = JBUI.Borders.empty(8, 16)
            }

        JBPopupFactory.getInstance().createComponentPopupBuilder(settingsPanel, null)
            .createPopup()
            .also {
                it.addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) = settingsPanel.apply()
                })
                it.showUnderneathOf(inputEvent.component)
            }
    }

}
