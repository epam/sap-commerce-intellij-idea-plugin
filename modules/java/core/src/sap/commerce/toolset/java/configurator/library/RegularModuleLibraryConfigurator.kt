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
import com.intellij.platform.workspace.jps.entities.DependencyScope
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.*
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor

class RegularModuleLibraryConfigurator : ModuleLibraryConfigurator<YRegularModuleDescriptor> {

    override val name: String
        get() = "Regular"

    override fun isApplicable(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor is YRegularModuleDescriptor

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YRegularModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        configureExtensionLibrary(importContext, workspaceModel, moduleDescriptor, moduleEntity)
        configureTestLibrary(workspaceModel, moduleDescriptor, moduleEntity)

        configureBackofficeLibrary(importContext, workspaceModel, moduleDescriptor, moduleEntity)
        configureBackofficeTestLibrary(importContext, workspaceModel, moduleDescriptor, moduleEntity)
    }

    private suspend fun configureExtensionLibrary(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YRegularModuleDescriptor,
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

    private suspend fun configureBackofficeLibrary(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YRegularModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        if (!moduleDescriptor.extensionInfo.backofficeModule) return

        val backofficeSubModuleDescriptor = moduleDescriptor.getSubModules()
            .firstOrNull { it is YBackofficeSubModuleDescriptor }
            ?: return
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val attachSources = moduleDescriptor.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode

        val libraryRoots = buildList {
            addAll(backofficeSubModuleDescriptor.classes(virtualFileUrlManager))
            addAll(backofficeSubModuleDescriptor.resources(virtualFileUrlManager))

            if (attachSources) {
                addAll(backofficeSubModuleDescriptor.sources(virtualFileUrlManager))
            }
        }

        moduleEntity.configureLibrary(
            workspaceModel = workspaceModel,
            libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.BACKOFFICE}",
            scope = DependencyScope.PROVIDED,
            exported = false,
            libraryRoots = libraryRoots
        )
    }

    private suspend fun configureBackofficeTestLibrary(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YRegularModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        if (!moduleDescriptor.extensionInfo.backofficeModule) return

        val backofficeSubModuleDescriptor = moduleDescriptor.getSubModules()
            .firstOrNull { it is YBackofficeSubModuleDescriptor }
            ?: return
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val attachSources = moduleDescriptor.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode

        val libraryRoots = buildList {
            addAll(backofficeSubModuleDescriptor.classes(virtualFileUrlManager))
            addAll(backofficeSubModuleDescriptor.testClasses(virtualFileUrlManager))
            addAll(backofficeSubModuleDescriptor.resources(virtualFileUrlManager))

            if (attachSources) {
                addAll(backofficeSubModuleDescriptor.testSources(virtualFileUrlManager))
            }
        }

        moduleEntity.configureLibrary(
            workspaceModel = workspaceModel,
            libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.BACKOFFICE_TEST}",
            scope = DependencyScope.PROVIDED,
            exported = false,
            libraryRoots = libraryRoots
        )
    }
}
