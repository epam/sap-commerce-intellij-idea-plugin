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
package com.intellij.idea.plugin.hybris.diagram.businessProcess.node

import com.intellij.diagram.DiagramDataModel
import com.intellij.diagram.DiagramNode
import com.intellij.diagram.DiagramProvider
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphService
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphFactory
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNodeNavigable
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNodeRoot
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import org.jetbrains.annotations.Contract
import java.io.Serial

class BpDiagramDataModel(
    project: Project,
    private val rootBpGraphNode: BpGraphNode?,
    provider: DiagramProvider<BpGraphNode?>
) : DiagramDataModel<BpGraphNode?>(project, provider) {

    private val edges: MutableCollection<BpDiagramEdge> = mutableSetOf()
    private val nodesMap: MutableMap<String, BpDiagramNode> = mutableMapOf()

    override fun getNodes() = nodesMap.values
    override fun getEdges() = edges
    override fun getNodeName(diagramNode: DiagramNode<BpGraphNode?>) = diagramNode.identifyingElement.name

    @Contract(value = "_ -> null", pure = true)
    override fun addElement(t: BpGraphNode?): BpDiagramNode? = null

    override fun refreshDataModel() {
        if (rootBpGraphNode !is BpGraphNodeRoot) return

        edges.clear()
        nodesMap.clear()

        val graphService = BpGraphService.getInstance(this.project)
        graphService.buildNodes(rootBpGraphNode)
            .values
            .forEach {
                val bpDiagramFileNode = BpDiagramNode(it, provider)
                nodesMap[it.name] = bpDiagramFileNode
            }

        nodesMap[rootBpGraphNode.name] = BpDiagramNode(rootBpGraphNode, provider)

        nodesMap["Context Parameters"]
            ?.let { BpGraphFactory.buildEdge("parameters", nodesMap[rootBpGraphNode.name]!!, it) }
            ?.let { edges.add(it) }

        nodesMap.values
            .filter { it.identifyingElement is BpGraphNodeNavigable }
            .forEach { targetBpDiagramFileNode ->
                val sourceBpGraphNode = targetBpDiagramFileNode.identifyingElement

                (sourceBpGraphNode as BpGraphNodeNavigable).transitions
                    .forEach { (transitionName, targetBpGraphNode) ->
                        nodesMap[targetBpGraphNode.name]
                            ?.let { sourceBpDiagramFileNode -> BpGraphFactory.buildEdge(transitionName, sourceBpDiagramFileNode, targetBpDiagramFileNode) }
                            ?.let { edges.add(it) }
                    }
            }
    }

    @Contract(pure = true)
    override fun getModificationTracker(): ModificationTracker = this

    override fun dispose() {
        edges.clear()
        nodesMap.clear()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4815844034119153893L
    }
}
