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

package sap.commerce.toolset.logging

import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.exec.CxRemoteLogExecClient
import sap.commerce.toolset.logging.exec.CxRemoteLogState
import sap.commerce.toolset.logging.exec.context.CxRemoteLogExecContext
import sap.commerce.toolset.logging.exec.context.CxRemoteLogExecResult
import sap.commerce.toolset.logging.exec.context.CxRemoteLogGroovyScriptExecResult
import sap.commerce.toolset.logging.exec.event.CxRemoteLogStateListener
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.settings.state.TransactionMode
import java.util.*

@Service(Service.Level.PROJECT)
class CxRemoteLogAccess(private val project: Project, private val coroutineScope: CoroutineScope) : Disposable {

    private var fetching: Boolean = false

    //map: key is HacConnectionSettingsState.UUID value is a Pair of CxRemoteLogState and HacConnectionSettingsState
    private val loggersStates = WeakHashMap<String, CxRemoteLogState>()

    val ready: Boolean
        get() = !fetching

    val stateInitialized: Boolean
        get() {
            val server = HacExecConnectionService.getInstance(project).activeConnection
            return state(server.uuid).initialized
        }

    init {
        with(project.messageBus.connect(this)) {
            subscribe(HacConnectionSettingsListener.TOPIC, object : HacConnectionSettingsListener {
                override fun onActive(connection: HacConnectionSettingsState) = refresh()
                override fun onUpdate(settings: Collection<HacConnectionSettingsState>) = settings.forEach { clearState(it) }
                override fun onSave(settings: Collection<HacConnectionSettingsState>) = settings.forEach { clearState(it) }
                override fun onDelete(connection: HacConnectionSettingsState) {
                    loggersStates.remove(connection.uuid)
                }
            })
        }
    }

    fun logger(loggerIdentifier: String) = if (stateInitialized) {
        val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
        state(activeConnection.uuid).get(loggerIdentifier)
    } else null

    fun setLogger(loggerName: String, logLevel: CxLogLevel, callback: (CoroutineScope, CxRemoteLogExecResult) -> Unit = { _, _ -> }) {
        val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
        val context = CxRemoteLogExecContext(
            connection = activeConnection,
            executionTitle = "Update Log Level Status for SAP Commerce [${activeConnection.shortenConnectionName}]...",
            loggerName = loggerName,
            logLevel = logLevel,
            timeout = activeConnection.timeout,
        )
        fetching = true

        CxRemoteLogExecClient.getInstance(project).execute(context) { coroutineScope, execResult ->
            updateState(execResult.loggers, activeConnection)
            callback.invoke(coroutineScope, execResult)

            if (execResult.hasError) notify(NotificationType.ERROR, "Failed To Update Log Level") {
                """
                <p>${execResult.errorMessage}</p>
                <p>Server: ${activeConnection.shortenConnectionName}</p>
            """.trimIndent()
            }
            else notify(NotificationType.INFORMATION, "Log Level Updated") {
                """
                <p>Level : $logLevel</p>
                <p>Logger: $loggerName</p>
                <p>Server: ${activeConnection.shortenConnectionName}</p>
            """.trimIndent()
            }
        }
    }

    fun fetch() = fetch(HacExecConnectionService.getInstance(project).activeConnection)

    fun fetch(server: HacConnectionSettingsState) {
        fetching = true

        coroutineScope.launch {
            val scriptContent = readAction { ExtensionsService.getInstance().findResource(CxLogConstants.EXTENSION_STATE_SCRIPT) }

            val context = GroovyExecContext(
                connection = server,
                executionTitle = "Fetching Loggers from SAP Commerce [${server.shortenConnectionName}]...",
                content = scriptContent,
                transactionMode = TransactionMode.ROLLBACK,
                timeout = server.timeout,
            )

            executeLoggersGroovyScript(context, server) { _, groovyScriptResult ->
                val result = groovyScriptResult.result
                val loggers = groovyScriptResult.loggers

                when {
                    result.hasError -> notify(NotificationType.ERROR, "Failed to retrieve loggers state") {
                        """
                                <p>${result.errorMessage}</p>
                                <p>Server: ${server.shortenConnectionName}</p>
                            """.trimIndent()
                    }

                    loggers == null -> notify(NotificationType.WARNING, "Unable to retrieve loggers state") {
                        """
                                <p>No Loggers information returned from the remote server or is in the incorrect format.</p>
                                <p>Server: ${server.shortenConnectionName}</p>
                            """.trimIndent()
                    }

                    else -> notify(NotificationType.INFORMATION, "Loggers state is fetched.") {
                        """
                                <p>Declared loggers: ${loggers.size}</p>
                                <p>Server: ${server.shortenConnectionName}</p>
                            """.trimIndent()
                    }
                }

            }
        }
    }

