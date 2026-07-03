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

package sap.commerce.toolset.flexibleSearch.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import kotlinx.coroutines.currentCoroutineContext
import org.apache.http.HttpStatus
import sap.commerce.toolset.ai.mcp.resolveHacConnection
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode

class FlexibleSearchMcpToolset : McpToolset {

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
        @McpDescription("Optional locale for the query. Default is 'en'")
        locale: String = "en",
        @McpDescription("Optional data source for the query. Default is 'master'")
        dataSource: String = "master",
        @McpDescription("Optional user to execute the query as. Default uses the current session user")
        user: String? = null,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
    ): String {
        val project = currentCoroutineContext().project
        val connection = resolveHacConnection(project, connectionName)

        val context = FlexibleSearchExecContext(
            connection = connection,
            content = query,
            maxCount = maxCount.coerceIn(1, 200),
            locale = locale,
            dataSource = dataSource,
            user = user,
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
        @McpDescription("Optional locale for the query. Default is 'en'")
        locale: String = "en",
        @McpDescription("Optional data source for the query. Default is 'master'")
        dataSource: String = "master",
        @McpDescription("Optional user to execute the query as. Default uses the current session user")
        user: String? = null,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
    ): String {
        val project = currentCoroutineContext().project
        val connection = resolveHacConnection(project, connectionName)

        val context = FlexibleSearchExecContext(
            connection = connection,
            content = query,
            queryMode = QueryMode.SQL,
            maxCount = maxCount.coerceIn(1, 200),
            locale = locale,
            dataSource = dataSource,
            user = user,
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
}
