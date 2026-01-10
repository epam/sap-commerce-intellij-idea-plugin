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

package sap.commerce.toolset.java.configurator.contentEntry

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.ContentRootEntityBuilder
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import sap.commerce.toolset.java.descriptor.SourceRootEntityDescriptor
import sap.commerce.toolset.java.descriptor.isCustomModuleDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import java.nio.file.Path

class SourcesContentEntryConfigurator : ModuleContentEntryConfigurator {

    override val name: String
        get() = "Sources"

    override fun isApplicable(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor.isCustomModuleDescriptor || importContext.settings.importOOTBModulesInWriteMode

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity,
        contentRootEntity: ContentRootEntityBuilder,
        pathsToIgnore: Collection<Path>
    ) {
        val moduleRootPath = moduleDescriptor.moduleRootPath

        val rootEntities = buildList {
            ProjectConstants.Directory.SRC_DIR_NAMES
                .map { moduleRootPath.resolve(it) }
                .map { SourceRootEntityDescriptor.sources(moduleEntity = moduleEntity, path = it) }
                .forEach { add(it) }

            moduleRootPath.resolve(ProjectConstants.Directory.GEN_SRC)
                .let { SourceRootEntityDescriptor.generatedSources(moduleEntity = moduleEntity, path = it) }
                .also { add(it) }
        }

        contentRootEntity.addSourceRoots(
            importContext = importContext,
            virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager(),
            rootEntities = rootEntities,
            pathsToIgnore = pathsToIgnore,
        )
    }
}