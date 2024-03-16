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
package com.intellij.idea.plugin.hybris.diagram.module

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramColorManagerBase
import com.intellij.diagram.DiagramEdge
import com.intellij.diagram.DiagramNode
import com.intellij.idea.plugin.hybris.diagram.module.node.ModuleDepDiagramEdge
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphNodeModule
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.openapi.editor.colors.ColorKey

class ModuleDepDiagramColorManager : DiagramColorManagerBase() {

    override fun getNodeHeaderBackground(builder: DiagramBuilder, node: DiagramNode<*>, graphNode: Any) = when (graphNode) {
        is ModuleDepGraphNodeModule -> when (graphNode.type) {
            ModuleDescriptorType.CUSTOM -> getColor(
                builder,
                graphNode,
                ModuleDepDiagramColors.NODE_HEADER_CUSTOM_BACKGROUND,
                ModuleDepDiagramColors.NODE_HEADER_CUSTOM_SUB_MODULE_BACKGROUND
            )

            ModuleDescriptorType.OOTB -> getColor(
                builder,
                graphNode,
                ModuleDepDiagramColors.NODE_HEADER_OOTB_BACKGROUND,
                ModuleDepDiagramColors.NODE_HEADER_OOTB_SUB_MODULE_BACKGROUND
            )

            ModuleDescriptorType.PLATFORM,
            ModuleDescriptorType.EXT -> getColor(
                builder,
                graphNode,
                ModuleDepDiagramColors.NODE_HEADER_CORE_BACKGROUND,
                ModuleDepDiagramColors.NODE_HEADER_CORE_SUB_MODULE_BACKGROUND
            )

            else -> super.getNodeHeaderBackground(builder, node, graphNode)
        }

        else -> super.getNodeHeaderBackground(builder, node, graphNode)
    }

    override fun getEdgeColorKey(builder: DiagramBuilder, edge: DiagramEdge<*>) = when (edge) {
        is ModuleDepDiagramEdge -> if (edge.isCircular()) ModuleDepDiagramColors.EDGE_CIRCULAR

        else when (val targetGraphNode = edge.target.identifyingElement) {
            is ModuleDepGraphNodeModule -> when (targetGraphNode.type) {
                ModuleDescriptorType.CUSTOM -> getColorKey(
                    targetGraphNode,
                    ModuleDepDiagramColors.EDGE_TO_CUSTOM,
                    ModuleDepDiagramColors.EDGE_TO_CUSTOM_SUB_MODULE
                )

                ModuleDescriptorType.OOTB -> getColorKey(
                    targetGraphNode,
                    ModuleDepDiagramColors.EDGE_TO_OOTB,
                    ModuleDepDiagramColors.EDGE_TO_OOTB_SUB_MODULE
                )

                ModuleDescriptorType.PLATFORM,
                ModuleDescriptorType.EXT -> getColorKey(
                    targetGraphNode,
                    ModuleDepDiagramColors.EDGE_TO_CORE,
                    ModuleDepDiagramColors.EDGE_TO_CORE_SUB_MODULE
                )

                else -> super.getEdgeColorKey(builder, edge)
            }

            else -> super.getEdgeColorKey(builder, edge)
        }

        else -> super.getEdgeColorKey(builder, edge)
    }

    private fun getColor(
        builder: DiagramBuilder,
        graphNode: ModuleDepGraphNodeModule,
        primaryColorKey: ColorKey,
        secondaryColorKey: ColorKey
    ) = getColorFromScheme(builder, getColorKey(graphNode, primaryColorKey, secondaryColorKey))

    private fun getColorKey(
        graphNode: ModuleDepGraphNodeModule,
        primaryColorKey: ColorKey,
        secondaryColorKey: ColorKey
    ) = if (graphNode.subModuleType == null) primaryColorKey
    else secondaryColorKey

}
