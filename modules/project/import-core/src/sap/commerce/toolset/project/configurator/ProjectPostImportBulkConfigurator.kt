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

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.util.application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectIndexed
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.context.ProjectImportContext
import sap.commerce.toolset.project.context.ProjectPostImportContext
import kotlin.time.measureTime

class ProjectPostImportBulkConfigurator : ProjectImportConfigurator {

    private val logger = thisLogger()

    override val name: String
        get() = "Post Import"

    override suspend fun configure(context: ProjectImportContext) {
        CoroutineScope(Dispatchers.Default).launch {
            context.workspace.eventLog.collectIndexed { index, value ->
                logger.info("Handling workspace event: $index")

                if (context.mutableStorage.committed) {
                    configure(ProjectPostImportContext.from(context, value.storageAfter))
                    cancel("post-import configurators started, do not listen for new events")
                }
            }
        }
    }

    private fun configure(context: ProjectPostImportContext) {
        val legacyWorkspace = IdeModifiableModelsProviderImpl(context.project)
        val edtActions = mutableListOf<() -> Unit>()

        // mostly background operations
        ProjectPostImportConfigurator.EP.extensionList.forEach { configurator ->
            runCatching {
                val duration = measureTime { configurator.configure(context, legacyWorkspace, edtActions) }
                logger.info("Post-configured project [${configurator.name} | $duration]")
            }
                .exceptionOrNull()
                ?.let { logger.warn("Post-configurator '${configurator.name}' error: ${it.message}", it) }
        }

        // Save legacy workspace (facets, javadocs, etc.)
        application.invokeAndWait {
            application.runWriteAction {
                edtActions.forEach { it.invoke() }
                legacyWorkspace.commit()
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            if (context.project.isDisposed) return@launch
            val postImportAsyncConfigurators = ProjectPostImportAsyncConfigurator.EP.extensionList

            withBackgroundProgress(context.project, "Applying post-import configurators...", true) {
                // async operations
                supervisorScope {
                    reportProgressScope(postImportAsyncConfigurators.size) { progressReporter ->
                        postImportAsyncConfigurators.map { configurator ->
                            async {
                                progressReporter.itemStep("Applying '${configurator.name}' configurator...") {
                                    runCatching {
                                        val duration = measureTime { configurator.configure(context) }
                                        logger.info("Post-configured async project [${configurator.name} | $duration]")
                                    }
                                        .exceptionOrNull()
                                        // TODO: cancel by DB
                                        // Control-flow exceptions (e.g. this class com.intellij.openapi.progress.CeProcessCanceledException) should never be logged.
                                        // Instead, these should have been rethrown if caught.
                                        ?.let { logger.warn("Post-configurator '${configurator.name}' error: ${it.message}", it) }
                                }
                            }
                        }
                            .awaitAll()
                    }
                }
            }

            notifyImportFinished(context.project, context.refresh)
        }
    }

    private fun notifyImportFinished(project: Project, refresh: Boolean) {
        val notificationContent = if (refresh) i18n("hybris.notification.project.refresh.finished.content")
        else i18n("hybris.notification.project.import.finished.content")
        val notificationTitle = if (refresh) i18n("hybris.notification.project.refresh.title")
        else i18n("hybris.notification.project.import.title")

        Notifications.create(NotificationType.INFORMATION, notificationTitle, notificationContent)
            .system(true)
            .notify(project)
    }
}