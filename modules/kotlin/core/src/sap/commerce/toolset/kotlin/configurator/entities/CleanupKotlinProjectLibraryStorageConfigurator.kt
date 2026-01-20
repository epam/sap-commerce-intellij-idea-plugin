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
package sap.commerce.toolset.kotlin.configurator.entities

import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryTableId
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.entities
import org.jetbrains.kotlin.idea.projectConfiguration.JavaRuntimeLibraryDescription
import sap.commerce.toolset.project.configurator.ProjectStorageCleanupConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext

class CleanupKotlinProjectLibraryStorageConfigurator : ProjectStorageCleanupConfigurator {

    override val name: String
        get() = "Cleanup Kotlin Project Library"

    override fun configure(context: ProjectImportContext, storage: MutableEntityStorage) {
        val removableLibraries = setOf(
            JavaRuntimeLibraryDescription.LIBRARY_NAME,
        )
        storage.entities<LibraryEntity>()
            .filter { it.tableId == LibraryTableId.ProjectLibraryTableId }
            .filter { removableLibraries.contains(it.name) }
            .forEach { storage.removeEntity(it) }
    }
}
