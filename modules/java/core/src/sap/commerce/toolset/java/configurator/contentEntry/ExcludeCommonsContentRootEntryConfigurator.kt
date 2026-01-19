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
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.java.configurator.contentEntry.util.excludeDirectories
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ModuleContentRootEntryConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.isNonCustomModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import java.nio.file.Path

class ExcludeCommonsContentRootEntryConfigurator : ModuleContentRootEntryConfigurator {

    override val name: String
        get() = "Common (exclusion)"

    override fun isApplicable(context: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = true

    override suspend fun configure(
        context: ProjectModuleConfigurationContext<ModuleDescriptor>,
        contentRootEntity: ContentRootEntityBuilder,
        pathsToIgnore: Collection<Path>
    ) {
        val moduleRootPath = context.moduleDescriptor.moduleRootPath
        val virtualFileUrlManager = context.importContext.workspace.getVirtualFileUrlManager()
        val excludePaths = buildList {
            add(moduleRootPath.resolve(HybrisConstants.EXTERNAL_TOOL_BUILDERS_DIRECTORY))
            add(moduleRootPath.resolve(HybrisConstants.SETTINGS_DIRECTORY))
            add(moduleRootPath.resolve(HybrisConstants.SPOCK_META_INF_SERVICES_DIRECTORY))
            add(moduleRootPath.resolve(ProjectConstants.Directory.NODE_MODULES))
            add(moduleRootPath.resolve(ProjectConstants.Directory.TEST_CLASSES))
            add(moduleRootPath.resolve(ProjectConstants.Directory.ECLIPSE_BIN))
            add(moduleRootPath.resolve(ProjectConstants.Directory.BOWER_COMPONENTS))
            add(moduleRootPath.resolve(ProjectConstants.Directory.JS_TARGET))

            moduleRootPath.resolve(ProjectConstants.Directory.APPS)
                .takeIf { context.moduleDescriptor.isNonCustomModuleDescriptor && context.importContext.settings.importOOTBModulesInReadOnlyMode }
                ?.takeIf { it.directoryExists }
                ?.let { add(it) }
        }

        contentRootEntity.excludeDirectories(context.importContext, virtualFileUrlManager, excludePaths)
    }
}