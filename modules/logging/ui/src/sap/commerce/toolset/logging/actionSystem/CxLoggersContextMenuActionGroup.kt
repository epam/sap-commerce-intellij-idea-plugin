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
import sap.commerce.toolset.logging.ui.tree.nodes.BundledLoggersTemplateItemNode
import sap.commerce.toolset.logging.ui.tree.nodes.CustomLoggersTemplateLoggersOptionsNode

class CxLoggersContextMenuActionGroup : ActionGroup(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val selectedNode = e?.selectedNode() ?: return emptyArray()
        return when (selectedNode) {
            is BundledLoggersTemplateItemNode -> arrayOf(ActionManager.getInstance().getAction("sap.cx.loggers.apply.bundle.template"))
            else -> emptyArray()
        }
    }

    override fun update(e: AnActionEvent) {
        val selectedNode = e.selectedNode()
        e.presentation.isEnabledAndVisible = selectedNode is BundledLoggersTemplateItemNode || selectedNode is CustomLoggersTemplateLoggersOptionsNode
    }
}