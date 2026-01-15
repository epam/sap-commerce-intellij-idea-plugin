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

package sap.commerce.toolset.java.configurator.library

import com.intellij.platform.workspace.jps.entities.DependencyScope
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.*
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectModuleConfigurationContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.isNonCustomModuleDescriptor
import sap.commerce.toolset.util.directoryExists

class TestModuleLibraryConfigurator : ModuleLibraryConfigurator<YModuleDescriptor> {

    override val name: String
        get() = "Test"

    override fun isApplicable(
        context: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor is YModuleDescriptor

    override suspend fun configure(context: ProjectModuleConfigurationContext<YModuleDescriptor>) {
        val importContext = context.importContext
        val moduleDescriptor = context.moduleDescriptor
        val moduleEntity = context.moduleEntity

        val virtualFileUrlManager = importContext.workspace.getVirtualFileUrlManager()
        val libraryRoots = buildList {
            addAll(moduleDescriptor.resources(virtualFileUrlManager))

            if (moduleDescriptor.isNonCustomModuleDescriptor) {
                if (importContext.settings.importOOTBModulesInReadOnlyMode) {
                    addAll(moduleDescriptor.classes(virtualFileUrlManager))
                    addAll(moduleDescriptor.testSources(virtualFileUrlManager))
                    addAll(moduleDescriptor.testClasses(virtualFileUrlManager))
                } else {
                    if (!moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Directory.TEST_SRC).directoryExists) {
                        addAll(moduleDescriptor.classes(virtualFileUrlManager))
                        addAll(moduleDescriptor.testClasses(virtualFileUrlManager))
                    }
                }
            }
        }
        val excludedRoots = buildList {
            addAll(moduleDescriptor.excludedResources(virtualFileUrlManager))
        }

        moduleEntity.configureLibrary(
            context = importContext,
            moduleDescriptor = moduleDescriptor,
            libraryNameSuffix = JavaConstants.ModuleLibrary.TEST,
            scope = DependencyScope.TEST,
            libraryRoots = libraryRoots,
            excludedRoots = excludedRoots,
        )
    }
}
