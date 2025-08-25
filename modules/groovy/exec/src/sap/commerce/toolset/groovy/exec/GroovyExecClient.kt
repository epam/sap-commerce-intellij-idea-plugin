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

package sap.commerce.toolset.groovy.exec

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import sap.commerce.toolset.exec.DefaultExecClient
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.exec.settings.state.generatedURL
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.exec.context.GroovyReplicaAwareContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class GroovyExecClient(project: Project, coroutineScope: CoroutineScope) : DefaultExecClient<GroovyExecContext>(project, coroutineScope) {

    var connectionContext: GroovyReplicaAwareContext
        get() = putUserDataIfAbsent(KEY_REMOTE_CONNECTION_CONTEXT, GroovyReplicaAwareContext.auto())
        set(value) {
            putUserData(KEY_REMOTE_CONNECTION_CONTEXT, value)
        }

    override suspend fun execute(context: GroovyExecContext): DefaultExecResult {
        val settings = HacExecConnectionService.getInstance(project).activeConnection
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
            val document = Jsoup.parse(response.entity.content, StandardCharsets.UTF_8.name(), "")
            val jsonAsString = document.getElementsByTag("body").text()
            val json = Json.parseToJsonElement(jsonAsString)

            json.jsonObject["stacktraceText"]

            val errorText = json.jsonObject["stacktraceText"]
                ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

            return if (errorText != null) DefaultExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                replicaContext = context.replicaContext,
                errorMessage = errorText
            )
            else DefaultExecResult(
                replicaContext = context.replicaContext,
                output = json.jsonObject["outputText"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() },
                result = json.jsonObject["executionResult"]
                    ?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
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
        val KEY_REMOTE_CONNECTION_CONTEXT = Key.create<GroovyReplicaAwareContext>("hybris.http.remote.connection.context")

        fun getInstance(project: Project): GroovyExecClient = project.service()
    }

}