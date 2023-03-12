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
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.openapi.diagnostic.Logger

object TSDiagramRefresher {

    private val logger = Logger.getInstance(TSDiagramRefresher::class.java)

    fun refresh(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        refreshNodes(model, nodesMap)
        refreshEdges(nodesMap, edges)
    }

    private fun refreshNodes(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        nodesMap.clear()

        collectNodesItems(model, nodesMap)
        collectNodesRelations(model, nodesMap)
        collectNodesDependencies(model, nodesMap)
        collectNodesExtends(model, nodesMap)
    }

    private fun collectNodesItems(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        TSMetaModelAccess.getInstance(model.project).getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
            .asSequence()
            .filter { it.name != null }
            .filterNot { model.removedNodes.contains(it.name) }
            .map { TSGraphFactory.buildNode(it) }
            .filter { model.scopeManager?.contains(it) ?: true }
            .map { TSDiagramNode(it, model.provider) }
            .toList()
            .forEach {
                nodesMap[it.graphNode.name] = it
            }
    }

    /**
     * This collector will create special type of the GraphNode with flag `additionalNode` = true.
     * It is required to not produce too many non-custom `extends` nodes.
     * Such nodes will be shown only in case of Scope level: "Custom with Extends" or "All"
     *
     * Also, it is possible to specify STOP Types for extends names to limit down amount of created "shared" Edges
     */
    private fun collectNodesExtends(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        val stopTypes = HybrisApplicationSettingsComponent.getInstance().state.tsdStopTypeList
        nodesMap.values
            .flatMap { sourceNode ->
                val graphNode = sourceNode.graphNode

                if (graphNode is TSGraphNodeClassifier) {
                    val meta = graphNode.meta
                    if (meta is TSGlobalMetaItem) {
                        return@flatMap meta.allExtends
                            .asSequence()
                            .filter { extendsMeta -> extendsMeta.name != null }
                            .filterNot { extendsMeta -> stopTypes.contains(extendsMeta.name) }
                            .filter { extendsMeta -> nodesMap[extendsMeta.name] == null }
                            .filterNot { extendsMeta -> model.removedNodes.contains(extendsMeta.name) }
                            .map { extendsMeta -> TSGraphFactory.buildNode(extendsMeta, true) }
                            .filter { extendsGraphNode -> model.scopeManager?.contains(extendsGraphNode) ?: true }
                            .map { extendsGraphNode -> TSDiagramNode(extendsGraphNode, model.provider) }
                            .toList()
                    }
                }

                return@flatMap emptyList()
            }
            .forEach { nodesMap[it.graphNode.name] = it }
    }

    private fun collectNodesRelations(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        TSMetaModelAccess.getInstance(model.project).getAll<TSGlobalMetaRelation>(TSMetaType.META_RELATION)
            .asSequence()
            .filter { it.name != null }
            .filterNot { model.removedNodes.contains(it.name) }
            .filter { it.deployment != null }
            .map { TSGraphFactory.buildNode(it) }
            .filter { model.scopeManager?.contains(it) ?: true }
            .map { TSDiagramNode(it, model.provider) }
            .toList()
            .forEach {
                nodesMap[it.graphNode.name] = it
            }
    }

    private fun collectNodesDependencies(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        if (model.isShowDependencies) {

        }
    }

    private fun refreshEdges(nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
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