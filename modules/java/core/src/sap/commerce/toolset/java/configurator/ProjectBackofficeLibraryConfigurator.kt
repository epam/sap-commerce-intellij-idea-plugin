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

package sap.commerce.toolset.java.configurator

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.*
import com.intellij.platform.workspace.jps.entities.LibraryRoot.InclusionOptions
import com.intellij.workspaceModel.ide.legacyBridge.LegacyBridgeJpsEntitySourceFactory
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectPreImportConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor
import sap.commerce.toolset.util.directoryExists
import kotlin.io.path.pathString

class ProjectBackofficeLibraryConfigurator : ProjectPreImportConfigurator {

    override val name: String
        get() = JavaConstants.Library.BACKOFFICE

    override suspend fun preConfigure(importContext: ProjectImportContext) {
        val backofficeWebDescriptor = importContext.chosenHybrisModuleDescriptors
            .filterIsInstance<YWebSubModuleDescriptor>()
            .find { it.owner.name == EiConstants.Extension.BACK_OFFICE }

        if (backofficeWebDescriptor == null) {
            thisLogger().info("Backoffice Library will not be created because ${EiConstants.Extension.BACK_OFFICE} extension is not used.")
            return
        }

        val workspaceModel = WorkspaceModel.getInstance(importContext.project)
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()

        val libraryRoots = buildList {
            backofficeWebDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_CLASSES)
                .takeIf { it.directoryExists }
                ?.normalize()
                ?.let { virtualFileUrlManager.fromPath(it.pathString) }
                ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED) }
                ?.let { add(it) }
            backofficeWebDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.WEBROOT_WEB_INF_LIB)
                .takeIf { it.directoryExists }
                ?.normalize()
                ?.let { virtualFileUrlManager.fromPath(it.pathString) }
                ?.let { LibraryRoot(it, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY) }
                ?.let { add(it) }

            backofficeWebDescriptor.moduleRootPath.resolve(ProjectConstants.Paths.RELATIVE_DOC_SOURCES)
                .takeIf { it.directoryExists && importContext.settings.withStandardProvidedSources }
                ?.normalize()
                ?.let { virtualFileUrlManager.fromPath(it.pathString) }
                ?.let { LibraryRoot(it, LibraryRootTypeId.SOURCES) }
                ?.let { add(it) }

            importContext.chosenHybrisModuleDescriptors
                .asSequence()
                .filterIsInstance<YOotbRegularModuleDescriptor>()
                .filter { it.extensionInfo.backofficeModule }
                .map { it.moduleRootPath.resolve(ProjectConstants.Paths.BACKOFFICE_JAR) }
                .filter { it.directoryExists }
                .map { it.normalize() }
                .map { virtualFileUrlManager.fromPath(it.pathString) }
                .map { LibraryRoot(it, LibraryRootTypeId.COMPILED, InclusionOptions.ARCHIVES_UNDER_ROOT_RECURSIVELY) }
                .toList()
                .forEach { add(it) }
        }

        workspaceModel.update("Processing library ${JavaConstants.Library.BACKOFFICE}") { storage ->
            val libraryEntity = storage.projectLibraries
                .find { it.name == JavaConstants.Library.BACKOFFICE }
                ?: storage.addEntity(
                    LibraryEntity(
                        name = JavaConstants.Library.BACKOFFICE,
                        tableId = LibraryTableId.ProjectLibraryTableId,
                        roots = emptyList(),
                        entitySource = LegacyBridgeJpsEntitySourceFactory.getInstance(importContext.project)
                            .createEntitySourceForProjectLibrary(null),
                    )
                )

            storage.modifyLibraryEntity(libraryEntity) {
                this.roots.clear()
                this.roots.addAll(libraryRoots)
            }
        }
    }
}
