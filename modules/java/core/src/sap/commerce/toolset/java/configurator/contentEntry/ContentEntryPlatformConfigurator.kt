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
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.PlatformModuleDescriptor
import java.nio.file.Path

class ContentEntryPlatformConfigurator : ModuleContentEntryConfigurator {

    override val name: String
        get() = "Platform"

    override fun isApplicable(importContext: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = moduleDescriptor is PlatformModuleDescriptor

    override fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        contentEntry: ContentEntry,
        pathsToIgnore: Collection<Path>
    ) {
        val moduleRootPath = moduleDescriptor.moduleRootPath
        val bootstrapPath = moduleRootPath.resolve(ProjectConstants.Directory.BOOTSTRAP)

        bootstrapPath.resolve(ProjectConstants.Directory.RESOURCES)
            .let { VfsUtil.findFile(it, true) }
            ?.let { contentEntry.addSourceFolder(it, JavaResourceRootType.RESOURCE) }

        // Only when bootstrap gensrc registered as source folder we can properly build the Class Hierarchy
        val genSrcPath = bootstrapPath.resolve(ProjectConstants.Directory.GEN_SRC)
        contentEntry.addSourceRoots(
            importContext,
            listOf(genSrcPath),
            pathsToIgnore,
            JavaSourceRootType.SOURCE,
            JpsJavaExtensionService.getInstance().createSourceRootProperties("", true),
        )
    }
}