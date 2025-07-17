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

package com.intellij.idea.plugin.hybris.tools.remote.execution.logging

import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class LoggingExecutionClient(project: Project, coroutineScope: CoroutineScope) : ExecutionClient<LoggingExecutionContext>(project, coroutineScope) {

    override suspend fun execute(context: LoggingExecutionContext): ExecutionResult {
        val settings = project.service<RemoteConnectionService>().getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)

        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }

        val actionUrl = settings.generatedURL + "/platform/log4j/changeLevel/"
        val response = HybrisHacHttpClient.getInstance(project)
            .post(actionUrl, params, false, HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT, settings, null)

        val statusLine = response.statusLine
        val resultBuilder = ExecutionResult.builder()
            .remoteConnectionType(RemoteConnectionType.Hybris)
            .httpCode(statusLine.statusCode)

        if (statusLine.statusCode != HttpStatus.SC_OK || response.entity == null) {
            return resultBuilder
                .httpCode(HttpStatus.SC_BAD_REQUEST)
                .errorMessage("[${statusLine.statusCode}] ${statusLine.reasonPhrase}")
                .build()
        }

        try {
            val document = Jsoup.parse(response.entity.content, StandardCharsets.UTF_8.name(), "")
            val jsonAsString = document.getElementsByTag("body").text()
            val json = Json.parseToJsonElement(jsonAsString)

            val loggers = json.jsonObject["loggers"]
            // TODO: @Eugeni to use this response
        } catch (e: SerializationException) {
            thisLogger().error("Cannot parse response", e)

            resultBuilder
                .httpCode(HttpStatus.SC_BAD_REQUEST)
                .errorMessage("Cannot parse response from the server...")
        } catch (e: IOException) {
            resultBuilder
                .errorMessage("${e.message} $actionUrl")
                .httpCode(HttpStatus.SC_BAD_REQUEST)
        }

        return resultBuilder.build()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 576041226131571722L
    }

}