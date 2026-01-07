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
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor

abstract class ExternalModuleConfigurator : ProjectPostImportAsyncConfigurator {

    abstract val moduleTypeId: String

    abstract suspend fun import(importContext: ProjectImportContext, workspaceModel: WorkspaceModel): Map<ModuleDescriptor, ModuleEntity>

    override suspend fun postImport(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        val descriptorToModule = import(importContext, workspaceModel)
            .takeIf { it.isNotEmpty() }
            ?: return

        val configurators = ModuleImportConfigurator.EP.extensionList
            .filter { it.isApplicable(moduleTypeId) }

        descriptorToModule.forEach { (moduleDescriptor, moduleEntity) ->
            configurators.forEach { configurator ->
                configurator.configure(importContext, workspaceModel, moduleDescriptor, moduleEntity)
            }
        }
    }

}