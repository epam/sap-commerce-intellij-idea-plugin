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

package sap.commerce.toolset.groovy.exec

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import sap.commerce.toolset.exec.DefaultExecClient
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.settings.state.GroovyExecMode
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.readResource
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64

@Service(Service.Level.PROJECT)
class GroovyExecClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecClient<GroovyExecContext>(project, coroutineScope) {

    override suspend fun execute(context: GroovyExecContext): DefaultExecResult {
        val executableContext = context.webContext
            ?.let { webContext -> createTemplateContext(context, webContext) }
            ?: if (context.execMode == GroovyExecMode.TEMPLATE) createTemplateContext(context,  GroovyExecConstants.DEFAULT_WEB_CONTEXT)
            else context

        return executeInternally(executableContext)
    }

    private fun createTemplateContext(context: GroovyExecContext, webContext: String): GroovyExecContext {
        val encodedScript = Base64.encode(context.content.toByteArray(StandardCharsets.UTF_8))
        val webContextGroovyScript = readResource("scripts/groovy-executeOnWebContext.groovy")
            .replace($$"$hacEncodedScript", encodedScript)
            .replace($$"$hacSpringWebContext", webContext)
            .replace($$"$exceptionHandling", context.exceptionHandling.name)

        return context.copy(
            content = webContextGroovyScript,
            execMode = GroovyExecMode.TEMPLATE,
        )
    }

    private suspend fun executeInternally(context: GroovyExecContext): DefaultExecResult {
        val settings = context.connection
        val actionUrl = "${settings.generatedURL}/console/scripting/execute"
        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }

        val response = HacHttpClient.getInstance(project)
            .post(actionUrl, params, true, context.timeout, settings, context.replicaContext)
        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) return DefaultExecResult(
            replicaContext = context.replicaContext,
            statusCode = statusCode,
            errorMessage = "[$statusCode] ${statusLine.reasonPhrase}"
        )

        try {
            val response = response.entity.content.readBytes().toString(Charsets.UTF_8)
            val responseWrapperJson = Json.parseToJsonElement(response)
            val json = when (context.execMode) {
                GroovyExecMode.DIRECT -> responseWrapperJson
                GroovyExecMode.TEMPLATE -> responseWrapperJson.jsonObject[GroovyExecConstants.RESPONSE_EXECUTION_RESULT]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?.let {
                        runCatching { Json.parseToJsonElement(it) }.getOrElse { responseWrapperJson }
                    }
                    ?: responseWrapperJson
            }

            val outputText = json.jsonObject[GroovyExecConstants.RESPONSE_OUTPUT_TEXT]
                ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            val executionResult = json.jsonObject[GroovyExecConstants.RESPONSE_EXECUTION_RESULT]
                ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            val error = GroovyExecResponseError.from(json)

            return DefaultExecResult(
                statusCode = error?.let { HttpStatus.SC_BAD_REQUEST } ?: HttpStatus.SC_OK,
                replicaContext = context.replicaContext,
                errorMessage = error?.message,
                errorDetailMessage = error?.details,
                output = outputText,
                result = executionResult
            )
        } catch (e: SerializationException) {
            thisLogger().error("Cannot parse response", e)

            return DefaultExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = "Cannot parse response from the server..."
            )
        } catch (e: IOException) {
            return DefaultExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = "${e.message} $actionUrl"
            )
        }
    }

    private suspend fun executeOnWebContext(originalContext: GroovyExecContext, webContext: String): DefaultExecResult {
        val settings = originalContext.connection
        val actionUrl = "${settings.generatedURL}/console/scripting/execute"

        val encodedScript = Base64.encode(originalContext.content.toByteArray(StandardCharsets.UTF_8))
        val webContextGroovyScript = readResource("scripts/groovy-executeOnWebContext.groovy")
            .replace($$"$hacEncodedScript", encodedScript)
            .replace($$"$hacSpringWebContext", webContext)
            .replace($$"$exceptionHandling", originalContext.exceptionHandling.name)

        val context = originalContext.copy(content = webContextGroovyScript)
        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }
        val response = HacHttpClient.getInstance(project)
            .post(actionUrl, params, true, context.timeout, settings, context.replicaContext)
        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) return DefaultExecResult(
            replicaContext = context.replicaContext,
            statusCode = statusCode,
            errorMessage = "[$statusCode] ${statusLine.reasonPhrase}"
        )

        try {
            val jsonAsString = response.entity.content.readBytes().toString(Charsets.UTF_8)
            var json = Json.parseToJsonElement(jsonAsString)

            var errorText = json.jsonObject["stacktraceText"]
                ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            var outputText = json.jsonObject["outputText"]
                ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

            if (errorText == null) {
                json = json.jsonObject["executionResult"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?.let { Json.parseToJsonElement(it) }
                    ?: JsonObject(emptyMap())

                val nestedOutputText = json.jsonObject["outputText"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

                outputText = when {
                    outputText == null -> nestedOutputText
                    nestedOutputText == null -> outputText
                    else -> outputText + "\n" + nestedOutputText
                }
                errorText = json.jsonObject["stacktraceText"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            }

            val errorTextSplitIndex = errorText?.indexOf("\tat") ?: -1

            return DefaultExecResult(
                statusCode = errorText?.let { HttpStatus.SC_BAD_REQUEST } ?: HttpStatus.SC_OK,
                replicaContext = context.replicaContext,
                errorMessage = when {
                    errorTextSplitIndex != -1 -> errorText?.substring(0, errorTextSplitIndex)
                    else -> errorText
                },
                errorDetailMessage = when {
                    errorTextSplitIndex != -1 -> errorText?.replace("\t", "    ")
                    else -> null
                },
                output = outputText,
                result = json.jsonObject["executionResult"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() && it != "null" }
            )

        } catch (e: SerializationException) {
            thisLogger().error("Cannot parse response", e)

            return DefaultExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = "Cannot parse response from the server..."
            )
        } catch (e: IOException) {
            return DefaultExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = "${e.message} $actionUrl"
            )
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 3297887080603991051L

        fun getInstance(project: Project): GroovyExecClient = project.service()
    }

}