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

package sap.commerce.toolset.typeSystem.diagram.node

import com.intellij.diagram.*
import sap.commerce.toolset.typeSystem.diagram.TSDiagramColors
import sap.commerce.toolset.typeSystem.diagram.node.graph.TSGraphNodeClassifier
import java.awt.Color

class TSDiagramColorManager : DiagramColorManagerBase() {

    override fun getEdgeColorKey(builder: DiagramBuilder, edge: DiagramEdge<*>) = when (edge) {
        is TSDiagramEdge -> when (edge.type) {
            TSDiagramEdgeType.EXTENDS -> TSDiagramColors.EDGE_EXTENDS
            TSDiagramEdgeType.PART_OF -> TSDiagramColors.EDGE_PART_OF
            TSDiagramEdgeType.DEPENDENCY -> TSDiagramColors.EDGE_DEPENDENCY
            TSDiagramEdgeType.DEPENDENCY_NAVIGABLE -> TSDiagramColors.EDGE_DEPENDENCY_NAVIGABLE
            else -> DiagramColors.DEFAULT_EDGE
        }

        else -> DiagramColors.DEFAULT_EDGE
    }

    override fun getNodeHeaderBackground(builder: DiagramBuilder, node: DiagramNode<*>, graphNode: Any?): Color = when (graphNode) {
        is TSGraphNodeClassifier -> {
            (if (graphNode.meta.isCustom) builder.colorScheme.getColor(TSDiagramColors.NODE_HEADER_CUSTOM) else null)
                ?: super.getNodeHeaderBackground(builder, node, graphNode)
        }

        else -> super.getNodeHeaderBackground(builder, node, graphNode)
    }

}