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

import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphFactory
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNodeClassifier
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.openapi.diagnostic.Logger

object TSDiagramRefresher {

    private val logger = Logger.getInstance(TSDiagramRefresher::class.java)

    fun refresh(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        refreshNodes(model, nodesMap)
        refreshEdges(model, nodesMap, edges)
    }

    private fun refreshNodes(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        nodesMap.clear()

        collectNodesBasic(model, nodesMap)
        // relations have to be collected before any extends
//        collectNodesRelations(model, nodesMap)
        collectNodesExtends(model, nodesMap)
    }

    private fun collectNodesBasic(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        TSMetaModelAccess.getInstance(model.project).getAll()
            .filter { it.name != null }
            .filter { !model.removedNodes.contains(it.name) }
            .filter { it.isCustom }
            .forEach {
                if (nodesMap.containsKey(it.name!!)) {
                    logger.warn("Classifier name collision: " + it + " vs " + nodesMap.containsKey(it.name!!))
                }
                nodesMap[it.name!!] = TSDiagramNode(TSGraphFactory.buildNode(it), model.provider)
            }
    }

    private fun collectNodesExtends(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        nodesMap.values
            .flatMap { sourceNode ->
                val graphNode = sourceNode.graphNode

                if (graphNode is TSGraphNodeClassifier) {
                    val meta = graphNode.meta
                    if (meta is TSGlobalMetaItem) {
                        return@flatMap meta.allExtends
                            .filter { extendsMeta -> extendsMeta.name != null }
                            .filter { extendsMeta -> nodesMap[extendsMeta.name] == null }
                            .filter { extendsMeta -> !model.removedNodes.contains(extendsMeta.name) }
                            .map { extendsMeta -> TSDiagramNode(TSGraphFactory.buildNode(extendsMeta), model.provider) }
                    }
                }

                return@flatMap emptyList()
            }
            .forEach { nodesMap[it.graphNode.name] = it }
    }

//    private fun collectNodesRelations(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
//        nodesMap.values
//            .flatMap { sourceNode ->
//                val graphNode = sourceNode.graphNode
//
//                if (graphNode is TSGraphNodeClassifier) {
//                    val meta = graphNode.meta
//                    if (meta is TSGlobalMetaItem) {
//                        return@flatMap meta.allRelationEnds
//                            .filter { relationEndMeta -> relationEndMeta.type != null }
//                            .filter { relationEndMeta -> nodesMap[relationEndMeta.name] == null }
//                            .map { relationEndMeta -> TSDiagramNode(TSGraphFactory.buildNode(relationEndMeta), model.provider) }
//                    }
//                }
//
//                return@flatMap emptyList()
//            }
//    }

    private fun refreshEdges(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        edges.clear()

        collectEdgesExtends(nodesMap, edges)
    }

    private fun collectEdgesExtends(nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        nodesMap.values.forEach { sourceNode ->
            val graphNode = sourceNode.graphNode

            if (graphNode is TSGraphNodeClassifier) {
                val meta = graphNode.meta
                if (meta is TSGlobalMetaItem) {
                    val targetNode = nodesMap[meta.extendedMetaItemName]
                    if (targetNode != null) {
                        edges.add(TSDiagramEdge(targetNode, sourceNode, TSDiagramRelationship("extends")))
                    }
                }
            }
        }
    }
}