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
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveHacConnection
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.hac.exec.settings.state.AuthMode
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.CxLogLevel

class LoggingMcpToolset : McpToolset {

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
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val connection = getHacConnection(project, connectionName)
        val loggers = LoggingMcpService.getInstance(project).listLoggers(connection, filter)
        return mapper.map(loggers)
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
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val connection = getHacConnection(project, connectionName)
        val normalizedLoggerName = getNormalizedLoggerName(loggerName)

        val logLevel = CxLogLevel.entries.find { it.name.equals(level.trim(), ignoreCase = true) }
            ?: error("Invalid log level '$level'. Valid levels: ${CxLogLevel.entries.joinToString { it.name }}")

        val result = LoggingMcpService.getInstance(project).updateLoggerLevel(connection, normalizedLoggerName, logLevel)
        return mapper.map(result)
    }

    private fun getNormalizedLoggerName(loggerName: String): String {
        val normalizedLoggerName = loggerName.trim()
        if (normalizedLoggerName.isBlank()) error("Logger name must not be blank.")
        if (normalizedLoggerName == CxLogConstants.ROOT_LOGGER_NAME) error("The '${CxLogConstants.ROOT_LOGGER_NAME}' logger level cannot be changed.")
        return normalizedLoggerName
    }

    private fun getHacConnection(project: Project, connectionName: String?): HacConnectionSettingsState {
        val connection = resolveHacConnection(project, connectionName)
        if (connection.authMode == AuthMode.AUTOMATIC) return connection

        error(
            "HAC connection '${connection.connectionName}' uses Manual (browser) authentication, " +
                "which is not yet supported by the logger MCP tools. " +
                "Switch the connection to '${AuthMode.AUTOMATIC.title}' to use this tool."
        )
    }

}
