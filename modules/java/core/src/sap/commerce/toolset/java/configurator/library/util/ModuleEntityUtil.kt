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
import com.intellij.workspaceModel.ide.legacyBridge.LegacyBridgeJpsEntitySourceFactory

internal suspend fun removeProjectLibrary(
    workspaceModel: WorkspaceModel,
    libraryName: String
) = workspaceModel.update("Removing project library '$libraryName'") { storage ->
    val libraryEntity = storage.projectLibraries.find { it.name == libraryName }
        ?: return@update

    storage.removeEntity(libraryEntity)
}

internal suspend fun ModuleEntity.configureProjectLibrary(
    project: Project,
    workspaceModel: WorkspaceModel,
    libraryName: String,
    scope: DependencyScope = DependencyScope.COMPILE,
    exported: Boolean = true,
    libraryRoots: Collection<LibraryRoot>,
) = workspaceModel.update("Add library $libraryName to module ${this.name}") { storage ->
    val libraryId = LibraryId(
        name = libraryName,
        tableId = LibraryTableId.ProjectLibraryTableId,
    )
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

    storage.modifyModuleEntity(this@configureProjectLibrary) {
        this.dependencies += LibraryDependency(libraryId, exported, scope)
    }
}

internal suspend fun ModuleEntity.configureLibrary(
    workspaceModel: WorkspaceModel,
    libraryName: String,
    scope: DependencyScope = DependencyScope.COMPILE,
    exported: Boolean = true,
    libraryRoots: Collection<LibraryRoot>,
) {
    if (libraryRoots.isEmpty()) {
        thisLogger().debug("No library roots for: $libraryName")
        return
    }

    workspaceModel.update("Add library $libraryName to module ${this.name}") { storage ->
        val moduleId = ModuleId(this.name)
        val libraryTableId = LibraryTableId.ModuleLibraryTableId(moduleId)
        val libraryId = LibraryId(libraryName, libraryTableId)
        val libraryEntity = this@configureLibrary.getModuleLibraries(storage)
            .find { it.name == libraryName }
            ?: storage.addEntity(
                LibraryEntity(
                    name = libraryName,
                    tableId = libraryTableId,
                    roots = emptyList(),
                    entitySource = this.entitySource,
                )
            )

        storage.modifyLibraryEntity(libraryEntity) {
            this.roots.clear()
            this.roots.addAll(libraryRoots)
        }

        storage.modifyModuleEntity(this@configureLibrary) {
            this.dependencies += LibraryDependency(libraryId, exported, scope)
        }
    }
}
