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

import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerModel
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.HttpStatus
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class LoggingExecutionClient(project: Project, coroutineScope: CoroutineScope) : ExecutionClient<LoggingExecutionContext, LoggingExecutionResult>(project, coroutineScope) {

    val cxLoggerUtilities: CxLoggerUtilities = CxLoggerUtilities.getInstance(project)

    override suspend fun execute(context: LoggingExecutionContext): LoggingExecutionResult {
        val settings = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)

        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }

        val actionUrl = settings.generatedURL + "/platform/log4j/changeLevel/"
        val response = HybrisHacHttpClient.getInstance(project)
            .post(actionUrl, params, false, context.timeout, settings, null)

        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) {
            return LoggingExecutionResult(
                statusCode = statusCode,
                errorMessage = "[$statusCode] ${statusLine.reasonPhrase}"
            )
        }

        try {
            val loggerModels = Jsoup
                .parse(response.entity.content, StandardCharsets.UTF_8.name(), "")
                .getElementsByTag("body").text()
                .let { Json.parseToJsonElement(it) }
                .jsonObject["loggers"]
                ?.jsonArray
                ?.mapNotNull {
                    val name = it.jsonObject["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val effectiveLevel = it.jsonObject["effectiveLevel"]?.jsonObject["standardLevel"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val parentName = it.jsonObject["parentName"]?.jsonPrimitive?.content
                    val icon = cxLoggerUtilities.getIcon(name)

                    CxLoggerModel.of(name, effectiveLevel, parentName, false, icon)
                }

            return LoggingExecutionResult(
                statusCode = statusCode,
                result = loggerModels,
            )
        } catch (e: SerializationException) {
            thisLogger().error("Cannot parse response", e)

            return LoggingExecutionResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                errorMessage = "Cannot parse response from the server..."
            )
        } catch (e: IOException) {
            return LoggingExecutionResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                errorMessage = "${e.message} $actionUrl"
            )
        }
    }

    override suspend fun onError(context: LoggingExecutionContext, exception: Throwable) = LoggingExecutionResult(
        errorMessage = exception.message,
        errorDetailMessage = exception.stackTraceToString(),
    )

    companion object {
        @Serial
        private const val serialVersionUID: Long = 576041226131571722L

        fun getInstance(project: Project): LoggingExecutionClient = project.service()
    }

}