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
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.TSDiagramSettings
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.Cardinality
import java.awt.Shape

object TSDiagramRefresher {

    fun refresh(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        val settings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(model.project).state.typeSystemDiagramSettings

        refreshNodes(model, nodesMap, settings)
        refreshEdges(model, nodesMap, edges)
    }

    private fun refreshNodes(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, settings: TSDiagramSettings) {
        nodesMap.clear()

        collectNodesItems(model, nodesMap, settings)
        collectNodesDependencies(model, nodesMap, settings)
        collectNodesExtends(model, nodesMap, settings)

        updatedCollapsedNodes(model, nodesMap, settings)
    }

    private fun collectNodesItems(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, settings: TSDiagramSettings) {
        TSMetaModelAccess.getInstance(model.project).getAll()
            .asSequence()
            .filter { it.name != null }
            .filterNot { model.removedNodes.contains(it.name) }
            .filterNot { settings.excludedTypeNames.contains(it.name) }
            .filter {
                when (it) {
                    is TSGlobalMetaItem -> true
                    is TSGlobalMetaAtomic -> settings.showCustomAtomicNodes
                    is TSGlobalMetaCollection -> settings.showCustomCollectionNodes
                    is TSGlobalMetaEnum -> settings.showCustomEnumNodes
                    is TSGlobalMetaMap -> settings.showCustomMapNodes
                    is TSGlobalMetaRelation -> settings.showCustomRelationNodes || it.deployment != null
                    else -> false
                }
            }
            .mapNotNull { TSGraphFactory.buildNode(it) }
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
    private fun collectNodesDependencies(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, settings: TSDiagramSettings) {
        if (!model.isShowDependencies) return

        nodesMap.values
            .flatMap { sourceNode ->
                val graphNode = sourceNode.graphNode

                if (graphNode is TSGraphNodeClassifier) {
                    return@flatMap DiagramElementManager.getNodeItemsAccordingToCurrentContentSettings(graphNode, model.builder)
                        .filterIsInstance<TSGraphField>()
                        .mapNotNull { graphField ->
                            val dependencyType = when (graphField) {
                                is TSGraphFieldRelationEnd -> graphField.meta.type
                                is TSGraphFieldRelationElement -> graphField.meta.type
                                is TSGraphFieldAttribute -> graphField.meta.type
                                is TSGraphFieldTyped -> graphField.value
                                else -> null
                            }
                                ?: return@mapNotNull null

                            if (model.removedNodes.contains(dependencyType)) return@mapNotNull null
                            if (settings.excludedTypeNames.contains(dependencyType)) return@mapNotNull null
                            if (nodesMap.containsKey(dependencyType)) return@mapNotNull null

                            val transitiveNode = TSGraphFactory.buildTransitiveNode(model.project, dependencyType)
                            if (!settings.showOOTBMapNodes && transitiveNode?.meta is TSGlobalMetaMap) return@mapNotNull null
                            return@mapNotNull transitiveNode
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
    private fun collectNodesExtends(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, settings: TSDiagramSettings) {
        nodesMap.values
            .flatMap { sourceNode ->
                val graphNode = sourceNode.graphNode

                if (graphNode is TSGraphNodeClassifier) {
                    val meta = graphNode.meta
                    if (meta is TSGlobalMetaItem) {
                        return@flatMap meta.allExtends
                            .asSequence()
                            .filter { extendsMeta -> extendsMeta.name != null }
                            .filterNot { extendsMeta -> settings.excludedTypeNames.contains(extendsMeta.name) }
                            .filter { extendsMeta -> nodesMap[extendsMeta.name] == null }
                            .filterNot { extendsMeta -> model.removedNodes.contains(extendsMeta.name) }
                            .mapNotNull { extendsMeta -> TSGraphFactory.buildTransitiveNode(extendsMeta) }
                            .filter { extendsGraphNode -> model.scopeManager?.contains(extendsGraphNode) ?: true }
                            .toList()
                    }
                }

                return@flatMap emptyList()
            }
            .map { TSDiagramNode(it, model.provider) }
            .forEach { nodesMap[it.graphNode.name] = it }
    }

    private fun updatedCollapsedNodes(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, settings: TSDiagramSettings) {
        if (model.modificationCount == 0L && settings.nodesCollapsedByDefault) {
            model.collapseAllNodes()
        }
        nodesMap.values
            .map { it.graphNode }
            .filter { model.collapsedNodes.contains(it.name) }
            .forEach {
                it.fields.clear()
                it.collapsed = true
            }
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
                        var name: String? = null
                        var type: String? = null

                        when (graphField) {
                            is TSGraphFieldRelationElement -> if (graphField.meta.modifiers.isPartOf) {
                                name = graphField.name
                                type = graphField.meta.type
                            }

                            is TSGraphFieldAttribute -> if (graphField.meta.modifiers.isPartOf) {
                                name = graphField.name
                                type = graphField.meta.type
                            }
                        }
                        if (name == null || type == null) return@mapNotNull null

                        val targetNode = nodesMap[type] ?: return@mapNotNull null
                        val relationship = TSDiagramRelationship(
                            upperCenterLabel = message("hybris.diagram.ts.provider.edge.part_of", name),
                            lineType = DiagramLineType.DOTTED,
                            targetArrow = DiagramRelationshipInfo.CONVEX,
                            sourceArrow = DiagramRelationshipInfo.CONCAVE
                        )
                        TSDiagramEdge(sourceNode, targetNode, relationship, TSDiagramEdgeType.PART_OF)
                    }
                    .forEach { edges.add(it) }
            }
        }
    }

    private fun collectEdgesDependencies(model: TSDiagramDataModel, nodesMap: MutableMap<String, TSDiagramNode>, edges: MutableCollection<TSDiagramEdge>) {
        nodesMap.values.forEach { sourceNode ->
            val sourceGraphNode = sourceNode.graphNode

            if (sourceGraphNode is TSGraphNodeClassifier) {
                DiagramElementManager.getNodeItemsAccordingToCurrentContentSettings(sourceGraphNode, model.builder)
                    .filterIsInstance<TSGraphField>()
                    .mapNotNull { graphField ->
                        var name: String? = null
                        var type: String? = null
                        var sourceArrow: Shape? = null
                        var targetArrow: Shape? = null
                        var navigable = true

                        when (graphField) {
                            is TSGraphFieldRelationEnd -> {
                                name = graphField.name
                                type = graphField.meta.type
                                navigable = graphField.meta.isNavigable
                                sourceArrow = if (graphField.meta.modifiers.isOptional) DiagramRelationshipInfo.CROWS_FOOT_ONE_OPTIONAL
                                else DiagramRelationshipInfo.CROWS_FOOT_ONE
                                targetArrow = if (graphField.meta.cardinality == Cardinality.ONE) DiagramRelationshipInfo.CROWS_FOOT_ONE
                                else DiagramRelationshipInfo.CROWS_FOOT_MANY
                            }

                            is TSGraphFieldRelationElement -> {
                                name = graphField.name
                                type = graphField.meta.type
                                navigable = graphField.meta.isNavigable
                                sourceArrow = if (graphField.meta.modifiers.isOptional) DiagramRelationshipInfo.CROWS_FOOT_ONE_OPTIONAL
                                else DiagramRelationshipInfo.CROWS_FOOT_ONE
                                targetArrow = if (graphField.meta.cardinality == Cardinality.ONE) DiagramRelationshipInfo.CROWS_FOOT_ONE
                                else DiagramRelationshipInfo.CROWS_FOOT_MANY
                            }

                            is TSGraphFieldAttribute -> {
                                name = graphField.name
                                type = graphField.meta.type
                                sourceArrow = if (graphField.meta.modifiers.isOptional) DiagramRelationshipInfo.CROWS_FOOT_ONE_OPTIONAL
                                else DiagramRelationshipInfo.CROWS_FOOT_ONE
                            }

                            is TSGraphFieldTyped -> {
                                name = graphField.name
                                type = graphField.value
                            }
                        }
                        if (name == null || type == null) return@mapNotNull null

                        val targetNode = nodesMap[type] ?: return@mapNotNull null
                        val targetGraphNode = targetNode.graphNode

                        if (targetGraphNode !is TSGraphNodeClassifier) return@mapNotNull null

                        val targetMeta = targetGraphNode.meta

                        if (targetMeta is TSGlobalMetaCollection || targetMeta is TSGlobalMetaMap) {
                            targetArrow = DiagramRelationshipInfo.CROWS_FOOT_MANY
                        }
                        val relationship = TSDiagramRelationship(
                            upperCenterLabel = name,
                            lineType = DiagramLineType.DASHED,
                            sourceArrow = sourceArrow ?: DiagramRelationshipInfo.CROWS_FOOT_ONE,
                            targetArrow = targetArrow
                        )

                        val edgeType = if (navigable) TSDiagramEdgeType.DEPENDENCY_NAVIGABLE
                        else TSDiagramEdgeType.DEPENDENCY

                        TSDiagramEdge(sourceNode, targetNode, relationship, edgeType)
                    }
                    .forEach { edges.add(it) }
            }
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
                        ?.let { targetNode ->
                            TSDiagramEdge(sourceNode, targetNode, TSDiagramRelationship(
                                upperCenterLabel = label,
                                targetArrow = DiagramRelationshipInfo.DELTA
                            ), TSDiagramEdgeType.EXTENDS)
                        }
                        ?.let { edge -> edges.add(edge) }
                }
            }
        }
    }
}