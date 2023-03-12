/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.diagram.typeSystem.node

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramColorManagerBase
import com.intellij.diagram.DiagramEdge
import com.intellij.diagram.DiagramNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors
import com.intellij.idea.plugin.hybris.diagram.typeSystem.TSDiagramColors
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNodeClassifier
import java.awt.Color

class TSDiagramColorManager : DiagramColorManagerBase() {

    override fun getEdgeColorKey(builder: DiagramBuilder, edge: DiagramEdge<*>) = when (edge) {
        is TSDiagramEdge -> when (edge.type) {
            TSDiagramEdgeType.EXTENDS -> TSDiagramColors.EDGE_EXTENDS
            TSDiagramEdgeType.PART_OF -> TSDiagramColors.EDGE_PART_OF
            TSDiagramEdgeType.DEPENDENCY -> TSDiagramColors.EDGE_DEPENDENCY
            TSDiagramEdgeType.DEPENDENCY_NAVIGABLE -> TSDiagramColors.EDGE_DEPENDENCY_NAVIGABLE
            else -> BpDiagramColors.EDGE_DEFAULT
        }

        else -> BpDiagramColors.EDGE_DEFAULT
    }

    override fun getNodeHeaderBackground(builder: DiagramBuilder, node: DiagramNode<*>, graphNode: Any?): Color = when (graphNode) {
        is TSGraphNodeClassifier -> {
            (if (graphNode.meta.isCustom) builder.colorScheme.getColor(TSDiagramColors.NODE_HEADER_CUSTOM) else null)
                ?: super.getNodeHeaderBackground(builder, node, graphNode)
        }

        else -> super.getNodeHeaderBackground(builder, node, graphNode)
    }

    private fun color(builder: DiagramBuilder, graphNode: TSGraphNodeClassifier): Color? {
        if (graphNode.meta.isCustom) {
            return builder.colorScheme.getColor(TSDiagramColors.NODE_HEADER_CUSTOM)
        }
        return null
    }

}