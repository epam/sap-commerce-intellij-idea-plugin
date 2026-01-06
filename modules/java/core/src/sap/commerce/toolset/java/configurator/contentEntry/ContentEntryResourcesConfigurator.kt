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

import com.intellij.openapi.roots.ContentEntry
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YBackofficeSubModuleDescriptor
import java.nio.file.Path

class ContentEntryResourcesConfigurator : ModuleContentEntryConfigurator {

    override val name: String
        get() = "Resources"

    override fun isApplicable(importContext: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = moduleDescriptor.isCustomModuleDescriptor
        || importContext.settings.importOOTBModulesInWriteMode

    override fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        contentEntry: ContentEntry,
        pathsToIgnore: Collection<Path>
    ) {
        val resourcesPath = moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)
        val properties = if (moduleDescriptor is YBackofficeSubModuleDescriptor) JpsJavaExtensionService.getInstance().createResourceRootProperties("cockpitng", false)
        else JavaResourceRootType.RESOURCE.createDefaultProperties()

        contentEntry.addSourceRoots(
            importContext,
            listOf(resourcesPath),
            pathsToIgnore,
            JavaResourceRootType.RESOURCE,
            properties,
        )
    }
}