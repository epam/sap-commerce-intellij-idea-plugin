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
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.mcp.context.FxSMcpExecRequest
import sap.commerce.toolset.flexibleSearch.mcp.context.FxSTransformMcpContext

class FxSMcpToolset : McpToolset {

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
        maxCount: Int = FlexibleSearchExecConstants.Defaults.MAX_COUNT,
        @McpDescription("Optional locale for the query. Default is 'en'")
        locale: String = FlexibleSearchExecConstants.Defaults.LOCALE,
        @McpDescription("Optional data source for the query. Default is 'master'")
        dataSource: String = FlexibleSearchExecConstants.Defaults.DATA_SOURCE,
        @McpDescription("Optional user to execute the query as. Default uses the current session user")
        user: String? = null,
        @McpDescription("Optional timeout. Default uses timeout of the connection")
        timeout: Int? = null,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val execRequest = FxSMcpExecRequest(connectionName, QueryMode.FlexibleSearch, query, maxCount, locale, dataSource, user, timeout)
        val result = FxSMcpService.getInstance(project).execute(execRequest)
        return mapper.map(result)
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
        maxCount: Int = FlexibleSearchExecConstants.Defaults.MAX_COUNT,
        @McpDescription("Optional locale for the query. Default is 'en'")
        locale: String = FlexibleSearchExecConstants.Defaults.LOCALE,
        @McpDescription("Optional data source for the query. Default is 'master'")
        dataSource: String = FlexibleSearchExecConstants.Defaults.DATA_SOURCE,
        @McpDescription("Optional user to execute the query as. Default uses the current session user")
        user: String? = null,
        @McpDescription("Optional timeout. Default uses timeout of the connection")
        timeout: Int? = null,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val execRequest = FxSMcpExecRequest(connectionName, QueryMode.SQL, query, maxCount, locale, dataSource, user, timeout)
        val result = FxSMcpService.getInstance(project).execute(execRequest)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_execute_and_transform_flexible_search")
    @McpDescription(
        """Executes a FlexibleSearch query on a SAP Commerce server and converts the results with the applicable and chosen transformer.
        |The output format depends on the selected transformer (specified via 'transformerName') — for example, the ImpEx transformer
        |uses the type system to resolve correct attribute types, nested FK paths (e.g. catalogVersion(catalog(id),version)),
        |localized attributes (lang=xx), collection delimiters, and [unique=true] modifiers derived from WHERE clause equality conditions.
        |Enum attribute values are resolved from their runtime PKs to their codes via follow-up queries.
        |FK attribute values are resolved from their runtime PKs to their natural key strings via follow-up queries.
        |Returns the transformed text along with metadata (primary type, column count, row count).
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun executeAndTransform(
        @McpDescription("ID of the FlexibleSearch applicable Transformer")
        transformerId: String,
        @McpDescription("FlexibleSearch query to execute and convert, e.g. 'SELECT {pk}, {code}, {catalogVersion} FROM {Product}'")
        query: String,
        @McpDescription("Maximum number of result rows to return. Default is 200")
        maxCount: Int = FlexibleSearchExecConstants.Defaults.MAX_COUNT,
        @McpDescription("Optional locale for the query. Default is 'en'")
        locale: String = FlexibleSearchExecConstants.Defaults.LOCALE,
        @McpDescription("Optional data source for the query. Default is 'master'")
        dataSource: String = FlexibleSearchExecConstants.Defaults.DATA_SOURCE,
        @McpDescription("Optional user to execute the query as. Default uses the current session user")
        user: String? = null,
        @McpDescription("Optional timeout. Default uses timeout of the connection")
        timeout: Int? = null,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
        @McpDescription("Optional flag to include all unique attributes from the type. Default is 'false'")
        includeTypeSystemUnique: Boolean = false,
        @McpDescription("Flag to include result data rows in the output. Set to 'true' whenever the user wants actual data (not just the ImpEx header). Default is 'true'.")
        includeData: Boolean = true,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val execRequest = FxSMcpExecRequest(connectionName, QueryMode.FlexibleSearch, query, maxCount, locale, dataSource, user, timeout)
        val transformContext = FxSTransformMcpContext(
            transformerId = transformerId,
            query = query,
            includeTypeSystemUnique = includeTypeSystemUnique,
            includeData = includeData,
            execRequest = execRequest,
        )
        val result = FxSMcpService.getInstance(project).transform(transformContext)
        return mapper.map(result)
    }
}
