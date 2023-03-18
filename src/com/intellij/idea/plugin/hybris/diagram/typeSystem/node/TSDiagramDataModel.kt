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

import com.intellij.diagram.DiagramDataModel
import com.intellij.diagram.DiagramNode
import com.intellij.idea.plugin.hybris.diagram.typeSystem.TSDiagramProvider
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNode
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Contract

/**
 * We need to override addElement method to ensure that Node will be re-added when Type System Diagram generated from the DiagramState (2nd+ generation)
 */
class TSDiagramDataModel(val myProject: Project, provider: TSDiagramProvider)
    : DiagramDataModel<TSGraphNode>(myProject, provider) {

    private val edges: MutableCollection<TSDiagramEdge> = mutableSetOf()
    private val nodesMap: MutableMap<String, TSDiagramNode> = mutableMapOf()
    val everShownNodes: MutableSet<String> = mutableSetOf()
    val removedNodes: MutableSet<String> = mutableSetOf()
    val collapsedNodes: MutableSet<String> = mutableSetOf()

    @Contract(pure = true)
    override fun getModificationTracker() = createModificationTracker()
    override fun isDependencyDiagramSupported() = true
    override fun getNodes() = nodesMap.values
    override fun getEdges() = edges
    override fun collapseNode(node: DiagramNode<TSGraphNode>) {
        collapsedNodes.add(node.identifyingElement.name)
        node.identifyingElement.collapsed = false
    }

    override fun expandNode(node: DiagramNode<TSGraphNode>) {
        collapsedNodes.remove(node.identifyingElement.name)
        node.identifyingElement.collapsed = true
    }

    override fun getNodeName(diagramNode: DiagramNode<TSGraphNode>) = diagramNode.identifyingElement.name

    override fun addElement(node: TSGraphNode?): DiagramNode<TSGraphNode>? {
        removedNodes.remove(node?.name)

        return nodesMap[node?.name]
    }

    override fun removeNode(node: DiagramNode<TSGraphNode>) {
        val name = node.identifyingElement.name
        if (nodesMap.containsKey(name)) {
            removedNodes.add(name)
        }
    }

    override fun refreshDataModel() = DumbService.getInstance(myProject).runReadActionInSmartMode {
        TSDiagramRefresher.refresh(this, nodesMap, edges)
        this.incModificationCount()
    }

    fun collapseAllNodes() {
        collapsedNodes.clear()
        nodes.forEach { collapseNode(it) }
    }

    fun expandAllNodes() {
        nodes.forEach { expandNode(it) }
    }

    override fun dispose() {
        edges.clear()
        nodesMap.clear()
        removedNodes.clear()
        collapsedNodes.clear()
        everShownNodes.clear()
    }

    companion object {
        private const val serialVersionUID: Long = 4148393944331345630L
    }

}
