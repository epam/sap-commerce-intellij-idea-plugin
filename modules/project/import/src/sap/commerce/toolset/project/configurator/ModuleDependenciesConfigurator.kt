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
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YPlatformExtModuleDescriptor

class ModuleDependenciesConfigurator : ProjectImportConfigurator {

    override fun configure(
        project: Project,
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>,
        rootProjectModifiableModel: ModifiableModuleModel,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        cache: ConfiguratorCache
    ) {
        indicator.text = i18n("hybris.project.import.dependencies")
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
        platformModule: com.intellij.openapi.module.Module,
        allModules: Map<String, com.intellij.openapi.module.Module>,
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