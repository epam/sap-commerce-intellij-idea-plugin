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

import com.intellij.idea.plugin.hybris.diagram.module.ModuleDepDiagramVisibilityManager
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphNode
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.uml.java.project.*

object ModuleDepDiagramRefresher {

    fun refresh(model: ModuleDepDiagramDataModel, nodes: MutableSet<ModuleDepDiagramNode>, edges: MutableSet<ModuleDepDiagramEdge>) {
        nodes.clear()
        edges.clear()

        val items = modulesToShow(model)
            .map { ModuleItem(it) }
        val modulesUmlProvider = ModulesUmlProvider()
        val item2Node = items.associateWith { ModulesUmlNode(it, modulesUmlProvider) }
        val umlEdges = UmlModulesRelationshipHelper.generateEdges(item2Node, model.project, false)
        nodes.addAll(createAdaptedNodes(model, items))
        edges.addAll(createAdaptedEdges(model, umlEdges))
        TarjanCircularDetection(nodes, edges).detectAndMarkCircles()
    }

    private fun modulesToShow(model: ModuleDepDiagramDataModel): Collection<Module> {
        val visibilityLevel = model.visibilityManager.currentVisibilityLevel
        val allModules = ModuleManager.getInstance(model.project).modules

        if (ModuleDepDiagramVisibilityManager.ALL_MODULES == visibilityLevel) {
            return allModules
                .filter { isCustomExtension(it) || isOotbOrPlatformExtension(it) }
        }
        val customExtModules = allModules
            .filter { isCustomExtension(it) }

        if (ModuleDepDiagramVisibilityManager.ONLY_CUSTOM_MODULES == visibilityLevel) return customExtModules

        val dependencies = customExtModules
            .flatMap { ModuleRootManager.getInstance(it).dependencies.asIterable() }
            .filter { isOotbOrPlatformExtension(it) }
        val backwardDependencies = allModules
            .filter {
                ModuleRootManager.getInstance(it).dependencies
                    .any { module: Module -> isCustomExtension(module) }
            }
            .filter { isOotbOrPlatformExtension(it) }
        return customExtModules + dependencies + backwardDependencies
    }

    private fun isCustomExtension(module: Module) = HybrisModuleDescriptor.getDescriptorType(module) == HybrisModuleDescriptorType.CUSTOM

    private fun isOotbOrPlatformExtension(module: Module) = with(HybrisModuleDescriptor.getDescriptorType(module)) {
        this == HybrisModuleDescriptorType.OOTB || this == HybrisModuleDescriptorType.PLATFORM
    }

    private fun createAdaptedEdges(model: ModuleDepDiagramDataModel, edges: Collection<ModulesUmlEdge>): List<ModuleDepDiagramEdge> = edges
        .map {
            val from = it.source.identifyingElement.module
            val fromItem = ModuleDepGraphNode(from, isCustomExtension(from))
            val to = it.target.identifyingElement.module
            val toItem = ModuleDepGraphNode(to, isCustomExtension(to))
            ModuleDepDiagramEdge(
                ModuleDepDiagramNode(fromItem, model.provider),
                ModuleDepDiagramNode(toItem, model.provider),
                it.relationship
            )
        }

    private fun createAdaptedNodes(model: ModuleDepDiagramDataModel, items: Collection<ModuleItem>): List<ModuleDepDiagramNode> = items
        .map {
            ModuleDepDiagramNode(ModuleDepGraphNode(it.module, isCustomExtension(it.module)), model.provider)
        }
}