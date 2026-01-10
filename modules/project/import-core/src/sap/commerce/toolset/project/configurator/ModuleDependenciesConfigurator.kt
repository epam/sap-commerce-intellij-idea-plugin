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

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.*
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YPlatformExtModuleDescriptor

class ModuleDependenciesConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Modules Dependencies"

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ) {
        val allModules = workspaceModel.currentSnapshot
            .entities(ModuleEntity::class.java)
            .associateBy { it.name }
        val extModules = importContext.chosenHybrisModuleDescriptors
            .filterIsInstance<YPlatformExtModuleDescriptor>()
            .toSet()


        val moduleDependencies = buildMap {
            importContext.chosenHybrisModuleDescriptors.forEach { moduleDescriptor ->
                allModules[moduleDescriptor.ideaModuleName()]
                    ?.let { moduleEntity ->
                        moduleDescriptor.getDirectDependencies()
                            .filterNot { moduleDescriptor is YOotbRegularModuleDescriptor && extModules.contains(it) }
                            .forEach { addDependency(moduleEntity, it) }
                    }
            }

            allModules[importContext.platformModuleDescriptor.ideaModuleName()]
                ?.let { addDependency(it, importContext.configModuleDescriptor) }
        }

        workspaceModel.update("Update module dependencies") { storage ->
            moduleDependencies.forEach { (moduleEntity, dependencies) ->
                storage.modifyModuleEntity(moduleEntity) {
                    this.dependencies += dependencies
                }
            }
        }
    }

    private fun MutableMap<ModuleEntity, MutableList<ModuleDependency>>.addDependency(
        moduleEntity: ModuleEntity,
        descriptor: ModuleDescriptor
    ) {
        getOrPut(moduleEntity) { mutableListOf() }
            .add(moduleDependency(descriptor))
    }

    private fun moduleDependency(moduleDescriptor: ModuleDescriptor): ModuleDependency {
        val moduleDependency = ModuleDependency(
            module = ModuleId(moduleDescriptor.ideaModuleName()),
            exported = true,
            scope = DependencyScope.COMPILE,
            productionOnTest = false
        )
        return moduleDependency
    }
}
