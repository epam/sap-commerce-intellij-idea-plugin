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

package com.intellij.idea.plugin.hybris.tools.logging

import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.TransactionMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionContext
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val FETCH_LOGGERS_STATE_GROOVY_SCRIPT = """
    import de.hybris.platform.core.Registry
    import de.hybris.platform.hac.facade.HacLog4JFacade
    import java.util.stream.Collectors
    
    Registry.applicationContext.getBean("hacLog4JFacade", HacLog4JFacade.class).getLoggers().stream()
            .map { it -> it.name + " | " + it.parentName + " | " + it.effectiveLevel }
            .collect(Collectors.joining("\n"))
"""

@Service(Service.Level.PROJECT)
class CxLoggerAccess(private val project: Project, private val coroutineScope: CoroutineScope) : UserDataHolderBase() {
    private var fetching: Boolean = false
    val loggers
        get() = getUserData(KEY_LOGGERS_STATE)

    val canRefresh: Boolean
        get() = !fetching

    fun logger(loggerIdentifier: String) = loggers?.get(loggerIdentifier)

    fun setLogger(loggerName: String, logLevel: LogLevel) {
        val server = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = LoggingExecutionContext(
            title = "Update Log Level Status for SAP Commerce [${server.shortenConnectionName()}]...",
            loggerName = loggerName,
            logLevel = logLevel
        )
        fetching = true
        project.service<LoggingExecutionClient>().execute(context) { coroutineScope, result ->
            val loggers = result.result

            if (loggers != null) {
                updateCache(
                    loggers
                        .distinctBy { it.name }
                        .associateBy { it.name }
                )
                notify(NotificationType.INFORMATION, "Log Level Updated", logLevel, loggerName, server)
            } else {
                updateCache(null)
                notify(NotificationType.ERROR, "Failed To Update Log Level", logLevel, loggerName, server)
            }
        }
    }

    fun fetch() {
        val server = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = GroovyExecutionContext(
            content = FETCH_LOGGERS_STATE_GROOVY_SCRIPT,
            transactionMode = TransactionMode.ROLLBACK,
            title = "Fetching Loggers from SAP Commerce [${server.shortenConnectionName()}]..."
        )
        fetching = true

        project.service<GroovyExecutionClient>().execute(context) { coroutineScope, result ->
            if (result.statusCode == 200) {
                result.result
                    ?.split("\n")
                    ?.map { it -> it.split(" | ") }
                    ?.filter { it.size == 3 }
                    ?.map { CxLoggerModel(it[0], it[2], if (it.size == 3) it[1] else null) }
                    ?.distinctBy { it.name }
                    ?.associateBy { it.name }
                    .let { updateCache(it) }

                notify(
                    NotificationType.INFORMATION,
                    "Loggers state is fetched.",
                    server
                )
            } else {
                updateCache(null)
                notify(
                    NotificationType.ERROR,
                    "Failed to fetch logger states",
                    server
                )
            }
        }

    }

    private fun updateCache(map: Map<String, CxLoggerModel>?) {
        coroutineScope.launch {
            putUserData(KEY_LOGGERS_STATE, map)

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(emptyList(), true)
            }
            fetching = false
        }
    }

    private fun notify(type: NotificationType, title: String, server: RemoteConnectionSettings) {
        Notifications
            .create(type, title, "<p>Server: ${server.shortenConnectionName()}</p>")
            .hideAfter(5)
            .notify(project)
    }

    private fun notify(
        type: NotificationType,
        title: String,
        logLevel: LogLevel,
        loggerName: String,
        server: RemoteConnectionSettings
    ) {
        Notifications.create(
            type,
            title,
            """
                <p>Level  : $logLevel</p>
                <p>Logger : $loggerName</p>
                <p>${server.shortenConnectionName()}</p>
            """.trimIndent()
        )
            .hideAfter(5)
            .notify(project)
    }

    companion object {
        fun getInstance(project: Project): CxLoggerAccess = project.getService(CxLoggerAccess::class.java)
        private val KEY_LOGGERS_STATE = Key.create<Map<String, CxLoggerModel>>("flexibleSearch.parameters.key")
    }
}