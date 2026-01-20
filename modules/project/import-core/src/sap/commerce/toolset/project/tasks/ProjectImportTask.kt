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

package sap.commerce.toolset.project.tasks

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.util.progress.reportSequentialProgress
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.configurator.ProjectImportConfigurator
import sap.commerce.toolset.project.configurator.ProjectStorageCleanupConfigurator
import sap.commerce.toolset.project.configurator.ProjectStorageSaveConfigurator
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectImportState
import sap.commerce.toolset.project.importState
import kotlin.time.measureTime

@Service(Service.Level.PROJECT)
class ProjectImportTask(private val project: Project) {

    private val logger = thisLogger()

    fun execute(context: ProjectImportContext) = runWithModalProgressBlocking(
        owner = ModalTaskOwner.guess(),
        title = if (context.refresh) i18n("hybris.project.refresh.commit")
        else i18n("hybris.project.import.commit"),
    ) {
        project.importState = ProjectImportState.IN_PROGRESS
        reportSequentialProgress { spr ->
            spr.nextStep(95) { importProject(context) }
            spr.nextStep(100) { saveWorkspace(context) }

            project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, true)
        }
    }

    private suspend fun importProject(context: ProjectImportContext) {
        val configurators = ProjectImportConfigurator.EP.extensionList

        reportProgressScope(configurators.size) { reporter ->
            configurators.forEach { configurator ->
                reporter.itemStep("Configuring project using '${configurator.name}' configurator...") {
                    checkCanceled()

                    val duration = measureTime { configurator.configure(context) }
                    logger.debug("Configured project [${configurator.name} | $duration]")
                }
            }
        }
    }

    private suspend fun saveWorkspace(context: ProjectImportContext) {
        context.workspace.update("Saving Workspace Model") { storage ->
            ProjectStorageCleanupConfigurator.EP.extensionList.forEach { configurator ->
                logger.debug("Cleaning workspace using ${configurator.name} configurator...")
                configurator.configure(context, storage)
            }

            ProjectStorageSaveConfigurator.EP.extensionList.forEach { configurator ->
                logger.debug("Saving workspace using ${configurator.name} configurator...")
                configurator.configure(context, storage)
            }

            context.mutableStorage.lock()
            project.importState = ProjectImportState.IMPORTED
        }
    }

    companion object {
        fun getInstance(project: Project): ProjectImportTask = project.service()
    }
}
