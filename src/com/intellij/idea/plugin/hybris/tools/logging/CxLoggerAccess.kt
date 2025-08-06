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

import com.intellij.idea.plugin.hybris.extensions.ExtensionResource
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionListener
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.TransactionMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.CxLoggerUtilities
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.logging.LoggingExecutionContext
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Service(Service.Level.PROJECT)
class CxLoggerAccess(private val project: Project, private val coroutineScope: CoroutineScope) : Disposable {
    private var fetching: Boolean = false
    private val loggersStates = WeakHashMap<RemoteConnectionSettings, CxLoggersState>()
    val cxLoggerUtilities: CxLoggerUtilities = CxLoggerUtilities.getInstance(project)


    val ready: Boolean
        get() = !fetching

    val stateInitialized: Boolean
        get() {
            val server = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
            return loggers(server).initialized
        }

    init {
        with(project.messageBus.connect(this)) {
            subscribe(RemoteConnectionListener.TOPIC, object : RemoteConnectionListener {

                override fun onActiveHybrisConnectionChanged(remoteConnection: RemoteConnectionSettings) = refresh()

                override fun onHybrisConnectionModified(remoteConnection: RemoteConnectionSettings) = clearState(remoteConnection)
            })
        }
    }

    fun logger(loggerIdentifier: String): CxLoggerModel? {
        val server = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        return if (stateInitialized) loggers(server).get(loggerIdentifier) else null
    }

    fun setLogger(loggerName: String, logLevel: LogLevel) {
        val server = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = LoggingExecutionContext(
            executionTitle = "Update Log Level Status for SAP Commerce [${server.shortenConnectionName()}]...",
            loggerName = loggerName,
            logLevel = logLevel
        )
        fetching = true
        LoggingExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
            updateState(result.loggers, server)
            project.messageBus.syncPublisher(LoggersStateListener.TOPIC).onLoggersStateChanged(server)

            if (result.hasError) notify(NotificationType.ERROR, "Failed To Update Log Level") {
                """
                <p>${result.errorMessage}</p>
                <p>Server: ${server.shortenConnectionName()}</p>
            """.trimIndent()
            }
            else notify(NotificationType.INFORMATION, "Log Level Updated") {
                """
                <p>Level : $logLevel</p>
                <p>Logger: $loggerName</p>
                <p>Server: ${server.shortenConnectionName()}</p>
            """.trimIndent()
            }
        }
    }

    fun fetch() = fetch(RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris))

    fun fetch(server: RemoteConnectionSettings) {
        val context = GroovyExecutionContext(
            executionTitle = "Fetching Loggers from SAP Commerce [${server.shortenConnectionName()}]...",
            content = ExtensionResource.CX_LOGGERS_STATE.content,
            transactionMode = TransactionMode.ROLLBACK
        )

        fetching = true

        GroovyExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
            coroutineScope.launch {
                val loggers = result.result
                    ?.split("\n")
                    ?.map { it.split(" | ") }
                    ?.filter { it.size == 3 }
                    ?.map { CxLoggerModel.of(it[0], it[1], it[2], false, cxLoggerUtilities.getIcon(it[0])) }
                    ?.distinctBy { it.name }
                    ?.associateBy { it.name }
                    ?.takeIf { it.isNotEmpty() }

                if (loggers == null || result.hasError) {
                    clearState(server)
                } else {
                    updateState(loggers, server)
                }

                project.messageBus.syncPublisher(LoggersStateListener.TOPIC).onLoggersStateChanged(server)

                when {
                    result.hasError -> notify(NotificationType.ERROR, "Failed to retrieve loggers state") {
                        "<p>${result.errorMessage}</p>"
                        "<p>Server: ${server.shortenConnectionName()}</p>"
                    }

                    loggers == null -> notify(NotificationType.WARNING, "Unable to retrieve loggers state") {
                        "<p>No Loggers information returned from the remote server or is in the incorrect format.</p>"
                        "<p>Server: ${server.shortenConnectionName()}</p>"
                    }

                    else -> notify(NotificationType.INFORMATION, "Loggers state is fetched.") {
                        """
                    <p>Declared loggers: ${loggers.size}</p>
                    <p>Server: ${server.shortenConnectionName()}</p>
                """.trimIndent()
                    }
                }
            }
        }
    }

    fun loggers(settings: RemoteConnectionSettings): CxLoggersState {
        return loggersStates.computeIfAbsent(settings) { CxLoggersState() }
    }

    private fun updateState(loggers: Map<String, CxLoggerModel>?, settings: RemoteConnectionSettings) {
        coroutineScope.launch {

            loggers(settings).update(loggers ?: emptyMap())

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(emptyList(), true)
            }

            fetching = false
        }
    }

    private fun notify(type: NotificationType, title: String, contentProvider: () -> String) = Notifications
        .create(type, title, contentProvider.invoke())
        .hideAfter(5)
        .notify(project)

    override fun dispose() {
        loggersStates.forEach { it.value.clear() }
        loggersStates.clear()
    }

    private fun clearState(settings: RemoteConnectionSettings) {
        val logState = loggersStates[settings]
        logState?.clear()

        coroutineScope.launch {
            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(emptyList(), true)
            }
        }

        fetching = false
    }

    private fun refresh() {
        coroutineScope.launch {
            fetching = true

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(emptyList(), true)
            }

            fetching = false
        }
    }

    companion object {
        fun getInstance(project: Project): CxLoggerAccess = project.service()
    }
}