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
import com.intellij.diagram.DiagramEdge
import com.intellij.diagram.DiagramNode
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.diagram.typeSystem.TSDiagramProvider
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.openapi.project.Project

class TSDiagramDataModel(myProject: Project, val root: TSGraphItem, provider: TSDiagramProvider) :
    DiagramDataModel<TSGraphItem>(myProject, provider) {

    private val myNodes: MutableMap<String, TSGraphNode> = hashMapOf()
    private val myEdges: MutableSet<TSDiagramEdge> = mutableSetOf()

    override fun dispose() {
        myEdges.clear()
        myNodes.clear()
    }

    override fun getNodes(): Collection<TSGraphNode> = myNodes.values

    override fun getEdges() = myEdges

    override fun getModificationTracker() = this

    override fun addElement(item: TSGraphItem?) = null

    override fun createEdge(from: DiagramNode<TSGraphItem>, to: DiagramNode<TSGraphItem>): DiagramEdge<TSGraphItem>? {
        return super.createEdge(from, to)
    }

    override fun getNodeName(node: DiagramNode<TSGraphItem>) = node.identifyingElement.meta?.name ?: "root node"

    override fun refreshDataModel() {
        myEdges.clear()
        myNodes.clear()

        TSMetaModelAccess.getInstance(project).getMetaModel().getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM)
            .values
            .filter { it.name != null }
            .filter { it.isCustom }
            .forEach {
                myNodes[it.name!!.lowercase()] = TSGraphNode(TSGraphItem(it), provider)
            }

        myNodes.values
            .filter { it.item.meta?.name != null }
            .forEach { sourceNode ->
                when (val meta = sourceNode.item.meta) {
                    is TSGlobalMetaItem -> {
                        processEdge(meta, sourceNode)
                    }
                }
            }
    }

    private fun processEdge(
        meta: TSGlobalMetaItem,
        sourceNode: TSGraphNode
    ) {
        val extendsName = meta.extendedMetaItemName?.lowercase() ?: HybrisConstants.TS_TYPE_ITEM.lowercase()
        var targetNode = myNodes[extendsName]

        if (targetNode == null) {
            val extendsMeta = TSMetaModelAccess.getInstance(project).findMetaItemByName(extendsName)

            if (extendsMeta?.name != null) {
                targetNode = TSGraphNode(TSGraphItem(extendsMeta), provider)
                myNodes[extendsMeta.name!!.lowercase()] = targetNode

                if (extendsName != HybrisConstants.TS_TYPE_ITEM.lowercase()) {
                    processEdge(extendsMeta, targetNode)
                }
            }
        }

        if (targetNode != null) {
            myEdges.add(TSDiagramEdge(sourceNode, targetNode, TSDiagramRelationship("extends")))
        }
    }

    companion object {
        private const val serialVersionUID: Long = 4148393944331345630L
    }

}
