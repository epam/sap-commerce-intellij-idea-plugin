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

package sap.commerce.toolset.typeSystem.diagram

import com.intellij.diagram.extras.DiagramExtras
import com.intellij.diagram.settings.DiagramConfigElement
import com.intellij.diagram.settings.DiagramConfigGroup
import com.intellij.openapi.actionSystem.ActionManager
import sap.commerce.toolset.typeSystem.diagram.node.graph.TSGraphNode

class TSDiagramExtras : DiagramExtras<TSGraphNode>() {

    private val diagramConfigGroups: Array<DiagramConfigGroup> by lazy {
        val categories = with(DiagramConfigGroup("Categories")) {
            TSDiagramNodeContentManager.CATEGORIES
                .map { DiagramConfigElement(it.name, true) }
                .forEach { addElement(it) }

            this
        }
        arrayOf(categories)
    }

    override fun getExtraActions() = ActionManager.getInstance().getAction("Diagram.Hybris.TypeSystem.Node.Actions")
        ?.let { mutableListOf(it) }
        ?: mutableListOf()

    override fun getAdditionalDiagramSettings() = diagramConfigGroups
    override fun getToolbarActionsProvider() = TSDiagramToolbarActionsProvider.getInstance()
    override fun isExpandCollapseActionsImplemented() = true
}