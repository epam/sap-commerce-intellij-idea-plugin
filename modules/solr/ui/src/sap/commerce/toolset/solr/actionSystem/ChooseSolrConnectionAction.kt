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

package sap.commerce.toolset.solr.actionSystem

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import kotlinx.html.div
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.console.HybrisConsoleService
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import sap.commerce.toolset.ui.ActionButtonWithTextAndDescription

class ChooseSolrConnectionAction : DefaultActionGroup() {

    init {
        templatePresentation.icon = HybrisIcons.Y.REMOTE
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
        templatePresentation.putClientProperty(ActionUtil.COMPONENT_PROVIDER, ActionButtonWithTextAndDescription(this))
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
        val project = e?.project ?: return emptyArray()
        val actions = super.getChildren(e)

        val execService = SolrExecConnectionService.getInstance(project)
        val activeConnection = execService.activeConnection
        val connectionActions = execService.connections
            .map {
                if (it == activeConnection) object : SolrConnectionAction(it.presentationName, HybrisIcons.Y.REMOTE) {
                    override fun actionPerformed(e: AnActionEvent) {
                        execService.activeConnection = it
                    }
                }
                else object : SolrConnectionAction(it.presentationName, HybrisIcons.Y.REMOTE_GREEN) {
                    override fun actionPerformed(e: AnActionEvent) {
                        execService.activeConnection = it
                    }
                }
            }

        return actions +
            Separator.create("Available Connections") +
            connectionActions
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val activeConnection = SolrExecConnectionService.getInstance(project).activeConnection

        e.presentation.isEnabledAndVisible = HybrisConsoleService.getInstance(project).getActiveConsole()
            ?.activeConnection()
            ?.let { it is SolrConnectionSettingsState }
            ?: false

        e.presentation.text = null
        e.presentation.putClientProperty(ActionUtil.HIDE_DROPDOWN_ICON, true)
        e.presentation.description = createHTML().div {
            p { +"Switch active connection" }
            activeConnection.generatedURL
                .let { p { +it } }
        }
    }
}