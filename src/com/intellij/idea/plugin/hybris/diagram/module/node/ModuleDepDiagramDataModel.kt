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
package com.intellij.idea.plugin.hybris.diagram.module.node

import com.intellij.diagram.DiagramDataModel
import com.intellij.diagram.DiagramNode
import com.intellij.diagram.DiagramProvider
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphNode
import com.intellij.openapi.project.Project
import java.io.Serial

class ModuleDepDiagramDataModel(project: Project, provider: DiagramProvider<ModuleDepGraphNode>) : DiagramDataModel<ModuleDepGraphNode>(project, provider) {

    private val myNodes: MutableSet<ModuleDepDiagramNode> = mutableSetOf()
    private val myEdges: MutableSet<ModuleDepDiagramEdge> = mutableSetOf()

    override fun getNodes() = myNodes.toList()
    override fun getEdges() = myEdges.toList()
    override fun getModificationTracker() = this
    override fun addElement(moduleItem: ModuleDepGraphNode?) = null
    override fun getNodeName(node: DiagramNode<ModuleDepGraphNode>) = node.identifyingElement.name
    override fun refreshDataModel() = ModuleDepDiagramRefresher.refresh(this, myNodes, myEdges)
    override fun dispose() {}

    companion object {
        @Serial
        private const val serialVersionUID: Long = 540309765628373954L
    }
}
