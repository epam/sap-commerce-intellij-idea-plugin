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
import com.intellij.platform.util.progress.reportProgressScope
import sap.commerce.toolset.project.context.ProjectImportContext
import kotlin.time.measureTimedValue

class ProjectLibrariesConfigurator : ProjectImportConfigurator {

    private val logger = thisLogger()

    override val name: String
        get() = "Project Libraries"

    override suspend fun configure(context: ProjectImportContext) {
        val configurators = ProjectLibraryConfigurator.EP.extensionList

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Applying project library '${configurator.name}' configurator...") {
                    checkCanceled()

                    val timedValue = measureTimedValue { configurator.configure(context) }
                    logger.info("Library configurator [${configurator.name} | ${timedValue.duration}]")
                    val libraryEntity = timedValue.value ?: return@itemStep

                    context.mutableStorage.add(libraryEntity)
                }
            }
        }
    }
}
