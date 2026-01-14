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

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.ModuleEntityBuilder
import com.intellij.platform.workspace.jps.entities.ModuleTypeId
import com.intellij.workspaceModel.ide.legacyBridge.LegacyBridgeJpsEntitySourceFactory
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import kotlin.io.path.pathString

interface ModuleProvider {

    val name: String
    val moduleTypeId: String

    suspend fun isApplicable(moduleDescriptor: ModuleDescriptor): Boolean

    suspend fun create(context: ProjectImportContext, moduleDescriptor: ModuleDescriptor): ModuleEntityBuilder {
        val project = context.project
        val ideaModuleParentPath = moduleDescriptor.ideaModuleFile(context).parent

        val baseModuleDir = context.workspace.getVirtualFileUrlManager().fromPath(ideaModuleParentPath.normalize().pathString)
        val entitySource = LegacyBridgeJpsEntitySourceFactory.getInstance(project).createEntitySourceForModule(
            baseModuleDir = baseModuleDir,
            externalSource = null
        )

        return ModuleEntity(
            name = moduleDescriptor.ideaModuleName(),
            dependencies = emptyList(),
            entitySource = entitySource
        ) {
            type = ModuleTypeId(moduleTypeId)
        }
    }

    companion object {
        val EP = ExtensionPointName.create<ModuleProvider>("sap.commerce.toolset.project.module.provider")
    }
}
