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
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphFactory
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
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
        val projectSettings = HybrisProjectSettingsComponent.getInstance(model.project)

        if (ModuleDepDiagramVisibilityManager.ALL_MODULES == visibilityLevel) {
            return allModules
                .filter {
                    val descriptor = projectSettings.getModuleSettings(it).type
                    isCustomExtension(descriptor) || isOotbOrPlatformExtension(descriptor)
                }
        }
        val customExtModules = allModules
            .filter { isCustomExtension(projectSettings.getModuleSettings(it).type) }

        if (ModuleDepDiagramVisibilityManager.ONLY_CUSTOM_MODULES == visibilityLevel) return customExtModules

        val dependencies = customExtModules
            .flatMap { ModuleRootManager.getInstance(it).dependencies.asIterable() }
            .filter { isOotbOrPlatformExtension(projectSettings.getModuleSettings(it).type) }
        val backwardDependencies = allModules
            .filter {
                ModuleRootManager.getInstance(it).dependencies
                    .any { module: Module -> isCustomExtension(projectSettings.getModuleSettings(module).type) }
            }
            .filter { isOotbOrPlatformExtension(projectSettings.getModuleSettings(it).type) }
        return customExtModules + dependencies + backwardDependencies
    }

    private fun isCustomExtension(descriptorType: ModuleDescriptorType) = descriptorType == ModuleDescriptorType.CUSTOM

    private fun isOotbOrPlatformExtension(descriptorType: ModuleDescriptorType) = with(descriptorType) {
        this == ModuleDescriptorType.OOTB || this == ModuleDescriptorType.PLATFORM || this == ModuleDescriptorType.EXT
    }

    private fun createAdaptedEdges(model: ModuleDepDiagramDataModel, edges: Collection<ModulesUmlEdge>): List<ModuleDepDiagramEdge> = edges
        .map {
            val from = it.source.identifyingElement.module
            val fromItem = ModuleDepGraphFactory.buildNode(from)
            val to = it.target.identifyingElement.module
            val toItem = ModuleDepGraphFactory.buildNode(to)
            ModuleDepDiagramEdge(
                ModuleDepDiagramNode(fromItem, model.provider),
                ModuleDepDiagramNode(toItem, model.provider),
                it.relationship
            )
        }

    private fun createAdaptedNodes(model: ModuleDepDiagramDataModel, items: Collection<ModuleItem>): List<ModuleDepDiagramNode> = items
        .map {
            ModuleDepDiagramNode(ModuleDepGraphFactory.buildNode(it.module), model.provider)
        }
}