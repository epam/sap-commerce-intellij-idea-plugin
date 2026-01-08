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
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.*
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.YRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor

class BackofficeSubModuleLibraryConfigurator : ModuleLibraryConfigurator<YRegularModuleDescriptor> {

    override val name: String
        get() = "Backoffice Sub Module"

    override fun isApplicable(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor is YRegularModuleDescriptor && moduleDescriptor.extensionInfo.backofficeModule

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YRegularModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val backofficeSubModuleDescriptor = moduleDescriptor.getSubModules()
            .firstOrNull { it is YBackofficeSubModuleDescriptor }
            ?: return
        val attachSources = moduleDescriptor.type == ModuleDescriptorType.CUSTOM || importContext.settings.importOOTBModulesInWriteMode

        configureLibrary(workspaceModel, moduleDescriptor, moduleEntity, JavaConstants.ModuleLibrary.BACKOFFICE) {
            buildList {
                addAll(backofficeSubModuleDescriptor.classes(it))
                addAll(backofficeSubModuleDescriptor.resources(it))

                if (attachSources) addAll(backofficeSubModuleDescriptor.sources(it))
            }
        }

        configureLibrary(workspaceModel, moduleDescriptor, moduleEntity, JavaConstants.ModuleLibrary.BACKOFFICE_TEST) {
            buildList {
                addAll(backofficeSubModuleDescriptor.classes(it))
                addAll(backofficeSubModuleDescriptor.testClasses(it))
                addAll(backofficeSubModuleDescriptor.resources(it))

                if (attachSources) addAll(backofficeSubModuleDescriptor.testSources(it))
            }
        }
    }

    private suspend fun configureLibrary(
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YRegularModuleDescriptor,
        moduleEntity: ModuleEntity,
        libraryNameSuffix: String,
        libraryRootsProvider: (VirtualFileUrlManager) -> Collection<LibraryRoot>
    ) = moduleEntity.configureLibrary(
        workspaceModel = workspaceModel,
        libraryName = "${moduleDescriptor.name} - $libraryNameSuffix",
        scope = DependencyScope.PROVIDED,
        exported = false,
        libraryRoots = libraryRootsProvider(workspaceModel.getVirtualFileUrlManager())
    )
}
