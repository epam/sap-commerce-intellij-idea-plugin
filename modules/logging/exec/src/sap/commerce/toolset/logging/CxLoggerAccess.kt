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
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.settings.state.shortenConnectionName
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.exec.LoggingExecClient
import sap.commerce.toolset.logging.exec.context.LoggingExecContext
import sap.commerce.toolset.logging.exec.context.LoggingExecResult
import sap.commerce.toolset.logging.exec.event.CxLoggersStateListener
import sap.commerce.toolset.settings.state.TransactionMode
import java.util.*

@Service(Service.Level.PROJECT)
class CxLoggerAccess(private val project: Project, private val coroutineScope: CoroutineScope) : Disposable {

    private var fetching: Boolean = false

    //map: key is HacConnectionSettingsState.UUID value is a Pair of CxLoggersState and HacConnectionSettingsState
    private val loggersStates = WeakHashMap<String, CxLoggersState>()

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
                override fun onSave(settings: Map<ExecConnectionScope, List<HacConnectionSettingsState>>) = settings.values
                    .flatten()
                    .forEach { clearState(it.uuid) }

                override fun onRemoved(connection: HacConnectionSettingsState) {
                    loggersStates.remove(connection.uuid)
                }
            })
        }
    }

    fun logger(loggerIdentifier: String): CxLoggerModel? {
        return if (stateInitialized)
            state(HacExecConnectionService.getInstance(project).activeConnection.uuid).get(loggerIdentifier)
        else null
    }

    fun setLogger(loggerName: String, logLevel: LogLevel, callback: (CoroutineScope, LoggingExecResult) -> Unit = { _, _ -> }) {
        val activeConnection = HacExecConnectionService.getInstance(project).activeConnection
        val context = LoggingExecContext(
            connection = activeConnection,
            executionTitle = "Update Log Level Status for SAP Commerce [${activeConnection.shortenConnectionName}]...",
            loggerName = loggerName,
            logLevel = logLevel,
            timeout = activeConnection.timeout,
        )
        fetching = true
        LoggingExecClient.getInstance(project).execute(context) { coroutineScope, result ->
            updateState(result.loggers, activeConnection.uuid)
            callback.invoke(coroutineScope, result)

            project.messageBus.syncPublisher(CxLoggersStateListener.TOPIC).onLoggersStateChanged(activeConnection)

            if (result.hasError) notify(NotificationType.ERROR, "Failed To Update Log Level") {
                """
                <p>${result.errorMessage}</p>
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

        val context = GroovyExecContext(
            connection = server,
            executionTitle = "Fetching Loggers from SAP Commerce [${server.shortenConnectionName}]...",
            content = ExtensionsService.getInstance().findResource(CxLoggersConstants.EXTENSION_STATE_SCRIPT),
            settings = GroovyExecContext.defaultSettings(server).copy(
                transactionMode = TransactionMode.ROLLBACK,
                timeout = server.timeout,
            )
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

    fun setLoggers(loggers: List<CxLoggerModel>, callback: (CoroutineScope, DefaultExecResult) -> Unit = { _, _ -> }) {
        val groovyScriptContent = loggers.joinToString(",\n") {
            """
                "${it.name}" : "${it.level}"
            """.trimIndent()
        }
            .let { ExtensionsService.getInstance().findResource(CxLoggersConstants.UPDATE_CX_LOGGERS_STATE).replace("[loggersMapToBeReplacedPlaceholder]", it) }

        val server = HacExecConnectionService.getInstance(project).activeConnection
        val context = GroovyExecContext(
            connection = server,
            executionTitle = "Applying the Loggers Template for SAP Commerce [${server.shortenConnectionName}]...",
            content = groovyScriptContent,
            settings = GroovyExecContext.defaultSettings(server).copy(
                transactionMode = TransactionMode.ROLLBACK,
                timeout = server.timeout
            )
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
        callback: (CoroutineScope, LoggersGroovyScriptExecResult) -> Unit = { _, _ -> }
    ) {
        GroovyExecClient.getInstance(project).execute(context) { coroutineScope, result ->
            coroutineScope.launch {
                val loggers = result.result
                    ?.split("\n")
                    ?.map { it.split(" | ") }
                    ?.filter { it.size == 3 }
                    ?.map {
                        val loggerIdentifier = it[0]
                        val effectiveLevel = it[1]
                        val parentName = it[2]

                        val psiElementPointer = getPsiElementPointer(project, loggerIdentifier)
                        val icon = resolveIcon(project, loggerIdentifier)

                        CxLoggerModel.of(loggerIdentifier, effectiveLevel, parentName, false, icon, psiElementPointer)
                    }
                    ?.distinctBy { it.name }
                    ?.associateBy { it.name }
                    ?.takeIf { it.isNotEmpty() }

                if (loggers == null || result.hasError) {
                    clearState(server.uuid)
                } else {
                    updateState(loggers, server.uuid)
                }

                callback.invoke(coroutineScope, LoggersGroovyScriptExecResult(loggers, result))

                project.messageBus.syncPublisher(CxLoggersStateListener.TOPIC).onLoggersStateChanged(server)
            }
        }
    }

    fun state(settingsUUID: String): CxLoggersState = loggersStates
        .computeIfAbsent(settingsUUID) { CxLoggersState() }

    private fun updateState(loggers: Map<String, CxLoggerModel>?, settingsUUID: String) {
        coroutineScope.launch {

            state(settingsUUID).update(loggers ?: emptyMap())

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

    private fun clearState(settingsUUID: String) {
        val logState = loggersStates[settingsUUID]
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

private data class LoggersGroovyScriptExecResult(
    val loggers: Map<String, CxLoggerModel>? = null,
    val result: DefaultExecResult
)