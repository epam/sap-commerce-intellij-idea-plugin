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
package sap.commerce.toolset.project.configurator

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.checkCanceled
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.util.progress.reportProgressScope
import sap.commerce.toolset.project.context.ProjectImportContext
import kotlin.time.measureTimedValue

class ProjectLibrariesConfigurator : ProjectPreImportConfigurator {

    private val logger = thisLogger()

    override val name: String
        get() = "Project Libraries"

    override suspend fun preConfigure(importContext: ProjectImportContext, workspaceModel: WorkspaceModel) {
        val configurators = ProjectLibraryConfigurator.EP.extensionList

        val projectLibraryEntities = reportProgressScope(configurators.size) { reporter ->
            configurators.mapNotNull { configurator ->
                reporter.itemStep("Applying project library '${configurator.name}' configurator...") {
                    checkCanceled()

                    val timedValue = measureTimedValue { configurator.configure(importContext, workspaceModel) }
                    logger.info("Library configurator [${configurator.name} | ${timedValue.duration}]")
                    return@itemStep timedValue.value
                }
            }
        }

        workspaceModel.update("Configuring project libraries") { storage ->
            projectLibraryEntities.forEach {
                storage.addEntity(it)
            }
        }
    }
}
