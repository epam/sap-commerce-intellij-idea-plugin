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

import com.intellij.platform.workspace.jps.entities.DependencyScope
import com.intellij.platform.workspace.jps.entities.ModuleDependency
import com.intellij.platform.workspace.jps.entities.ModuleId
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YPlatformExtModuleDescriptor

class ModulesDependenciesConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Modules Dependencies"

    override suspend fun configure(context: ProjectImportContext) {
        val modules = context.mutableStorage.modules
            .associateBy { it.name }
        val extModules = context.chosenHybrisModuleDescriptors
            .filterIsInstance<YPlatformExtModuleDescriptor>()
            .toSet()

        context.chosenHybrisModuleDescriptors.forEach { moduleDescriptor ->
            modules[moduleDescriptor.ideaModuleName()]
                ?.let { moduleEntity ->
                    moduleDescriptor.getDirectDependencies()
                        .filterNot { moduleDescriptor is YOotbRegularModuleDescriptor && extModules.contains(it) }
                        .forEach {
                            moduleEntity.dependencies += it.moduleDependency
                        }
                }
        }

        modules[context.platformModuleDescriptor.ideaModuleName()]
            ?.let {
                it.dependencies += context.configModuleDescriptor.moduleDependency
            }
    }

    private val ModuleDescriptor.moduleDependency
        get() = ModuleDependency(
            module = ModuleId(this.ideaModuleName()),
            exported = true,
            scope = DependencyScope.COMPILE,
            productionOnTest = false
        )
}
