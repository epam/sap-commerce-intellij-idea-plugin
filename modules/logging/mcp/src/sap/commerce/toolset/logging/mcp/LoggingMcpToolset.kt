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

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import org.apache.http.HttpStatus
import sap.commerce.toolset.ai.mcp.json.buildListResponse
import sap.commerce.toolset.ai.mcp.regexOrContainsMatcher
import sap.commerce.toolset.ai.mcp.resolveHacConnection
import sap.commerce.toolset.extensions.ExtensionsService
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.settings.state.AuthMode
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.mcp.json.LoggerJsonBuilder
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.settings.state.TransactionMode

/**
 * Exposes the remote SAP Commerce logger operations available in the "SAP Loggers" tool window
 * (module `logging`) as MCP tools: listing the loggers of a remote instance and changing a
 * logger's level via the hAC `HacLog4JFacade`.
 *
 * The underlying `HacLog4JFacade` only supports reading loggers and changing their level, so
 * there is intentionally no "delete logger" tool — there is no such operation on the server.
 */
class LoggingMcpToolset : McpToolset {

    private val json = Json { prettyPrint = false }

    @McpTool(name = "sap_commerce_list_loggers")
    @McpDescription(
        """Lists loggers declared on a SAP Commerce (Hybris) server via the HAC.
        |Returns a JSON object: {"connection", "filter"?, "matched", "total", "items": [{"name", "level", "parent"?}]}, where each item is a logger, 'level' is the currently effective log level and 'parent' (the parent logger) is omitted for root-level loggers. 'filter' echoes the applied name filter and is present only when one was given.
        |A remote instance can declare hundreds or thousands of loggers, so prefer the optional 'filter' parameter to return only the loggers you need and keep the response (and token usage) small.
        |Requires a configured and authenticated HAC connection.
        |PRECONDITION: only call this tool against a connection whose authMode is AUTOMATIC (supportedByMcp = true in sap_commerce_list_hac_connections). If the user asks to use a connection that uses MANUAL (browser) authentication, do NOT call this tool — instead tell the user that connection is not supported by MCP tools yet and offer an AUTOMATIC one. Calling it against a MANUAL connection will fail."""
    )
    suspend fun listLoggers(
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified. Must refer to a connection with AUTOMATIC authentication; MANUAL (browser) connections are rejected")
        connectionName: String? = null,
        @McpDescription(
            """Optional logger-name filter used to shrink the response and save tokens.
            |If the value is a valid regular expression it is matched against each logger name with a case-insensitive regex search (e.g. '^de\.hybris\.platform\.' or 'spring|hibernate');
            |otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all loggers."""
        )
        filter: String? = null,
    ): String {
        val project = currentCoroutineContext().project
        val connection = resolveAutomaticHacConnection(project, connectionName)

        val scriptContent = readAction { ExtensionsService.getInstance().findResource(CxLogConstants.EXTENSION_STATE_SCRIPT) }
        val allLoggers = runLoggersScript(project, connection, scriptContent)

        val normalizedFilter = filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val matched = matcher?.let { match -> allLoggers.filter { match(it.name) } } ?: allLoggers
        val payload = buildListResponse(
            items = matched,
            total = allLoggers.size,
            itemBuilder = LoggerJsonBuilder,
            filterText = normalizedFilter,
            additionalFields = { put("connection", connection.connectionName) },
        )

        return json.encodeToString(JsonObject.serializer(), payload)
    }

