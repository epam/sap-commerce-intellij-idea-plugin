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
import sap.commerce.toolset.java.configurator.library.util.compiled
import sap.commerce.toolset.java.configurator.library.util.configureLibrary
import sap.commerce.toolset.java.configurator.library.util.configureTestLibrary
import sap.commerce.toolset.java.configurator.library.util.linkProjectLibrary
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YHmcSubModuleDescriptor
import kotlin.io.path.Path

class HmcSubModuleLibraryConfigurator : ModuleLibraryConfigurator<YHmcSubModuleDescriptor> {

    override val name: String
        get() = "Hmc Libraries"

    override fun isApplicable(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor is YHmcSubModuleDescriptor

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YHmcSubModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        configureExtensionLibrary(workspaceModel, moduleDescriptor, moduleEntity)
        configureTestLibrary(workspaceModel, moduleDescriptor, moduleEntity)

        moduleEntity.linkProjectLibrary(
            workspaceModel = workspaceModel,
            libraryName = JavaConstants.ProjectLibrary.HMC,
            exported = false
        )
    }

    private suspend fun configureExtensionLibrary(
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YHmcSubModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val libraryRoots = moduleDescriptor.compiled(
            virtualFileUrlManager, Path(ProjectConstants.Directory.BIN)
        )

        moduleEntity.configureLibrary(
            workspaceModel = workspaceModel,
            libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.EXTENSION}",
            libraryRoots = libraryRoots
        )
    }
}
