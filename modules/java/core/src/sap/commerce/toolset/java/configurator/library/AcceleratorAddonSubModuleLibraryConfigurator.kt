/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.java.configurator.library

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.*
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YAcceleratorAddonSubModuleDescriptor

class AcceleratorAddonSubModuleLibraryConfigurator : ModuleLibraryConfigurator<YAcceleratorAddonSubModuleDescriptor> {

    override val name: String
        get() = "Accelerator Addon Libraries"

    override fun isApplicable(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor is YAcceleratorAddonSubModuleDescriptor

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YAcceleratorAddonSubModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        configureExtensionLibrary(importContext, workspaceModel, moduleDescriptor, moduleEntity)
        configureTestLibrary(workspaceModel, moduleDescriptor, moduleEntity)
        configureAddonLibrary(importContext, workspaceModel, moduleDescriptor, moduleEntity)
        configureAddonTestLibrary(importContext, workspaceModel, moduleDescriptor, moduleEntity)
    }

    private suspend fun configureExtensionLibrary(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YAcceleratorAddonSubModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val libraryRoots = buildList {
            addAll(moduleDescriptor.serverJarFiles(virtualFileUrlManager))
            addAll(moduleDescriptor.sources(virtualFileUrlManager))
            addAll(moduleDescriptor.resources(virtualFileUrlManager))
            addAll(moduleDescriptor.classes(virtualFileUrlManager))
            addAll(moduleDescriptor.docSources(importContext, virtualFileUrlManager))
            addAll(moduleDescriptor.lib(virtualFileUrlManager))
        }

        moduleEntity.configureLibrary(
            workspaceModel = workspaceModel,
            libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.EXTENSION}",
            libraryRoots = libraryRoots
        )
    }

    private suspend fun configureAddonLibrary(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YAcceleratorAddonSubModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val attachSources = moduleDescriptor.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode
        val libraryRoots = buildList {
            importContext.chosenHybrisModuleDescriptors
                .filterIsInstance<YModuleDescriptor>()
                .distinct()
                .associateBy { it.name }
                .values
                .filter { it.getDirectDependencies().contains(moduleDescriptor.owner) }
                .filter { it != this }
                .forEach { yModule ->
                    // process owner extension dependencies

                    addAll(yModule.classes(virtualFileUrlManager))
                    addAll(yModule.resources(virtualFileUrlManager))

                    if (attachSources) {
                        addAll(yModule.sources(virtualFileUrlManager))
                    }
                }
        }

        moduleEntity.configureLibrary(
            workspaceModel = workspaceModel,
            libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.ADDON}",
            exported = false,
            libraryRoots = libraryRoots,
        )
    }

    private suspend fun configureAddonTestLibrary(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YAcceleratorAddonSubModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val attachSources = moduleDescriptor.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode
        val libraryRoots = buildList {
            importContext.chosenHybrisModuleDescriptors
                .filterIsInstance<YModuleDescriptor>()
                .distinct()
                .associateBy { it.name }
                .values
                .filter { it.getDirectDependencies().contains(moduleDescriptor.owner) }
                .filter { it != this }
                .forEach { yModule ->
                    // process owner extension dependencies

                    addAll(yModule.classes(virtualFileUrlManager))
                    addAll(yModule.testClasses(virtualFileUrlManager))
                    addAll(yModule.resources(virtualFileUrlManager))

                    if (attachSources) {
                        addAll(yModule.testSources(virtualFileUrlManager))
                    }
                }
        }

        moduleEntity.configureLibrary(
            workspaceModel = workspaceModel,
            libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.ADDON_TEST}",
            exported = false,
            libraryRoots = libraryRoots,
        )
    }
}
