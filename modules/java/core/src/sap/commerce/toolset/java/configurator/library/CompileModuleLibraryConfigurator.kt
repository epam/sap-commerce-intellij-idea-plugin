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

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.*
import sap.commerce.toolset.java.descriptor.isNonCustomModuleDescriptor
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.YModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCommonWebSubModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.util.directoryExists

class CompileModuleLibraryConfigurator : ModuleLibraryConfigurator<YModuleDescriptor> {

    override val name: String
        get() = "Compile"

    override fun isApplicable(
        importContext: ProjectImportContext,
        moduleDescriptor: ModuleDescriptor
    ) = moduleDescriptor is YModuleDescriptor

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: YModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val libraryRoots = buildList {
            addAll(moduleDescriptor.serverJarFiles(virtualFileUrlManager))
            addAll(moduleDescriptor.docSources(virtualFileUrlManager))
            addAll(moduleDescriptor.lib(virtualFileUrlManager))

            if (moduleDescriptor.isNonCustomModuleDescriptor) {
                when (moduleDescriptor) {
                    is YWebSubModuleDescriptor -> {
                        addAll(moduleDescriptor.webRootJars(virtualFileUrlManager))
                        addAll(moduleDescriptor.webRootClasses(virtualFileUrlManager))
                    }

                    is YCommonWebSubModuleDescriptor -> {
                        addAll(moduleDescriptor.webRootJars(virtualFileUrlManager))
                        addAll(moduleDescriptor.webRootClasses(virtualFileUrlManager))
                    }
                }

                if (importContext.settings.importOOTBModulesInReadOnlyMode) {
                    addAll(moduleDescriptor.classes(virtualFileUrlManager))
                    addAll(moduleDescriptor.resources(virtualFileUrlManager))
                    addAll(moduleDescriptor.sources(virtualFileUrlManager))

                    when (moduleDescriptor) {
                        is YWebSubModuleDescriptor -> {
                            // not applicable for backoffice, hac, hmc, applicable only to actual Accelerator Storefronts
                            addAll(moduleDescriptor.nestedSources(virtualFileUrlManager, ProjectConstants.Directory.ADDON_SRC))
                            addAll(moduleDescriptor.nestedSources(virtualFileUrlManager, ProjectConstants.Directory.COMMON_WEB_SRC))
                        }

                        is YCommonWebSubModuleDescriptor -> {
                            addAll(moduleDescriptor.dependantWebExtensions(virtualFileUrlManager))
                        }
                    }
                } else {
                    if (!moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Directory.SRC).directoryExists) {
                        addAll(moduleDescriptor.classes(virtualFileUrlManager))
                    }
                }
            }
        }

        moduleEntity.configureLibrary(
            importContext = importContext,
            libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.COMPILE}",
            libraryRoots = libraryRoots,
        )
    }

    private fun YCommonWebSubModuleDescriptor.dependantWebExtensions(virtualFileUrlManager: VirtualFileUrlManager) = this.dependantWebExtensions
        .flatMap { moduleDescriptor ->
            buildList {
                addAll(moduleDescriptor.webRootClasses(virtualFileUrlManager))
                addAll(moduleDescriptor.sources(virtualFileUrlManager))
            }
        }
}
