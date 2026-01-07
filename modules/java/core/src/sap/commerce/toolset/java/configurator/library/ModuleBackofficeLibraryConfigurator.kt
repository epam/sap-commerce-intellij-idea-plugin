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
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor

class ModuleBackofficeLibraryConfigurator : ModuleLibraryConfigurator {

    override val name: String
        get() = JavaConstants.Library.BACKOFFICE

    override fun isApplicable(importContext: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = moduleDescriptor is YOotbRegularModuleDescriptor
        && moduleDescriptor.name == EiConstants.Extension.BACK_OFFICE

    override suspend fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) {
        val workspaceModel = WorkspaceModel.getInstance(importContext.project)
//        val moduleEntity = workspaceModel.currentSnapshot
//            .resolve(ModuleId(module.name)) ?: return
//        backgroundWriteAction {
//            workspaceModel.updateProjectModel("Adding new module dependency") { storage ->
//                storage.modifyModuleEntity(moduleEntity) {
//                    val libraryId = LibraryId(
//                        JavaConstants.Library.BACKOFFICE,
//                        LibraryTableId.ProjectLibraryTableId
//                    )
//                    this.dependencies.add(
//                        LibraryDependency(libraryId, true, DependencyScope.COMPILE)
//                    )
//                }
//            }
//        }
    }
}