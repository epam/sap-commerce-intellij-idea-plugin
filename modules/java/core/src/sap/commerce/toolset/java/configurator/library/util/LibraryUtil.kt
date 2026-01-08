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

package sap.commerce.toolset.java.configurator.library.util

import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.DependencyScope
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.project.descriptor.YModuleDescriptor

internal suspend fun configureTestLibrary(
    workspaceModel: WorkspaceModel,
    moduleDescriptor: YModuleDescriptor,
    moduleEntity: ModuleEntity
) {
    val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
    val libraryRoots = buildList {
        addAll(moduleDescriptor.classes(virtualFileUrlManager))
        addAll(moduleDescriptor.resources(virtualFileUrlManager))
        addAll(moduleDescriptor.testSources(virtualFileUrlManager))
        addAll(moduleDescriptor.testClasses(virtualFileUrlManager))
        addAll(moduleDescriptor.lib(virtualFileUrlManager))
    }

    moduleEntity.configureLibrary(
        workspaceModel = workspaceModel,
        libraryName = "${moduleDescriptor.name} - ${JavaConstants.ModuleLibrary.EXTENSION_TEST}",
        scope = DependencyScope.TEST,
        libraryRoots = libraryRoots
    )
}
