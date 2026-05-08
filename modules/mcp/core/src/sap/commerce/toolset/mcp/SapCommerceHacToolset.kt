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

package sap.commerce.toolset.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.project.Project
import org.apache.http.HttpStatus
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.impex.exec.ImpExExecClient
import sap.commerce.toolset.impex.exec.context.ImpExExecContext
import sap.commerce.toolset.impex.exec.context.ImpExExecutionMode
import sap.commerce.toolset.settings.state.TransactionMode
import kotlin.coroutines.coroutineContext

class SapCommerceHacToolset : McpToolset {

    @McpTool(name = "sap_commerce_execute_groovy")
    @McpDescription(
        """Executes a Groovy script on a SAP Commerce (Hybris) server via the HAC (Hybris Administration Console).
        |Returns the script's console output and execution result.
        |The script runs in the server's context with access to all SAP Commerce APIs and Spring beans.
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun executeGroovy(
        @McpDescription("Groovy script source code to execute on the SAP Commerce server")
        script: String,
        @McpDescription("Whether to commit the transaction after execution. Default is false (rollback)")
        commit: Boolean = false,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
    ): String {
        val project = coroutineContext.mcpProject
        val connection = resolveHacConnection(project, connectionName)

        val context = GroovyExecContext(
            connection = connection,
            content = script,
            timeout = connection.timeout,
            transactionMode = if (commit) TransactionMode.COMMIT else TransactionMode.ROLLBACK,
        )

        val result = GroovyExecClient.getInstance(project).execute(context)

        return buildString {
            if (result.statusCode != HttpStatus.SC_OK) {
                appendLine("Error (${result.statusCode}):")
                result.errorMessage?.let { appendLine(it) }
                result.errorDetailMessage?.let { appendLine(it) }
            } else {
                result.output?.takeIf { it.isNotBlank() }?.let {
                    appendLine("Output:")
                    appendLine(it)
                }
                result.result?.takeIf { it.isNotBlank() }?.let {
                    appendLine("Result:")
                    appendLine(it)
                }
                if (result.output.isNullOrBlank() && result.result.isNullOrBlank()) {
                    appendLine("Script executed successfully with no output.")
                }
            }
        }.trim()
    }

    @McpTool(name = "sap_commerce_execute_flexible_search")
    @McpDescription(
        """Executes a FlexibleSearch query on a SAP Commerce (Hybris) server via the HAC.
        |FlexibleSearch is the SAP Commerce query language for accessing the type system.
        |Returns query results as a formatted table.
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun executeFlexibleSearch(
        @McpDescription("FlexibleSearch query to execute, e.g. 'SELECT {pk}, {uid} FROM {User} WHERE {uid} = 'admin''")
        query: String,
        @McpDescription("Maximum number of result rows to return. Default is 200")
        maxCount: Int = 200,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
    ): String {
        val project = coroutineContext.mcpProject
        val connection = resolveHacConnection(project, connectionName)

        val context = FlexibleSearchExecContext(
            connection = connection,
            content = query,
            maxCount = maxCount.coerceIn(1, 200),
            locale = "en",
            dataSource = "master",
            user = null,
            timeout = connection.timeout,
        )

        val result = FlexibleSearchExecClient.getInstance(project).execute(context)

        return buildString {
            if (result.statusCode != HttpStatus.SC_OK) {
                appendLine("Error (${result.statusCode}):")
                result.errorMessage?.let { appendLine(it) }
                result.errorDetailMessage?.let { appendLine(it) }
            } else {
                result.output?.takeIf { it.isNotBlank() }?.let {
                    appendLine(it)
                }
                if (result.output.isNullOrBlank()) {
                    appendLine("Query executed successfully with no results.")
                }
            }
        }.trim()
    }

    @McpTool(name = "sap_commerce_list_hac_connections")
    @McpDescription(
        """Lists all configured HAC (Hybris Administration Console) connections for the current project.
        |Returns connection names and URLs. Use a connection name with other HAC tools to target a specific server."""
    )
    suspend fun listHacConnections(): String {
        val project = coroutineContext.mcpProject
        val connectionService = HacExecConnectionService.getInstance(project)
        val activeConnection = connectionService.activeConnection

        return buildString {
            appendLine("HAC Connections:")
            connectionService.connections.forEach { connection ->
                val active = if (connection.uuid == activeConnection.uuid) " (active)" else ""
                appendLine("  - ${connection.connectionName} (${connection.generatedURL})$active")
            }
        }.trim()
    }

    @McpTool(name = "sap_commerce_execute_sql")
    @McpDescription(
        """Executes a raw SQL query on a SAP Commerce (Hybris) server via the HAC.
        |This executes SQL directly against the underlying database (not FlexibleSearch).
        |Returns query results as a formatted table.
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun executeSql(
        @McpDescription("SQL query to execute against the underlying database")
        query: String,
        @McpDescription("Maximum number of result rows to return. Default is 200")
        maxCount: Int = 200,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
    ): String {
        val project = coroutineContext.mcpProject
        val connection = resolveHacConnection(project, connectionName)

        val context = FlexibleSearchExecContext(
            connection = connection,
            content = query,
            queryMode = QueryMode.SQL,
            maxCount = maxCount.coerceIn(1, 200),
            locale = "en",
            dataSource = "master",
            user = null,
            timeout = connection.timeout,
        )

        val result = FlexibleSearchExecClient.getInstance(project).execute(context)

        return buildString {
            if (result.statusCode != HttpStatus.SC_OK) {
                appendLine("Error (${result.statusCode}):")
                result.errorMessage?.let { appendLine(it) }
                result.errorDetailMessage?.let { appendLine(it) }
            } else {
                result.output?.takeIf { it.isNotBlank() }?.let {
                    appendLine(it)
                }
                if (result.output.isNullOrBlank()) {
                    appendLine("Query executed successfully with no results.")
                }
            }
        }.trim()
    }

    @McpTool(name = "sap_commerce_execute_impex")
    @McpDescription(
        """Executes an ImpEx script on a SAP Commerce (Hybris) server via the HAC.
        |ImpEx is the SAP Commerce import/export language for data manipulation.
        |Can either import data or validate the script without committing changes.
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun executeImpex(
        @McpDescription("ImpEx script content to execute on the SAP Commerce server")
        content: String,
        @McpDescription("Whether to only validate the ImpEx without importing. Default is false (import)")
        validate: Boolean = false,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
    ): String {
        val project = coroutineContext.mcpProject
        val connection = resolveHacConnection(project, connectionName)

        val defaultSettings = ImpExExecContext.defaultSettings(connection)
        val context = ImpExExecContext(
            connection = connection,
            content = content,
            executionMode = if (validate) ImpExExecutionMode.VALIDATE else ImpExExecutionMode.IMPORT,
            settings = defaultSettings,
        )

        val result = ImpExExecClient.getInstance(project).execute(context)

        val action = if (validate) "Validation" else "Import"
        return buildString {
            if (result.statusCode != HttpStatus.SC_OK) {
                appendLine("$action Error (${result.statusCode}):")
                result.errorMessage?.let { appendLine(it) }
                result.errorDetailMessage?.let { appendLine(it) }
            } else {
                result.output?.takeIf { it.isNotBlank() }?.let {
                    appendLine("$action Result:")
                    appendLine(it)
                }
                if (result.output.isNullOrBlank()) {
                    appendLine("$action completed successfully.")
                }
            }
        }.trim()
    }

    companion object {
        fun resolveHacConnection(project: Project, connectionName: String?): HacConnectionSettingsState {
            val connectionService = HacExecConnectionService.getInstance(project)

            if (connectionName.isNullOrBlank()) return connectionService.activeConnection

            return connectionService.connections.find {
                it.connectionName.equals(connectionName, ignoreCase = true)
                    || it.name?.equals(connectionName, ignoreCase = true) == true
            }
                ?: error("HAC connection '$connectionName' not found. Available: ${connectionService.connections.joinToString { it.connectionName }}")
        }
    }
}
