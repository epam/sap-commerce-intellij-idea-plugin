/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.configurator

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import sap.commerce.toolset.project.context.ModuleGroup
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YPlatformExtModuleDescriptor

class ModuleDependenciesConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Modules Dependencies"

    override fun configure(
        importContext: ProjectImportContext,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val allModules = modifiableModelsProvider.modules
            .associateBy { it.name }
        val extModules = importContext.chosenModuleDescriptors(ModuleGroup.HYBRIS)
            .filterIsInstance<YPlatformExtModuleDescriptor>()
            .toSet()

        val platformIdeaModuleName = importContext.platformModuleDescriptor.ideaModuleName()
        val platformModule = allModules[platformIdeaModuleName] ?: return

        importContext.chosenModuleDescriptors(ModuleGroup.HYBRIS).forEach { moduleDescriptor ->
            allModules[moduleDescriptor.ideaModuleName()]
                ?.let { module ->
                    val rootModel = modifiableModelsProvider.getModifiableRootModel(module)

                    moduleDescriptor.getDirectDependencies()
                        .filterNot { moduleDescriptor is YOotbRegularModuleDescriptor && extModules.contains(it) }
                        .forEach { addModuleDependency(allModules, it.ideaModuleName(), rootModel) }
                }
        }

        processPlatformModulesDependencies(
            importContext,
            platformModule,
            allModules,
            modifiableModelsProvider
        )
    }

    private fun processPlatformModulesDependencies(
        importContext: ProjectImportContext,
        platformModule: Module,
        allModules: Map<String, Module>,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val platformRootModel = modifiableModelsProvider.getModifiableRootModel(platformModule)
        val configModuleName = importContext.configModuleDescriptor.ideaModuleName()

        addModuleDependency(allModules, configModuleName, platformRootModel)
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