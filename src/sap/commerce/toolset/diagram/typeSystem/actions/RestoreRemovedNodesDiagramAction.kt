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
package sap.commerce.toolset.diagram.typeSystem.actions

import com.intellij.diagram.DiagramAction
import sap.commerce.toolset.diagram.typeSystem.node.TSDiagramDataModel
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisI18NBundleUtils.message
import sap.commerce.toolset.HybrisIcons

class RestoreRemovedNodesDiagramAction : DiagramAction(
    message("hybris.diagram.ts.provider.actions.restore_removed_nodes"),
    message("hybris.diagram.ts.provider.actions.restore_removed_nodes.description"),
    HybrisIcons.TypeSystem.Diagram.Actions.RESET_VIEW
) {

    override fun perform(event: AnActionEvent) {
        getBuilder(event)
            ?.dataModel
            ?.asSafely<TSDiagramDataModel>()
            ?.let {
                it.removedNodes.clear()
                val action = ActionManager.getInstance().getAction("Diagram.RefreshDataModelManually")
                ActionUtil.performActionDumbAwareWithCallbacks(action, event)
            }
    }

    override fun getActionName() = message("hybris.diagram.ts.provider.actions.restore_removed_nodes")
}
