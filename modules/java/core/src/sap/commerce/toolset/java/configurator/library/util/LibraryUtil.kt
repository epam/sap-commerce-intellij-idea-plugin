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

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.*
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.workspaceModel.ide.legacyBridge.LegacyBridgeJpsEntitySourceFactory
import sap.commerce.toolset.project.context.ProjectImportContext

internal suspend fun WorkspaceModel.removeProjectLibrary(
    libraryName: String
) = this.update("Removing project library '$libraryName'") { storage ->
    val libraryEntity = storage.projectLibraries.find { it.name == libraryName }
        ?: return@update

    storage.removeEntity(libraryEntity)
}

internal suspend fun WorkspaceModel.configureProjectLibrary(
    project: Project,
    libraryName: String,
    libraryRoots: Collection<LibraryRoot>,
) = this.update("Configure project library $libraryName") { storage ->
    val libraryEntity = storage.projectLibraries.find { it.name == libraryName }
        ?: storage.addEntity(
            LibraryEntity(
                name = libraryName,
                tableId = LibraryTableId.ProjectLibraryTableId,
                roots = emptyList(),
                entitySource = LegacyBridgeJpsEntitySourceFactory.getInstance(project)
                    .createEntitySourceForProjectLibrary(null),
            )
        )

    storage.modifyLibraryEntity(libraryEntity) {
        this.roots.clear()
        this.roots.addAll(libraryRoots)
    }
}

internal fun ModuleEntity.linkProjectLibrary(
    importContext: ProjectImportContext,
    libraryName: String,
    scope: DependencyScope = DependencyScope.COMPILE,
    exported: Boolean = true,
) {
    val libraryId = LibraryId(
        name = libraryName,
        tableId = LibraryTableId.ProjectLibraryTableId,
    )
    val libraryDependency = LibraryDependency(libraryId, exported, scope)

    importContext.mutableStorage.add(this, libraryDependency)
}

internal fun ModuleEntity.configureLibrary(
    importContext: ProjectImportContext,
    libraryName: String,
    scope: DependencyScope = DependencyScope.COMPILE,
    exported: Boolean = true,
    libraryRoots: Collection<LibraryRoot>,
    excludedRoots: Collection<VirtualFileUrl> = emptyList(),
) {
    if (libraryRoots.isEmpty()) {
        thisLogger().debug("No library roots for: $libraryName")
        return
    }

    val moduleId = ModuleId(this.name)
    val libraryTableId = LibraryTableId.ModuleLibraryTableId(moduleId)
    val libraryId = LibraryId(libraryName, libraryTableId)
    val libraryEntity = LibraryEntity(
        name = libraryName,
        tableId = libraryTableId,
        roots = libraryRoots.toList(),
        entitySource = this.entitySource,
    ) {
        this.excludedRoots = this.excludedRoots(excludedRoots)
    }

    val libraryDependency = LibraryDependency(libraryId, exported, scope)

    importContext.mutableStorage.add(this, libraryEntity)
    importContext.mutableStorage.add(this, libraryDependency)
}

private fun LibraryEntityBuilder.excludedRoots(
    excludedRoots: Collection<VirtualFileUrl>
): List<ExcludeUrlEntityBuilder> = excludedRoots.map {
    ExcludeUrlEntity(
        url = it,
        entitySource = this.entitySource,
    )
}