    fun setLoggers(loggers: List<CxLoggerPresentation>, callback: (CoroutineScope, DefaultExecResult) -> Unit = { _, _ -> }) {
        val groovyScriptContent = loggers.joinToString(",\n") {
            """
                "${it.name}" : "${it.level}"
            """.trimIndent()
        }
            .let { ExtensionsService.getInstance().findResource(CxLogConstants.UPDATE_CX_LOGGERS_STATE).replace("[loggersMapToBeReplacedPlaceholder]", it) }

        val server = HacExecConnectionService.getInstance(project).activeConnection
        val context = GroovyExecContext(
            connection = server,
            executionTitle = "Applying the Loggers Template for SAP Commerce [${server.shortenConnectionName}]...",
            content = groovyScriptContent,
            transactionMode = TransactionMode.ROLLBACK,
            timeout = server.timeout,
        )

        executeLoggersGroovyScript(
            context,
            server
        ) { _, groovyScriptResult ->

            callback.invoke(coroutineScope, groovyScriptResult.result)

            val result = groovyScriptResult.result
            val loggers = groovyScriptResult.loggers

            when {
                result.hasError -> notify(NotificationType.ERROR, "Failed to apply the loggers template") {
                    "<p>${result.errorMessage}</p>"
                    "<p>Server: ${server.shortenConnectionName}</p>"
                }

                loggers == null -> notify(NotificationType.WARNING, "Unable to apply the loggers template") {
                    "<p>No Loggers information returned from the remote server or is in the incorrect format.</p>" +
                        "<p>Server: ${server.shortenConnectionName}</p>"
                }

                else -> notify(NotificationType.INFORMATION, "The logger template is applied.") {
                    """
                        <p>Declared loggers: ${loggers.size}</p>
                        <p>Server: ${server.shortenConnectionName}</p>
                    """.trimIndent()
                }
            }

        }
    }

    private fun executeLoggersGroovyScript(
        context: GroovyExecContext, server: HacConnectionSettingsState,
        callback: (CoroutineScope, CxRemoteLogGroovyScriptExecResult) -> Unit = { _, _ -> }
    ) {
        GroovyExecClient.getInstance(project).execute(context) { coroutineScope, execResult ->
            coroutineScope.launch {
                val loggers = execResult.result
                    ?.split("\n")
                    ?.map { it.split(" | ") }
                    ?.filter { it.size == 3 }
                    ?.map {
                        val loggerIdentifier = it[0]
                        val effectiveLevel = it[1]
                        val parentName = it[2]

                        CxLoggerPresentation.of(loggerIdentifier, effectiveLevel, parentName, false)
                    }
                    ?.distinctBy { it.name }
                    ?.associateBy { it.name }
                    ?.takeIf { it.isNotEmpty() }

                if (loggers == null || execResult.hasError) {
                    clearState(server)
                    project.messageBus.syncPublisher(CxRemoteLogStateListener.TOPIC).onLoggersStateChanged(server)
                } else {
                    updateState(loggers, server)
                }

                callback.invoke(coroutineScope, CxRemoteLogGroovyScriptExecResult(loggers, execResult))
            }
        }
    }

    fun state(settingsUUID: String): CxRemoteLogState = loggersStates
        .computeIfAbsent(settingsUUID) { CxRemoteLogState() }

    private fun updateState(loggers: Map<String, CxLoggerPresentation>?, activeConnection: HacConnectionSettingsState) {
        coroutineScope.launch {

            state(activeConnection.uuid).update(loggers ?: emptyMap())

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(emptyList(), true)
            }

            fetching = false

            project.messageBus.syncPublisher(CxRemoteLogStateListener.TOPIC).onLoggersStateChanged(activeConnection)
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

    private fun clearState(server: HacConnectionSettingsState) {
        val logState = loggersStates[server.uuid]
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
        fun getInstance(project: Project): CxRemoteLogAccess = project.service()
    }
}

