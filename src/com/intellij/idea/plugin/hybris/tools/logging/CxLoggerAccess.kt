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
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.TransactionMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionContext
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.removeUserData
import kotlinx.coroutines.CoroutineScope

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

    fun set(loggerName: String, logLevel: LogLevel) {
        val server = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = LoggingExecutionContext(
            title = "Update Log Level Status for SAP Commerce [${server.shortenConnectionName()}]...",
            loggerName = loggerName,
            logLevel = logLevel
        )
        project.service<LoggingExecutionClient>().execute(context) { coroutineScope, result ->

        }

    }


    fun fetch() {
        val server = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = GroovyExecutionContext(
            content = FETCH_LOGGERS_STATE_GROOVY_SCRIPT,
            transactionMode = TransactionMode.ROLLBACK,
            title = "Fetching Loggers from SAP Commerce [${server.shortenConnectionName()}]..."
        )

        project.service<GroovyExecutionClient>().execute(context) { coroutineScope, result ->
            if (result.statusCode == 200) {
                result.result
                    ?.split("\n")
                    ?.map { it -> it.split(" | ") }
                    ?.filter { it.size == 3 }
                    ?.map { CxLoggerModel(it[0], it[2], it[1]) }
                    ?.associateBy { it.name }
                    .let { putUserData(KEY_LOGGERS_STATE, it) }

                notifySuccess()
            } else {
                removeUserData(KEY_LOGGERS_STATE)
                notifyError()
            }
        }

    }

    private fun notifyError() {
        val server = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        Notifications
            .create(
                NotificationType.ERROR,
                "Loading loggers from SAP Commerce...",
                """
                            <p>Failed to fetch logger states.</p>
                            <p>${server.shortenConnectionName()}</p>
                        """.trimIndent()
            )
            .hideAfter(5)
            .notify(project)
    }

    private fun notifySuccess() {
        val server = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        Notifications
            .create(
                NotificationType.INFORMATION,
                "Loading loggers from SAP Commerce...",
                """
                            <p>Loggers state is fetched.</p>
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