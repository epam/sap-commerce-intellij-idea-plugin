/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.configurator

import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryTableId
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.workspaceModel.ide.legacyBridge.findModule
import sap.commerce.toolset.project.context.ProjectRefreshContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.facet.YFacet

// TODO: maybe merge into storage save configurators, but only when I'll be ready to support pure "update"
class CleanupProjectRefreshConfigurator : ProjectRefreshConfigurator {

    override val name: String
        get() = "Cleanup"

    override suspend fun configure(context: ProjectRefreshContext) {
        if (!context.removeOldProjectData && !context.removeExternalModules) return

        backgroundWriteAction {
            context.workspace.updateProjectModel("Cleanup current project") { storage ->
                // remove project libraries
                storage.entities(LibraryEntity::class.java)
                    .filter { it.tableId == LibraryTableId.ProjectLibraryTableId }
                    .forEach { storage.removeEntity(it) }

                // remove modules
                storage.entities(ModuleEntity::class.java)
                    .filter { moduleEntity ->
                        moduleEntity.findModule(storage)
                            ?.let { YFacet.getState(it) }
                            ?.let {
                                it.type != ModuleDescriptorType.ECLIPSE
                                    && it.type != ModuleDescriptorType.MAVEN
                                    && it.type != ModuleDescriptorType.GRADLE
                            }
                            ?: context.removeExternalModules
                    }
                    .forEach { storage.removeEntity(it) }
            }
        }
    }
}