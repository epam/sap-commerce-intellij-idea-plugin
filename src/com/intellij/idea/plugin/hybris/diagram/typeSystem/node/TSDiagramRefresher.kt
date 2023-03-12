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

import com.intellij.diagram.DiagramElementManager
import com.intellij.diagram.DiagramRelationshipInfo
import com.intellij.diagram.presentation.DiagramLineType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.*
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaRelation
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType

object TSDiagramRefresher {

    fun refresh(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        refreshNodes(model, nodesMap)
        refreshEdges(model, nodesMap, edges)
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

    private fun collectNodesRelations(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        TSMetaModelAccess.getInstance(model.project).getAll<TSGlobalMetaRelation>(TSMetaType.META_RELATION)
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
     * Additional dependency nodes will be shown only if User selected "Show Dependencies".
     * Each Node field will be traversed and corresponding new Node (`transitiveNode` = true) will be created.
     * Nested dependencies will not be created as we're not interested at this stage in complete picture.
     * If All possible dependencies will be needed, another Type Specific filter can be introduced with Scope = "All"
     */
    private fun collectNodesDependencies(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>) {
        if (!model.isShowDependencies) return
        val stopTypes = HybrisApplicationSettingsComponent.getInstance().state.tsdStopTypeList

        nodesMap.values
            .flatMap { sourceNode ->
                val graphNode = sourceNode.graphNode

                if (graphNode is TSGraphNodeClassifier) {
                    return@flatMap DiagramElementManager.getNodeItemsAccordingToCurrentContentSettings(graphNode, model.builder)
                        .filterIsInstance<TSGraphField>()
                        .mapNotNull { graphField ->
                            when (graphField) {
                                is TSGraphFieldRelationEnd -> graphField.meta.type
                                is TSGraphFieldRelationElement -> graphField.meta.type
                                is TSGraphFieldAttribute -> graphField.meta.type
                                is TSGraphFieldTyped -> graphField.value
                                else -> null
                            }
                        }
                        .mapNotNull { dependencyType ->
                            if (model.removedNodes.contains(dependencyType)) return@mapNotNull null
                            if (stopTypes.contains(dependencyType)) return@mapNotNull null
                            if (nodesMap.containsKey(dependencyType)) return@mapNotNull null

                            return@mapNotNull TSGraphFactory.buildTransitiveNode(model.project, dependencyType)
                        }
                }

                return@flatMap emptyList()
            }
            .map { TSDiagramNode(it, model.provider) }
            .forEach { nodesMap[it.graphNode.name] = it }
    }

    /**
     * This collector will create special type of the GraphNode with flag `transitiveNode` = true.
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
                            .map { extendsMeta -> TSGraphFactory.buildTransitiveNode(extendsMeta) }
                            .filter { extendsGraphNode -> model.scopeManager?.contains(extendsGraphNode) ?: true }
                            .toList()
                    }
                }

                return@flatMap emptyList()
            }
            .map { TSDiagramNode(it, model.provider) }
            .forEach { nodesMap[it.graphNode.name] = it }
    }

    private fun refreshEdges(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        edges.clear()

        collectEdgesExtends(nodesMap, edges)
        collectEdgesPartOf(model, nodesMap, edges)
        collectEdgesDependencies(model, nodesMap, edges)
    }

    private fun collectEdgesPartOf(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        nodesMap.values.forEach { sourceNode ->
            val graphNode = sourceNode.graphNode

            if (graphNode is TSGraphNodeClassifier) {
                DiagramElementManager.getNodeItemsAccordingToCurrentContentSettings(graphNode, model.builder)
                    .filterIsInstance<TSGraphField>()
                    .mapNotNull { graphField ->
                        when (graphField) {
                            is TSGraphFieldRelationElement -> if (graphField.meta.modifiers.isPartOf) (graphField.name to graphField.meta.type) else null
                            is TSGraphFieldAttribute -> if (graphField.meta.modifiers.isPartOf) (graphField.name to graphField.meta.type) else null
                            else -> null
                        }
                    }
                    .mapNotNull { pair ->
                        val targetNode = nodesMap[pair.second] ?: return@mapNotNull null
                        val relationship = TSDiagramRelationship(
                            upperCenterLabel = message("hybris.diagram.ts.provider.edge.part_of", pair.first),
                            lineType = DiagramLineType.DOTTED,
                            targetArrow =  DiagramRelationshipInfo.CONVEX,
                            sourceArrow =  DiagramRelationshipInfo.CONCAVE
                        )
                        TSDiagramEdge(sourceNode, targetNode, relationship, TSDiagramEdgeType.PART_OF)
                    }
                    .forEach { edges.add(it) }
            }
        }
    }

    private fun collectEdgesDependencies(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        nodesMap.values.forEach { sourceNode ->
            val graphNode = sourceNode.graphNode

//            if (graphNode is TSGraphNodeClassifier) {
//                DiagramElementManager.getNodeItemsAccordingToCurrentContentSettings(graphNode, model.builder)
//                    .filterIsInstance<TSGraphField>()
//                    .mapNotNull { graphField ->
//                        when (graphField) {
//                            is TSGraphFieldRelationEnd -> graphField.meta.type
//                            is TSGraphFieldRelationElement -> graphField.meta.type
//                            is TSGraphFieldAttribute -> graphField.meta.modifiers.type
//                            is TSGraphFieldTyped -> graphField.value
//                            else -> null
//                        }
//                    }
//                    .mapNotNull { dependencyType ->
//                        val targetNode = nodesMap[dependencyType] ?: return@mapNotNull  null
//
//                        if (model.removedNodes.contains(dependencyType)) return@mapNotNull null
//                        if (stopTypes.contains(dependencyType)) return@mapNotNull null
//                        if (nodesMap.containsKey(dependencyType)) return@mapNotNull null
//
//                        return@mapNotNull TSGraphFactory.buildTransitiveNode(model.project, dependencyType)
//                    }
//
//
//                val meta = graphNode.meta
//                if (meta is TSGlobalMetaItem) {
//                    nodesMap[meta.extendedMetaItemName]
//                        ?.let { targetNode -> TSDiagramEdge(targetNode, sourceNode, TSDiagramRelationship("11")) }
//                        ?.let { edge -> edges.add(edge) }
//                }
//            }
        }
    }

    private fun collectEdgesExtends(nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        val label: String by lazy { message("hybris.diagram.ts.provider.edge.extends") }
        nodesMap.values.forEach { sourceNode ->
            val graphNode = sourceNode.graphNode

            if (graphNode is TSGraphNodeClassifier) {
                val meta = graphNode.meta
                if (meta is TSGlobalMetaItem) {
                    nodesMap[meta.extendedMetaItemName]
                        ?.let { targetNode -> TSDiagramEdge(sourceNode, targetNode, TSDiagramRelationship(
                            upperCenterLabel = label,
                            targetArrow =  DiagramRelationshipInfo.DELTA
                        ), TSDiagramEdgeType.EXTENDS) }
                        ?.let { edge -> edges.add(edge) }
                }
            }
        }
    }
}