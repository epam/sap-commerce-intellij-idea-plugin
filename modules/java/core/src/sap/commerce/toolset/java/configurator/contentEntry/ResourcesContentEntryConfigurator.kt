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

import com.intellij.platform.workspace.jps.entities.ContentRootEntityBuilder
import sap.commerce.toolset.java.configurator.contentEntry.util.addSourceRoots
import sap.commerce.toolset.java.configurator.contentEntry.util.resources
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.isCustomModuleDescriptor
import java.nio.file.Path

class ResourcesContentEntryConfigurator : ModuleContentEntryConfigurator {

    override val name: String
        get() = "Resources"

    override fun isApplicable(
        context: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor.isCustomModuleDescriptor || context.settings.importOOTBModulesInWriteMode

    override suspend fun configure(
        context: ProjectModuleConfigurationContext<ModuleDescriptor>,
        contentRootEntity: ContentRootEntityBuilder,
        pathsToIgnore: Collection<Path>
    ) {
        val moduleDescriptor = context.moduleDescriptor
        val resourcesPath = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)
        val relativeOutputPath = if (moduleDescriptor is YBackofficeSubModuleDescriptor) "cockpitng" else ""
        val rootEntities = resourcesPath
            .let { context.moduleEntity.resources(path = it, relativeOutputPath = relativeOutputPath) }
            .let { listOf(it) }

        contentRootEntity.addSourceRoots(
            context = context.importContext,
            virtualFileUrlManager = context.importContext.workspace.getVirtualFileUrlManager(),
            rootEntities = rootEntities,
            pathsToIgnore = pathsToIgnore,
        )
    }
}