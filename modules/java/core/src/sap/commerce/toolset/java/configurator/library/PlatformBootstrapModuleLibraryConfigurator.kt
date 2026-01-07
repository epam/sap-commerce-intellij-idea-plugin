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
import com.intellij.platform.workspace.jps.entities.LibraryRoot
import com.intellij.platform.workspace.jps.entities.LibraryRoot.InclusionOptions
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YCoreExtModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

class PlatformBootstrapModuleLibraryConfigurator : ModuleLibraryConfigurator {

    override val name: String
        get() = JavaConstants.Library.PLATFORM_BOOTSTRAP

    override fun isApplicable(importContext: ProjectImportContext, moduleDescriptor: ModuleDescriptor) = moduleDescriptor is YCoreExtModuleDescriptor

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel,
        moduleDescriptor: ModuleDescriptor,
        moduleEntity: ModuleEntity
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val libraryRoots = buildList {
            moduleDescriptor.libraryDirectories
                .map { virtualFileUrlManager.fromPath(it.pathString) }
                .map { LibraryRoot(it, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY) }
                .forEach { add(it) }

            moduleDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.BOOTSTRAP_GEN_SRC)
                .takeIf { it.directoryExists }
                ?.let { virtualFileUrlManager.fromPath(it.pathString) }
                ?.let { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                ?.let { add(it) }

            importContext.sourceCodeFile
                ?.takeIf { it.fileExists }
                ?.let { virtualFileUrlManager.fromPath(it.pathString) }
                ?.let { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                ?.let { add(it) }
        }

        moduleEntity.addProjectLibrary(
            project = importContext.project,
            workspaceModel = workspaceModel,
            libraryName = JavaConstants.Library.PLATFORM_BOOTSTRAP,
            libraryRoots = libraryRoots,
        )
    }

    private val ModuleDescriptor.libraryDirectories
        get() = buildList {
            val moduleRootPath = this@libraryDirectories.moduleRootPath

            moduleRootPath.resolve(ProjectConstants.Directory.RESOURCES)
                .takeIf { it.directoryExists }
                ?.listDirectoryEntries()
                ?.filter { it.directoryExists }
                ?.forEach { resourcesInnerDirectory ->
                    add(resourcesInnerDirectory.resolve(ProjectConstants.Directory.LIB))
                    add(resourcesInnerDirectory.resolve(ProjectConstants.Directory.BIN))
                }

            add(moduleRootPath.resolve(ProjectConstants.Paths.BOOTSTRAP_BIN))
            add(moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_BIN))
            add(moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_6_BIN))
            add(moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_LIB))
            add(moduleRootPath.resolve(ProjectConstants.Paths.TOMCAT_6_LIB))
        }
            .filter { it.directoryExists }
}