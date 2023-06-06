/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.project.configurators.ModulesDependenciesConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YOotbRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformExtModuleDescriptor
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel

class DefaultModulesDependenciesConfigurator : ModulesDependenciesConfigurator {
    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val modulesChosenForImport = hybrisProjectDescriptor.modulesChosenForImport
        val allModules = modifiableModelsProvider.modules
            .associateBy { it.name }
        val extModules = modulesChosenForImport.filterIsInstance<YPlatformExtModuleDescriptor>()
            .toSet()

        modulesChosenForImport
            .filterIsInstance<YModuleDescriptor>()
            .forEach { yModuleDescriptor ->
                allModules[yModuleDescriptor.ideaModuleName()]
                    ?.let { configureModuleDependencies(yModuleDescriptor, it, allModules, extModules, modifiableModelsProvider) }
            }

        val platformDescriptor = hybrisProjectDescriptor.platformHybrisModuleDescriptor
        allModules[platformDescriptor.ideaModuleName()]
            ?.let {
                val rootModel = modifiableModelsProvider.getModifiableRootModel(it)
                modulesChosenForImport
                    .filterIsInstance<YPlatformExtModuleDescriptor>()
                    .forEach { dependency -> processModuleDependency(allModules, dependency.ideaModuleName(), rootModel) }
            }
        val configDescriptor = hybrisProjectDescriptor.configHybrisModuleDescriptor

        if (configDescriptor != null) {
            val module = allModules[configDescriptor.ideaModuleName()]
            val rootModel = modifiableModelsProvider.getModifiableRootModel(module)
            processModuleDependency(allModules, configDescriptor.ideaModuleName(), rootModel)
        }
    }

    private fun configureModuleDependencies(
        moduleDescriptor: YModuleDescriptor,
        module: Module,
        allModules: Map<String, Module>,
        extModules: Set<ModuleDescriptor>,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val rootModel = modifiableModelsProvider.getModifiableRootModel(module)

        moduleDescriptor.dependenciesTree
            .filterNot { moduleDescriptor is YOotbRegularModuleDescriptor && extModules.contains(it) }
            .forEach { processModuleDependency(allModules, it.ideaModuleName(), rootModel) }
    }

    private fun processModuleDependency(
        allModules: Map<String, Module>,
        dependencyName: String,
        rootModel: ModifiableRootModel
    ) {
        val moduleOrderEntry = allModules[dependencyName]
            ?.let { rootModel.addModuleOrderEntry(it) }
            ?: rootModel.addInvalidModuleEntry(dependencyName)

        with(moduleOrderEntry) {
            isExported = true
            scope = DependencyScope.COMPILE
        }
    }
}
