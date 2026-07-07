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

package sap.commerce.toolset.logging.mcp

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.http.HttpStatus
import sap.commerce.toolset.ai.mcp.regexOrContainsMatcher
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.mcp.dto.LoggerDto
import sap.commerce.toolset.logging.mcp.dto.LoggerListResponse
import sap.commerce.toolset.logging.mcp.dto.LoggerUpdateResult
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.settings.state.TransactionMode

@Service(Service.Level.PROJECT)
class LoggingMcpService(private val project: Project) {

    suspend fun listLoggers(connection: HacConnectionSettingsState, filter: String?): LoggerListResponse {
        val scriptContent = readAction { ExtensionsService.getInstance().findResource(CxLogConstants.EXTENSION_STATE_SCRIPT) }
        val allLoggers = runScript(connection, scriptContent)

        val normalizedFilter = filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val matched = matcher?.let { match -> allLoggers.filter { match(it.name) } } ?: allLoggers

        return LoggerListResponse(
            connection = connection.connectionName,
            filter = normalizedFilter,
            matched = matched.size,
            total = allLoggers.size,
            items = matched.map { it.toDto() },
        )
    }

    suspend fun runScript(connection: HacConnectionSettingsState, scriptContent: String): List<CxLoggerPresentation> {
        val context = GroovyExecContext(
            connection = connection,
            content = scriptContent,
            transactionMode = TransactionMode.ROLLBACK,
            timeout = connection.timeout,
        )

        val result = GroovyExecClient.getInstance(project).execute(context)

        if (result.statusCode != HttpStatus.SC_OK || result.errorMessage != null) {
            error(
                buildString {
                    append("Failed to communicate with ${connection.connectionName}")
                    result.errorMessage?.let { append(": $it") }
                }
            )
        }

        return result.result
            ?.split("\n")
            ?.map { it.split(" | ") }
            ?.filter { it.size == 3 }
            ?.map { CxLoggerPresentation.of(it[0], it[1], it[2], false) }
            ?.distinctBy { it.name }
            ?: emptyList()
    }

    suspend fun updateLoggerLevel(
        connection: HacConnectionSettingsState,
        loggerName: String,
        logLevel: CxLogLevel,
    ): LoggerUpdateResult {
        val loggerEntry = "\"${escapeGroovyString(loggerName)}\" : \"${logLevel.name}\""
        val scriptContent = readAction { ExtensionsService.getInstance().findResource(CxLogConstants.UPDATE_CX_LOGGERS_STATE) }
            .replace("[loggersMapToBeReplacedPlaceholder]", loggerEntry)

        val loggers = runScript(connection, scriptContent)
        val effectiveLevel = loggers.find { it.name == loggerName }?.level?.name ?: logLevel.name

        return LoggerUpdateResult(
            connection = connection.connectionName,
            logger = loggerName,
            level = effectiveLevel,
        )
    }

    private fun escapeGroovyString(value: String): String = buildString {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> append("\\$")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(ch)
            }
        }
    }

    private fun CxLoggerPresentation.toDto() = LoggerDto(
        name = name,
        level = level.name,
        parent = parentName?.takeIf { it.isNotBlank() && it != CxLogConstants.ROOT_LOGGER_NAME },
    )

    companion object {
        fun getInstance(project: Project): LoggingMcpService = project.service()
    }
}