    @McpTool(name = "sap_commerce_update_logger_level")
    @McpDescription(
        """Sets the log level of a single logger on a SAP Commerce (Hybris) server via the HAC.
        |Creates the logger override if it does not exist yet, otherwise updates the existing one.
        |Valid levels: ALL, OFF, TRACE, DEBUG, INFO, WARN, ERROR, FATAL.
        |Returns the resulting effective level of the logger.
        |Requires a configured and authenticated HAC connection.
        |PRECONDITION: only call this tool against a connection whose authMode is AUTOMATIC (supportedByMcp = true in sap_commerce_list_hac_connections). If the user asks to use a connection that uses MANUAL (browser) authentication, do NOT call this tool — instead tell the user that connection is not supported by MCP tools yet and offer an AUTOMATIC one. Calling it against a MANUAL connection will fail."""
    )
    suspend fun updateLoggerLevel(
        @McpDescription("Fully-qualified logger name (package or class), e.g. 'org.springframework' or 'de.hybris.platform'")
        loggerName: String,
        @McpDescription("New log level. One of: ALL, OFF, TRACE, DEBUG, INFO, WARN, ERROR, FATAL")
        level: String,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified. Must refer to a connection with AUTOMATIC authentication; MANUAL (browser) connections are rejected")
        connectionName: String? = null,
    ): String {
        val project = currentCoroutineContext().project
        val connection = resolveAutomaticHacConnection(project, connectionName)

        val normalizedLoggerName = loggerName.trim()
        if (normalizedLoggerName.isBlank()) error("Logger name must not be blank.")
        if (normalizedLoggerName == CxLogConstants.ROOT_LOGGER_NAME) error("The '${CxLogConstants.ROOT_LOGGER_NAME}' logger level cannot be changed.")

        val logLevel = CxLogLevel.entries.find { it.name.equals(level.trim(), ignoreCase = true) }
            ?: error("Invalid log level '$level'. Valid levels: ${CxLogLevel.entries.joinToString { it.name }}")

        val loggerEntry = "\"${escapeGroovyString(normalizedLoggerName)}\" : \"${logLevel.name}\""
        val scriptContent = readAction { ExtensionsService.getInstance().findResource(CxLogConstants.UPDATE_CX_LOGGERS_STATE) }
            .replace("[loggersMapToBeReplacedPlaceholder]", loggerEntry)

        val loggers = runLoggersScript(project, connection, scriptContent)

        val effectiveLevel = loggers.find { it.name == normalizedLoggerName }?.level?.name
            ?: logLevel.name

        return "Logger '$normalizedLoggerName' set to $effectiveLevel on ${connection.connectionName}."
    }

    /**
     * Resolves the target HAC [connectionName] (or the active connection) and ensures it uses
     * AUTOMATIC authentication.
     *
     * The logger MCP tools talk to the server through the Groovy console, which currently cannot
     * drive the external browser-based authentication used by [AuthMode.MANUAL] connections. Until
     * that is supported, such connections are rejected with an actionable message.
     *
     * Note: this guard is intentionally limited to the logger tools — the other toolsets will be
     * updated in a separate PR.
     */
    private fun resolveAutomaticHacConnection(project: Project, connectionName: String?): HacConnectionSettingsState {
        val connection = resolveHacConnection(project, connectionName)
        if (connection.authMode == AuthMode.MANUAL) {
            error(
                "HAC connection '${connection.connectionName}' uses Manual (browser) authentication, " +
                    "which is not yet supported by the logger MCP tools. " +
                    "Switch the connection to '${AuthMode.AUTOMATIC.title}' to use this tool."
            )
        }
        return connection
    }

    /**
     * Escapes [value] for safe embedding inside a Groovy double-quoted string literal (GString), so
     * that a quote, backslash, `$` (GString interpolation) or newline in a user-supplied logger name
     * cannot break — or inject Groovy into — the generated `update-cx-loggers-state.groovy` script.
     */
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

    /**
     * Executes one of the bundled loggers Groovy scripts against [connection] and parses the
     * `name | effectiveLevel | parentName` lines it returns into [CxLoggerPresentation]s.
     * Mirrors the parsing performed by `CxRemoteLogStateService`.
     */
    private suspend fun runLoggersScript(
        project: Project,
        connection: HacConnectionSettingsState,
        scriptContent: String,
    ): List<CxLoggerPresentation> {
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
}
