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
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import java.nio.file.Path

class ContentEntryExcludePlatformConfigurator : ModuleContentEntryConfigurator {

    override val name: String
        get() = "Platform (exclusion)"

    override fun isApplicable(importContext: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = moduleDescriptor is PlatformModuleDescriptor

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity,
        contentRootEntity: ContentRootEntityBuilder,
        pathsToIgnore: Collection<Path>
    ) {
        val moduleRootPath = moduleDescriptor.moduleRootPath
        val bootstrapPath = moduleRootPath.resolve(ProjectConstants.Directory.BOOTSTRAP)
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val excludePaths = listOf(
            bootstrapPath.resolve(ProjectConstants.Directory.GEN_SRC),
            bootstrapPath.resolve(ProjectConstants.Directory.MODEL_CLASSES),

            moduleRootPath.resolve(ProjectConstants.Directory.TOMCAT_6),
            moduleRootPath.resolve(ProjectConstants.Directory.TOMCAT)
        )

        contentRootEntity.excludeDirectories(importContext, virtualFileUrlManager, excludePaths)

        if ("apache-ant-*" !in contentRootEntity.excludedPatterns) {
            contentRootEntity.excludedPatterns += "apache-ant-*"
        }
    }
}