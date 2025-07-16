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
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult.HybrisHttpResultBuilder
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import org.jetbrains.kotlin.idea.gradleTooling.get
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class GroovyExecutionClient(project: Project, coroutineScope: CoroutineScope) : ExecutionClient<GroovyExecutionContext>(project, coroutineScope) {

    override suspend fun execute(context: GroovyExecutionContext): ExecutionResult {
        val settings = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)

        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }
        val actionUrl = settings.generatedURL + "/console/scripting/execute"

        val hacHttpClient = HybrisHacHttpClient.getInstance(project)
        val response: HttpResponse = hacHttpClient
            .post(actionUrl, params, true, context.timeout, settings, context.replicaContext)
        val statusLine = response.statusLine
        val resultBuilder = HybrisHttpResultBuilder.createResult()
            .httpCode(statusLine.statusCode)

        if (statusLine.statusCode != HttpStatus.SC_OK || response.entity == null) {
            return resultBuilder
                .httpCode(HttpStatus.SC_BAD_REQUEST)
                .errorMessage("[${statusLine.statusCode}] ${statusLine.reasonPhrase}")
                .build()
        }
        val document: Document
        try {
            document = Jsoup.parse(response.entity.content, StandardCharsets.UTF_8.name(), "")
        } catch (e: IOException) {
            return resultBuilder
                .httpCode(HttpStatus.SC_BAD_REQUEST)
                .errorMessage("${e.message} $actionUrl")
                .build()
        }
        val fsResultStatus = document.getElementsByTag("body")
        if (fsResultStatus == null) {
            return resultBuilder
                .httpCode(HttpStatus.SC_BAD_REQUEST)
                .errorMessage("No data in response")
                .build()
        }
        val json = parseResponse(fsResultStatus)

        if (json == null) {
            return resultBuilder
                .errorMessage("Cannot parse response from the server...")
                .build()
        }

        val errorText = json["stacktraceText"]?.toString()
            ?.takeIf { it.isNotBlank() }
        if (errorText != null) {
            resultBuilder.errorMessage(errorText)
        } else {
            json["outputText"]?.toString()
                ?.let { resultBuilder.output(it) }
            json["executionResult"]?.toString()
                ?.let { resultBuilder.result(it) }
        }

        return resultBuilder
            .build()
    }

}
