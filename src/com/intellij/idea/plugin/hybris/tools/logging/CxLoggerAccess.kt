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
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionListener
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.TransactionMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionContext
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

private const val FETCH_LOGGERS_STATE_GROOVY_SCRIPT = """
    import de.hybris.platform.core.Registry
    import de.hybris.platform.hac.facade.HacLog4JFacade
    import java.util.stream.Collectors
    
    Registry.applicationContext.getBean("hacLog4JFacade", HacLog4JFacade.class).getLoggers().stream()
            .map { it -> it.name + " | " + it.parentName + " | " + it.effectiveLevel }
            .collect(Collectors.joining("\n"))
"""

@Service(Service.Level.PROJECT)
class CxLoggerAccess(private val project: Project, private val coroutineScope: CoroutineScope) : Disposable {
    private var fetching: Boolean = false
    val loggersCache = CxLoggersStorage()

    val canRefresh: Boolean
        get() = !fetching

    val cacheInitialized: Boolean
        get() = loggersCache.initialized

    init {
        with(project.messageBus.connect(this)) {
            subscribe(RemoteConnectionListener.TOPIC, object : RemoteConnectionListener {
                override fun onActiveHybrisConnectionChanged(remoteConnection: RemoteConnectionSettings) = refresh()

                override fun onActiveSolrConnectionChanged(remoteConnection: RemoteConnectionSettings) = refresh()
            })
        }
    }

    fun logger(loggerIdentifier: String): CxLoggerModel? = if (!cacheInitialized) null else loggersCache.get(loggerIdentifier)

    fun setLogger(loggerName: String, logLevel: LogLevel) {
        val server = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = LoggingExecutionContext(
            executionTitle = "Update Log Level Status for SAP Commerce [${server.shortenConnectionName()}]...",
            loggerName = loggerName,
            logLevel = logLevel
        )
        fetching = true
        LoggingExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
            updateCache(result.loggers)

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

    fun fetch() {
        val server = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val context = GroovyExecutionContext(
            content = FETCH_LOGGERS_STATE_GROOVY_SCRIPT,
            transactionMode = TransactionMode.ROLLBACK,
            executionTitle = "Fetching Loggers from SAP Commerce [${server.shortenConnectionName()}]..."
        )

        fetching = true

        GroovyExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
            val loggers = result.result
                ?.split("\n")
                ?.map { it.split(" | ") }
                ?.filter { it.size == 3 }
                ?.map { CxLoggerModel.of(it[0], it[2], if (it.size == 3) it[1] else null) }
                ?.distinctBy { it.name }
                ?.associateBy { it.name }

            updateCache(loggers)

            if (result.hasError) notify(NotificationType.ERROR, "Failed to fetch logger states") {
                "<p>${result.errorMessage}</p>"
                "<p>Server: ${server.shortenConnectionName()}</p>"
            }
            else notify(NotificationType.INFORMATION, "Loggers state is fetched.") {
                "<p>Server: ${server.shortenConnectionName()}</p>"
            }
        }
    }

    private fun updateCache(loggers: Map<String, CxLoggerModel>?) {
        coroutineScope.launch {

            loggersCache.update(loggers ?: emptyMap())

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(emptyList(), true)
            }

            fetching = false
        }
    }

    private fun refresh() {
        loggersCache.clear()

        coroutineScope.launch {
            fetching = true

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
        loggersCache.clear()
    }

    companion object {
        fun getInstance(project: Project): CxLoggerAccess = project.service()
    }
}