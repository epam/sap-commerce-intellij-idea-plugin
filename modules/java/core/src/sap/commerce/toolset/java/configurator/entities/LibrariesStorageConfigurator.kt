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
package sap.commerce.toolset.java.configurator.entities

import com.intellij.platform.workspace.jps.entities.getModuleLibraries
import com.intellij.platform.workspace.storage.MutableEntityStorage
import sap.commerce.toolset.project.configurator.ProjectStorageConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext

class LibrariesStorageConfigurator : ProjectStorageConfigurator {

    override val name: String
        get() = "Libraries"

    override fun configure(importContext: ProjectImportContext, storage: MutableEntityStorage) {
        importContext.mutableStorage.libraryEntities.forEach { (moduleEntity, libraryEntities) ->
            moduleEntity.getModuleLibraries(storage).forEach { storage.removeEntity(it) }
            libraryEntities.forEach { storage.addEntity(it) }
        }
    }
}
