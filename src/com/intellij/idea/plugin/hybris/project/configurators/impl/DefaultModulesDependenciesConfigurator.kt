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

        val platformIdeaModuleName = hybrisProjectDescriptor.platformHybrisModuleDescriptor.ideaModuleName()
        val platformModule = allModules[platformIdeaModuleName] ?: return

        modulesChosenForImport
            .filterIsInstance<YModuleDescriptor>()
            .forEach { yModuleDescriptor ->
                allModules[yModuleDescriptor.ideaModuleName()]
                    ?.let { module ->
                        val rootModel = modifiableModelsProvider.getModifiableRootModel(module)

                        yModuleDescriptor.dependenciesTree
                            .filterNot { yModuleDescriptor is YOotbRegularModuleDescriptor && extModules.contains(it) }
                            .forEach { addModuleDependency(allModules, it.ideaModuleName(), rootModel) }

                        // also add Platform to every extension except YPlatformExt modules
                        if (yModuleDescriptor !is YPlatformExtModuleDescriptor) {
                            addModuleDependency(allModules, platformIdeaModuleName, rootModel)
                        }
                    }
            }

        processPlatformModulesDependencies(
            hybrisProjectDescriptor,
            platformModule,
            allModules,
            modifiableModelsProvider,
            modulesChosenForImport
        )
    }

    private fun processPlatformModulesDependencies(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        platformModule: Module,
        allModules: Map<String, Module>,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        modulesChosenForImport: List<ModuleDescriptor>
    ) {
        val platformRootModel = modifiableModelsProvider.getModifiableRootModel(platformModule)

        modulesChosenForImport
            .filterIsInstance<YPlatformExtModuleDescriptor>()
            .forEach { dependency -> addModuleDependency(allModules, dependency.ideaModuleName(), platformRootModel) }

        hybrisProjectDescriptor.configHybrisModuleDescriptor
            ?.let { addModuleDependency(allModules, it.ideaModuleName(), platformRootModel) }
    }

    private fun addModuleDependency(
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
