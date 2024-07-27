/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.diagram.typeSystem.actions

import com.intellij.diagram.DiagramAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.TSDiagramDataModel
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.asSafely

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
                ActionManager.getInstance().getAction("Diagram.RefreshDataModelManually").actionPerformed(event)
            }
    }

    override fun getActionName() = message("hybris.diagram.ts.provider.actions.restore_removed_nodes")
}
