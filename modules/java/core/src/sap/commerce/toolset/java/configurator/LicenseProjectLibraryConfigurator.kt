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

import com.intellij.platform.backend.workspace.WorkspaceModel
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.java.configurator.library.util.compiledArchives
import sap.commerce.toolset.java.configurator.library.util.configureProjectLibrary
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.ProjectLibraryConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import kotlin.io.path.Path

class LicenseProjectLibraryConfigurator : ProjectLibraryConfigurator {

    override val name: String
        get() = JavaConstants.ProjectLibrary.PLATFORM_LICENSE

    override suspend fun configure(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ) {
        val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
        val configModuleDescriptor = importContext.configModuleDescriptor
        val libraryRoots = configModuleDescriptor.compiledArchives(
            virtualFileUrlManager, Path(ProjectConstants.Directory.LICENCE)
        )

        workspaceModel.configureProjectLibrary(
            project = importContext.project,
            libraryName = JavaConstants.ProjectLibrary.PLATFORM_LICENSE,
            libraryRoots = libraryRoots
        )
    }
}
