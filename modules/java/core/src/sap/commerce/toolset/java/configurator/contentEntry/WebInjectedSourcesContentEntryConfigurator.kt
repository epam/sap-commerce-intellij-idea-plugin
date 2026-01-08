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
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

class WebInjectedSourcesContentEntryConfigurator : ModuleContentEntryConfigurator {

    override val name: String
        get() = "Web injected sources (addons, common web)"

    override fun isApplicable(importContext: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = moduleDescriptor is YWebSubModuleDescriptor
        && (moduleDescriptor.isCustomModuleDescriptor || importContext.settings.importOOTBModulesInWriteMode)

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity,
        contentRootEntity: ContentRootEntityBuilder,
        pathsToIgnore: Collection<Path>
    ) {
        val moduleRootPath = moduleDescriptor.moduleRootPath
        val rootEntities = listOf(
            moduleRootPath.resolve(ProjectConstants.Directory.COMMON_WEB_SRC),
            moduleRootPath.resolve(ProjectConstants.Directory.ADDON_SRC)
        )
            .filter { it.directoryExists }
            .flatMap { path ->
                Files.newDirectoryStream(path) { it.isDirectory() }.use { directoryStream ->
                    directoryStream.toList()
                }
            }
            .map { SourceRootEntityDescriptor.generatedSources(moduleEntity, it) }

        contentRootEntity.addSourceRoots(
            importContext = importContext,
            virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager(),
            rootEntities = rootEntities,
            pathsToIgnore = pathsToIgnore,
        )
    }
}