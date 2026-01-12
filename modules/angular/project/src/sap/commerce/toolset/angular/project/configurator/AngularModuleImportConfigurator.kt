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
package sap.commerce.toolset.angular.project.configurator

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import sap.commerce.toolset.angular.AngularConstants
import sap.commerce.toolset.project.configurator.ModuleImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.io.path.pathString

class AngularModuleImportConfigurator : ModuleImportConfigurator {

    override val name: String
        get() = "Angular Modules"

    override fun isApplicable(moduleTypeId: String) = AngularConstants.MODULE_TYPE_ID == moduleTypeId

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val contentRootUrl = virtualFileUrlManager.fromPath(moduleDescriptor.moduleRootPath.pathString)

        val contentRootEntity = ContentRootEntity(
            url = contentRootUrl,
            excludedPatterns = emptyList(),
            entitySource = moduleEntity.entitySource
        )

        importContext.mutableStorage.add(moduleEntity, contentRootEntity)
    }
}
