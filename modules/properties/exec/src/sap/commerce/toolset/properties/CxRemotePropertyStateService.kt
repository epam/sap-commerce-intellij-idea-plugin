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

package sap.commerce.toolset.properties

import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.hac.exec.settings.event.HacConnectionSettingsListener
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.properties.exec.CxRemotePropertyState
import sap.commerce.toolset.properties.exec.event.CxRemotePropertyStateListener
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.settings.state.TransactionMode
import java.util.*

@Service(Service.Level.PROJECT)
class CxRemotePropertyStateService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) : Disposable {

    private val propertyStates = WeakHashMap<String, CxRemotePropertyState>()
    private var fetching = false

    val ready: Boolean
        get() = !fetching

    val stateInitialized: Boolean
        get() = state(HacExecConnectionService.getInstance(project).activeConnection.uuid).initialized()

    init {
        with(project.messageBus.connect(this)) {
            subscribe(HacConnectionSettingsListener.TOPIC, object : HacConnectionSettingsListener {
                override fun onActive(connection: HacConnectionSettingsState) = Unit
                override fun onUpdate(settings: Collection<HacConnectionSettingsState>) = settings.forEach { clearState(it) }
                override fun onSave(settings: Collection<HacConnectionSettingsState>) = settings.forEach { clearState(it) }
                override fun onDelete(connection: HacConnectionSettingsState) {
                    propertyStates.remove(connection.uuid)
                }
            })
        }
    }

    fun fetch() = fetch(HacExecConnectionService.getInstance(project).activeConnection)

    fun fetch(server: HacConnectionSettingsState) {
        fetching = true

        coroutineScope.launch {
            val context = GroovyExecContext(
                connection = server,
                executionTitle = "Fetching Properties from SAP Commerce [${server.shortenConnectionName}]...",
                content = ExtensionsService.getInstance().findResource(CxPropertyConstants.EXTENSION_STATE_SCRIPT) ,
                transactionMode = TransactionMode.ROLLBACK,
                timeout = server.timeout,
            )

            GroovyExecClient.getInstance(project).execute(context) { _, result ->
                val properties = result.result
                    ?.takeIf { !result.hasError }
                    ?.let(::parseProperties)

                if (properties == null || result.hasError) {
                    clearState(server)
                    notify(NotificationType.ERROR, "Failed to retrieve properties") {
                        result.errorMessage ?: "Unable to retrieve properties state."
                    }
                } else {
                    state(server.uuid).update(properties)
                    fetching = false
                    project.messageBus.syncPublisher(CxRemotePropertyStateListener.TOPIC).onPropertiesStateChanged(server)
                    notify(NotificationType.INFORMATION, "Properties fetched") {
                        "<p>Declared properties: ${properties.size}</p><p>Server: ${server.shortenConnectionName}</p>"
                    }
                }
            }
        }
    }

