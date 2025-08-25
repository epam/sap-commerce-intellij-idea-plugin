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

package sap.commerce.toolset.logging.exec

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
import sap.commerce.toolset.exec.ExecClient
import sap.commerce.toolset.exec.settings.state.generatedURL
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.http.HacHttpClient
import sap.commerce.toolset.logging.CxLoggerModel
import sap.commerce.toolset.logging.exec.context.LoggingExecContext
import sap.commerce.toolset.logging.exec.context.LoggingExecResult
import sap.commerce.toolset.logging.getIcon
import sap.commerce.toolset.logging.getPsiElementPointer
import java.io.IOException
import java.io.Serial
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class LoggingExecClient(project: Project, coroutineScope: CoroutineScope) : ExecClient<LoggingExecContext, LoggingExecResult>(project, coroutineScope) {

    override suspend fun execute(context: LoggingExecContext): LoggingExecResult {
        val connectionSettings = HacExecConnectionService.getInstance(project).activeConnection

        val params = context.params()
            .map { BasicNameValuePair(it.key, it.value) }

        val actionUrl = connectionSettings.generatedURL + "/platform/log4j/changeLevel/"
        val response = HacHttpClient.getInstance(project)
            .post(actionUrl, params, false, context.timeout, connectionSettings, null)

        val statusLine = response.statusLine
        val statusCode = statusLine.statusCode

        if (statusCode != HttpStatus.SC_OK || response.entity == null) {
            return LoggingExecResult(
                statusCode = statusCode,
                errorMessage = "[$statusCode] ${statusLine.reasonPhrase}",
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
                    val psiElementPointer = getPsiElementPointer(project, name)
                    val icon = getIcon(project, name)

                    CxLoggerModel.of(name, effectiveLevel, parentName, false, icon, psiElementPointer)
                }

            return LoggingExecResult(
                statusCode = statusCode,
                result = loggerModels,
            )
        } catch (e: SerializationException) {
            thisLogger().error("Cannot parse response", e)

            return LoggingExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                errorMessage = "Cannot parse response from the server...",
            )
        } catch (e: IOException) {
            return LoggingExecResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                errorMessage = "${e.message} $actionUrl",
            )
        }
    }

    override suspend fun onError(context: LoggingExecContext, exception: Throwable) = LoggingExecResult(
        errorMessage = exception.message,
        errorDetailMessage = exception.stackTraceToString(),
    )

    companion object {
        @Serial
        private const val serialVersionUID: Long = 576041226131571722L

        fun getInstance(project: Project): LoggingExecClient = project.service()
    }

}