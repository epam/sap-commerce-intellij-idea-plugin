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

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import sap.commerce.toolset.logging.selectedNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxBundledLogTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxCustomLogTemplateGroupNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxCustomLogTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CxRemoteLogStateNode

class CxLogTemplateContextMenuActionGroup : ActionGroup(), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val selectedNode = e?.selectedNode() ?: return emptyArray()
        val actionManager = ActionManager.getInstance()

        return when (selectedNode) {
            is CxRemoteLogStateNode -> arrayOf(actionManager.getAction("sap.cx.loggers.create.custom.template"))
            is CxBundledLogTemplateItemNode -> arrayOf(actionManager.getAction("sap.cx.loggers.bundled.template.item.actions"))
            is CxCustomLogTemplateItemNode -> arrayOf(actionManager.getAction("sap.cx.loggers.template.item.actions"))
            is CxCustomLogTemplateGroupNode -> arrayOf(actionManager.getAction("sap.cx.loggers.custom.addTemplate"))
            else -> emptyArray()
        }
    }

    override fun update(e: AnActionEvent) {
        val selectedNode = e.selectedNode()

        e.presentation.isEnabledAndVisible = selectedNode is CxBundledLogTemplateItemNode
            || selectedNode is CxCustomLogTemplateGroupNode
            || selectedNode is CxCustomLogTemplateItemNode
            || selectedNode is CxRemoteLogStateNode
    }
}
