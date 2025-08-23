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

package com.intellij.idea.plugin.hybris.tools.remote.execution.groovy

import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.RemoteConnectionContext
import com.intellij.idea.plugin.hybris.tools.remote.http.RemoteConnectionContext.Companion.auto
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.WindowManager
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Serial
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

@Service(Service.Level.PROJECT)
class GroovyExecutionClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecutionClient<GroovyExecutionContext>(project, coroutineScope) {

    var connectionContext: RemoteConnectionContext
        get() = putUserDataIfAbsent(KEY_REMOTE_CONNECTION_CONTEXT, auto())
        set(value) {
            putUserData(KEY_REMOTE_CONNECTION_CONTEXT, value)
        }

    override suspend fun execute(context: GroovyExecutionContext): DefaultExecutionResult {
        val settings = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        val actionUrl = "${settings.generatedURL}/console/scripting/execute"
        val mutableParams = context.params().toMutableMap()
        if (context.scriptTemplate != null) {
            mutableParams["script"] = applyScriptTemplate(
                context.content,
                context.scriptTemplate,
                context.webContext ?: "default")
        }
        val params = mutableParams.map { BasicNameValuePair(it.key, it.value) }

        val response = HybrisHacHttpClient.getInstance(project)
            .post(actionUrl, params, true, context.timeout, settings, context.replicaContext)
        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) return DefaultExecutionResult(
            replicaContext = context.replicaContext,
            statusCode = statusCode,
            errorMessage = "[$statusCode] ${statusLine.reasonPhrase}"
        )

        try {
            val jsonAsString = String(response.entity.content.readAllBytes(), StandardCharsets.UTF_8)
            var json = Json.parseToJsonElement(jsonAsString)

            var errorText = json.jsonObject["stacktraceText"]
                ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

            if (errorText == null && context.scriptTemplate != null) {
                json = json.jsonObject["executionResult"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?.let { Json.parseToJsonElement(it) }
                    ?: JsonObject(emptyMap())
                errorText = json.jsonObject["stacktraceText"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            }

            return if (errorText != null) DefaultExecutionResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = errorText
            )
            else DefaultExecutionResult(
                replicaContext = context.replicaContext,
                output = json.jsonObject["outputText"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() },
                result = json.jsonObject["executionResult"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            )

        } catch (e: SerializationException) {
            thisLogger().error("Cannot parse response", e)

            return DefaultExecutionResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = "Cannot parse response from the server..."
            )
        } catch (e: IOException) {
            return DefaultExecutionResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = "${e.message} $actionUrl"
            )
        }
    }

    fun applyScriptTemplate(script: String, scriptTemplatePath: String, webContext: String): String {
        val statusBar = WindowManager.getInstance().getStatusBar(project)

        var template : String? = null

        try {
            template = if (scriptTemplatePath.startsWith("file://")) {
                Files.readString(Path.of(scriptTemplatePath.substring("file://".length)), StandardCharsets.UTF_8)
            } else {
                getScriptAsResource(scriptTemplatePath)
            }
        } catch (e: IOException) {
            statusBar.setInfo(String.format("Can't find custom script template %s [%s]", scriptTemplatePath, e.message))
        }

        return if (template == null) {
            statusBar.setInfo("Can't load custom script template $scriptTemplatePath")
            script
        } else {
            val encodedScript = Base64.getEncoder().encodeToString(script.toByteArray(StandardCharsets.UTF_8))
            template.replace("\$hacEncodedScript", encodedScript).replace("\$hacSpringWebContext", webContext)
        }
    }

    @Throws(IOException::class)
    fun getScriptAsResource(path: String): String? {
        val templateStream = javaClass.getResourceAsStream(path)
        var template: String? = null

        if (templateStream != null) {
            BufferedReader(InputStreamReader(templateStream, StandardCharsets.UTF_8)).use { reader ->
                template = reader.lines().collect(
                    Collectors.joining("\n")
                )
            }
        }

        return template
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 3297887080603991051L
        val KEY_REMOTE_CONNECTION_CONTEXT = Key.create<RemoteConnectionContext>("hybris.http.remote.connection.context")
        const val GHAC_SCRIPT_TEMPLATE_GROOVY: String = "/ghac/scriptTemplate.groovy"

        fun getInstance(project: Project): GroovyExecutionClient = project.service()
    }

}
