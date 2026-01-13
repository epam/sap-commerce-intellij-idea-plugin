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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.LibraryEntityBuilder
import com.intellij.platform.workspace.storage.url.VirtualFileUrlManager
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.*
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectLibraryConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.descriptor.impl.YBackofficeModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YOotbRegularModuleDescriptor
import sap.commerce.toolset.project.descriptor.impl.YWebSubModuleDescriptor

class BackofficeProjectLibraryConfigurator : ProjectLibraryConfigurator {

    override val name: String
        get() = JavaConstants.ProjectLibrary.PLATFORM_BOOTSTRAP

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ): LibraryEntityBuilder? {
        val backofficeWebDescriptor = importContext.chosenHybrisModuleDescriptors
            .filterIsInstance<YWebSubModuleDescriptor>()
            .find { it.owner is YBackofficeModuleDescriptor }

        if (backofficeWebDescriptor == null) {
            thisLogger().info("Project library '${JavaConstants.ProjectLibrary.BACKOFFICE}' will not be created because ${EiConstants.Extension.BACK_OFFICE} extension is not used.")
            workspaceModel.removeProjectLibrary(JavaConstants.ProjectLibrary.BACKOFFICE)
            return null
        }

        val workspaceModel = WorkspaceModel.Companion.getInstance(importContext.project)
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val libraryRoots = buildList {
            addAll(backofficeWebDescriptor.webRootClasses(virtualFileUrlManager))
            addAll(backofficeWebDescriptor.webRootJars(virtualFileUrlManager))
            addAll(backofficeWebDescriptor.docSources(virtualFileUrlManager))

            addAll(importContext.backofficeJars(virtualFileUrlManager))
        }

        return importContext.project.configureProjectLibrary(
            libraryName = JavaConstants.ProjectLibrary.BACKOFFICE,
            libraryRoots = libraryRoots,
        )
    }

    private fun ProjectImportContext.backofficeJars(virtualFileUrlManager: VirtualFileUrlManager) = this.chosenHybrisModuleDescriptors
        .filterIsInstance<YOotbRegularModuleDescriptor>()
        .filter { it.extensionInfo.backofficeModule }
        .flatMap { it.compiledArchivesRecursively(virtualFileUrlManager, ProjectConstants.Paths.BACKOFFICE_JAR) }
}