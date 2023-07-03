/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.project.configurators.ModuleDependenciesConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YOotbRegularModuleDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.impl.YPlatformExtModuleDescriptor
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel

class DefaultModuleDependenciesConfigurator : ModuleDependenciesConfigurator {

    override fun configure(
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        indicator.text = HybrisI18NBundleUtils.message("hybris.project.import.dependencies")
        indicator.text2 = ""
        val modulesChosenForImport = hybrisProjectDescriptor.modulesChosenForImport
        val allModules = modifiableModelsProvider.modules
            .associateBy { it.name }
        val extModules = modulesChosenForImport.filterIsInstance<YPlatformExtModuleDescriptor>()
            .toSet()

        val platformIdeaModuleName = hybrisProjectDescriptor.platformHybrisModuleDescriptor.ideaModuleName()
        val platformModule = allModules[platformIdeaModuleName] ?: return

        modulesChosenForImport.forEach { moduleDescriptor ->
            allModules[moduleDescriptor.ideaModuleName()]
                ?.let { module ->
                    val rootModel = modifiableModelsProvider.getModifiableRootModel(module)

                    moduleDescriptor.getDirectDependencies()
                        .filterNot { moduleDescriptor is YOotbRegularModuleDescriptor && extModules.contains(it) }
                        .forEach { addModuleDependency(allModules, it.ideaModuleName(), rootModel) }
                }
        }

        processPlatformModulesDependencies(
            hybrisProjectDescriptor,
            platformModule,
            allModules,
            modifiableModelsProvider
        )
    }

    private fun processPlatformModulesDependencies(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        platformModule: Module,
        allModules: Map<String, Module>,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val platformRootModel = modifiableModelsProvider.getModifiableRootModel(platformModule)

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
