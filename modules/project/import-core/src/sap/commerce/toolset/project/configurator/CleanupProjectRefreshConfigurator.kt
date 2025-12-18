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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import sap.commerce.toolset.project.facet.YFacet
import sap.commerce.toolset.project.refresh.ProjectRefreshContext

class CleanupProjectRefreshConfigurator : ProjectRefreshConfigurator {

    override val name: String
        get() = "Cleanup"

    override fun beforeRefresh(refreshContext: ProjectRefreshContext) {
        if (!refreshContext.removeOldProjectData && !refreshContext.removeExternalModules) return
        val project = refreshContext.project

        val moduleModel = ModuleManager.getInstance(project).getModifiableModel()
        val libraryModel = LibraryTablesRegistrar.getInstance().getLibraryTable(project).modifiableModel

        moduleModel.modules
            .filter {
                if (YFacet.get(it) != null) refreshContext.removeOldProjectData
                else refreshContext.removeExternalModules
            }
            .forEach { module ->
                val moduleLibraries = ModuleRootManager.getInstance(module)
                    .orderEntries
                    .filterIsInstance<LibraryOrderEntry>()
                    .mapNotNull { it.library }

                val moduleLibrariesNames = moduleLibraries.map { it.presentableName }
                thisLogger().debug("Disposing module '${module.name}' and linked libraries: $moduleLibrariesNames")

                moduleLibraries.forEach { libraryModel.removeLibrary(it) }
                moduleModel.disposeModule(module)
            }

        ApplicationManager.getApplication().runWriteAction {
            moduleModel.commit()
            libraryModel.commit()
        }
    }
}