    fun upsertProperty(key: String, value: String, callback: (Boolean) -> Unit = {}) {
        val trimmedKey = key.trim()
        if (!isValidPropertyKey(trimmedKey)) {
            callback(false)
            return
        }

        val server = HacExecConnectionService.getInstance(project).activeConnection
        coroutineScope.launch {
            val response = HacHttpClient.getInstance(project).post(
                "${server.generatedURL}/platform/configstore",
                listOf(
                    BasicNameValuePair("key", trimmedKey),
                    BasicNameValuePair("val", value),
                ),
                true,
                server.timeout,
                server,
                null,
            )

            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                state(server.uuid).put(CxPropertyPresentation.of(trimmedKey, value))
                project.messageBus.syncPublisher(CxRemotePropertyStateListener.TOPIC).onPropertiesStateChanged(server)
                notify(NotificationType.INFORMATION, "Property stored") {
                    "<p>Property: $trimmedKey</p><p>Server: ${server.shortenConnectionName}</p>"
                }
                callback(true)
            } else {
                notify(NotificationType.ERROR, "Failed to store property") {
                    "<p>${response.statusLine.reasonPhrase}</p><p>Server: ${server.shortenConnectionName}</p>"
                }
                callback(false)
            }
        }
    }

    fun applyProperties(
        properties: List<CxPropertyPresentation>,
        callback: (CoroutineScope, DefaultExecResult) -> Unit = { _, _ -> },
    ) {
        val server = HacExecConnectionService.getInstance(project).activeConnection

        coroutineScope.launch {
            var failed: String? = null

            for (property in properties) {
                val ok = postConfigStore(server, property.key, property.value)
                if (!ok) {
                    failed = property.key
                    break
                }

                state(server.uuid).put(property)
            }

            project.messageBus.syncPublisher(CxRemotePropertyStateListener.TOPIC).onPropertiesStateChanged(server)

            val result = if (failed == null) {
                notify(NotificationType.INFORMATION, "Properties template applied") {
                    "<p>Applied properties: ${properties.size}</p><p>Server: ${server.shortenConnectionName}</p>"
                }
                DefaultExecResult()
            } else {
                notify(NotificationType.ERROR, "Failed to apply properties template") {
                    "<p>Property: $failed</p><p>Server: ${server.shortenConnectionName}</p>"
                }
                DefaultExecResult(
                    statusCode = HttpStatus.SC_BAD_REQUEST,
                    errorMessage = "Failed to apply property: $failed",
                )
            }

            callback(coroutineScope, result)
        }
    }

    fun deleteProperty(key: String, callback: (Boolean) -> Unit = {}) {
        val trimmedKey = key.trim()
        val server = HacExecConnectionService.getInstance(project).activeConnection

        coroutineScope.launch {
            val response = HacHttpClient.getInstance(project).post(
                "${server.generatedURL}/platform/configdelete",
                listOf(BasicNameValuePair("key", trimmedKey)),
                true,
                server.timeout,
                server,
                null,
            )

            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                state(server.uuid).remove(trimmedKey)
                project.messageBus.syncPublisher(CxRemotePropertyStateListener.TOPIC).onPropertiesStateChanged(server)
                notify(NotificationType.INFORMATION, "Property deleted") {
                    "<p>Property: $trimmedKey</p><p>Server: ${server.shortenConnectionName}</p>"
                }
                callback(true)
            } else {
                notify(NotificationType.ERROR, "Failed to delete property") {
                    "<p>${response.statusLine.reasonPhrase}</p><p>Server: ${server.shortenConnectionName}</p>"
                }
                callback(false)
            }
        }
    }

    fun state(settingsUUID: String): CxRemotePropertyState = propertyStates.computeIfAbsent(settingsUUID) { CxRemotePropertyState() }

    private fun clearState(server: HacConnectionSettingsState) {
        propertyStates[server.uuid]?.clear()
        fetching = false
        project.messageBus.syncPublisher(CxRemotePropertyStateListener.TOPIC).onPropertiesStateChanged(server)
    }

    private fun parseProperties(payload: String): Map<String, CxPropertyPresentation>? = try {
        Json.parseToJsonElement(payload)
            .jsonArray
            .mapNotNull {
                val obj = it.jsonObject
                val key = obj["key"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val value = obj["value"]?.jsonPrimitive?.content
                key to CxPropertyPresentation.of(key, value)
            }
            .toMap()
    } catch (e: Exception) {
        thisLogger().warn("Unable to parse properties payload", e)
        null
    }

    private fun isValidPropertyKey(key: String): Boolean = key.isNotBlank() && !key.any(Char::isWhitespace)

    private suspend fun postConfigStore(server: HacConnectionSettingsState, key: String, value: String): Boolean {
        val response = HacHttpClient.getInstance(project).post(
            "${server.generatedURL}/platform/configstore",
            listOf(
                BasicNameValuePair("key", key),
                BasicNameValuePair("val", value),
            ),
            true,
            server.timeout,
            server,
            null,
        )

        return response.statusLine.statusCode == HttpStatus.SC_OK
    }

    private fun notify(type: NotificationType, title: String, contentProvider: () -> String) = Notifications
        .create(type, title, contentProvider())
        .hideAfter(5)
        .notify(project)

    override fun dispose() = propertyStates.clear()

    companion object {
        fun getInstance(project: Project): CxRemotePropertyStateService = project.service()
    }
}
