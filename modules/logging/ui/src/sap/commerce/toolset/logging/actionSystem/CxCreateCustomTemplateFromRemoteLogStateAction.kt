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

package sap.commerce.toolset.logging.actionSystem

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnimatedIcon
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.logging.CxLogService
import sap.commerce.toolset.logging.CxRemoteLogAccess
import sap.commerce.toolset.logging.selectedNode
import sap.commerce.toolset.logging.ui.CxCustomLogTemplateDialog
import sap.commerce.toolset.logging.ui.tree.nodes.CxRemoteLogStateNode

class CxCreateCustomTemplateFromRemoteLogStateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return

        val node = e.selectedNode()
            ?.asSafely<CxRemoteLogStateNode>()
            ?: return

        val connectionName = node.connection.shortenConnectionName
        val customTemplate = CxRemoteLogAccess.getInstance(project).state(node.connection.uuid).get()
            ?.let { CxLogService.getInstance(project).createTemplateFromLoggers(connectionName, it) }
            ?.mutable()
            ?: return

        if (CxCustomLogTemplateDialog(project, customTemplate, "Create a Log Template | Remote '$connectionName'").showAndGet()) {
            CxLogService.getInstance(project).addTemplate(customTemplate.immutable())
        }

    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        val project = e.project ?: return
        val node = e.selectedNode()
            ?.asSafely<CxRemoteLogStateNode>()
            ?: return

        val loggerAccess = CxRemoteLogAccess.getInstance(project)

        val stateInitialized = CxRemoteLogAccess.getInstance(project).stateInitialized
        e.presentation.isEnabled = loggerAccess.ready
            && stateInitialized
            && CxRemoteLogAccess.getInstance(project).state(node.connection.uuid).get()?.isNotEmpty() ?: false

        if (!e.presentation.isVisible) return

        e.presentation.text = "Save as Template"
        e.presentation.icon = HybrisIcons.Log.Action.SAVE_AS_TEMPLATE
        e.presentation.disabledIcon = if (loggerAccess.ready) null else AnimatedIcon.Default.INSTANCE
    }
}