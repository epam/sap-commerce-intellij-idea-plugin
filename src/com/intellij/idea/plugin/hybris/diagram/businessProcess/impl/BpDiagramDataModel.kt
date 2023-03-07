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
package com.intellij.idea.plugin.hybris.diagram.businessProcess.impl

import com.intellij.diagram.DiagramDataModel
import com.intellij.diagram.DiagramNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramProvider
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import org.jetbrains.annotations.Contract
import java.io.Serial

class BpDiagramDataModel(
    project: Project,
    rootBpGraphNode: BpGraphNode?
) : DiagramDataModel<BpGraphNode?>(project, ApplicationManager.getApplication().getService(BpDiagramProvider::class.java)) {

    private val edges: MutableCollection<BpDiagramFileEdge> = ArrayList()
    private val nodesMap: MutableMap<String, BpDiagramFileNode> = HashMap()

    init {
        rootBpGraphNode
            ?.nodesMap
            ?.values
            ?.filter { it.navigableElement.getId().stringValue != null }
            ?.forEach {
                val bpDiagramFileNode = BpDiagramFileNode(it)
                nodesMap[it.navigableElement.getId().stringValue!!] = bpDiagramFileNode
            }
    }

    override fun getNodes() = nodesMap.values
    override fun getEdges() = edges
    override fun getNodeName(diagramNode: DiagramNode<BpGraphNode?>) = diagramNode.identifyingElement.navigableElement.getId().stringValue
        ?: "node"

    @Contract(value = "_ -> null", pure = true)
    override fun addElement(t: BpGraphNode?): BpDiagramFileNode? = null

    override fun refreshDataModel() {
        edges.clear()

        nodesMap.values
            .forEach { targetBpDiagramFileNode ->
                val sourceBpGraphNode = targetBpDiagramFileNode.identifyingElement

                sourceBpGraphNode.transitions
                    .forEach { (transitionName, targetBpGraphNode) ->
                        nodesMap[targetBpGraphNode.navigableElement.getId().stringValue]
                            ?.let { sourceBpDiagramFileNode ->
                                edges.add(BpDiagramFileEdge(sourceBpDiagramFileNode, targetBpDiagramFileNode, BpDiagramRelationship(transitionName)))
                            }
                    }
            }

    }

    @Contract(pure = true)
    override fun getModificationTracker(): ModificationTracker = this

    override fun dispose() {}

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4815844034119153893L
    }
}
