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
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import java.nio.file.Path

class ContentEntryExcludeCommonsConfigurator : ModuleContentEntryConfigurator {

    override val name: String
        get() = "Common (exclusion)"

    override fun isApplicable(importContext: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = true

    override fun configure(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor,
        contentEntry: ContentEntry,
        pathsToIgnore: Collection<Path>
    ) {
        val moduleRootPath = moduleDescriptor.moduleRootPath

        contentEntry.excludeDirectories(
            moduleRootPath.resolve(HybrisConstants.EXTERNAL_TOOL_BUILDERS_DIRECTORY),
            moduleRootPath.resolve(HybrisConstants.SETTINGS_DIRECTORY),
            moduleRootPath.resolve(HybrisConstants.SPOCK_META_INF_SERVICES_DIRECTORY),
            moduleRootPath.resolve(ProjectConstants.Directory.NODE_MODULES),
            moduleRootPath.resolve(ProjectConstants.Directory.TEST_CLASSES),
            moduleRootPath.resolve(ProjectConstants.Directory.ECLIPSE_BIN),
            moduleRootPath.resolve(ProjectConstants.Directory.BOWER_COMPONENTS),
            moduleRootPath.resolve(ProjectConstants.Directory.JS_TARGET),
        )
    }
}