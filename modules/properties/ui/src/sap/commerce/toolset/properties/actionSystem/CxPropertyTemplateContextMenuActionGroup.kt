/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.actionSystem

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.properties.selectedNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateGroupNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxCustomPropertyTemplateItemNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxRemotePropertyStateNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceCodeNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceExtensionGroupNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceExtensionNode
import sap.commerce.toolset.properties.ui.tree.nodes.CxSourceProjectNode

class CxPropertyTemplateContextMenuActionGroup : ActionGroup() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val selectedNode = e?.selectedNode() ?: return emptyArray()
        val manager = ActionManager.getInstance()

        return when (selectedNode) {
            is CxSourceProjectNode,
            is CxSourceExtensionNode -> arrayOf(
                manager.getAction("sap.cx.properties.fetch"),
                manager.getAction("sap.cx.properties.source.mode"),
            )

            is CxSourceCodeNode,
            is CxSourceExtensionGroupNode -> arrayOf(manager.getAction("sap.cx.properties.source.mode"))

            is CxRemotePropertyStateNode -> arrayOf(
                manager.getAction("sap.cx.properties.fetch"),
                manager.getAction("sap.cx.properties.custom.createTemplate"),
            )
            is CxCustomPropertyTemplateItemNode -> arrayOf(manager.getAction("sap.cx.properties.template.item.actions"))
            is CxCustomPropertyTemplateGroupNode -> arrayOf(manager.getAction("sap.cx.properties.custom.addTemplate"))
            else -> emptyArray()
        }
    }

    override fun update(e: AnActionEvent) {
        val selectedNode = e.selectedNode()
        e.presentation.isEnabledAndVisible = e.isHybrisProject && (
            selectedNode is CxRemotePropertyStateNode
                || selectedNode is CxCustomPropertyTemplateGroupNode
                || selectedNode is CxCustomPropertyTemplateItemNode
                || selectedNode is CxSourceCodeNode
                || selectedNode is CxSourceProjectNode
                || selectedNode is CxSourceExtensionGroupNode
                || selectedNode is CxSourceExtensionNode
            )
    }
}
