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
import com.intellij.openapi.application.readAction
import kotlinx.coroutines.currentCoroutineContext
import org.apache.http.HttpStatus
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.impex.FxSColumn
import sap.commerce.toolset.flexibleSearch.impex.FxSImpExHeaderBuilder
import sap.commerce.toolset.flexibleSearch.impex.FxSQueryAnalyzer
import sap.commerce.toolset.flexibleSearch.impex.FxSQueryInfo
import sap.commerce.toolset.flexibleSearch.mcp.dto.FxSImpExResult
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchElementFactory
import sap.commerce.toolset.hac.mcp.HacMcpService

class FxSToImpExMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_fxs_results_to_impex")
    @McpDescription(
        """Executes a FlexibleSearch query on a SAP Commerce server and converts the results into an ImpEx INSERT_UPDATE script.
        |The generated ImpEx uses the type system to resolve correct attribute types, nested FK paths (e.g. catalogVersion(catalog(id),version)),
        |localized attributes (lang=xx), collection delimiters, and [unique=true] modifiers derived from WHERE clause equality conditions.
        |Returns the ImpEx text along with metadata (primary type, column count, row count).
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun flexibleSearchResultsToImpEx(
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
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val connection = HacMcpService.getInstance(project).resolveConnection(connectionName)

        val execContext = FlexibleSearchExecContext(
            connection = connection,
            content = query,
            queryMode = QueryMode.FlexibleSearch,
            maxCount = maxCount.coerceIn(1, 200),
            locale = locale,
            dataSource = dataSource,
            user = user,
            timeout = connection.timeout,
        )

        val execResult = FlexibleSearchExecClient.getInstance(project).execute(execContext)

        if (execResult.statusCode != HttpStatus.SC_OK) {
            return mapper.map(
                FxSImpExResult(
                    connection = connection.connectionName,
                    success = false,
                    error = execResult.errorMessage,
                    errorDetail = execResult.errorDetailMessage,
                )
            )
        }

        val headers = execResult.headers
        val rows = execResult.rows

        if (headers == null || rows == null) {
            return mapper.map(
                FxSImpExResult(
                    connection = connection.connectionName,
                    success = false,
                    error = "Query returned no structured data (headers or rows missing).",
                )
            )
        }

        // Parse query PSI in a read action to extract column metadata
        val queryInfo = readAction {
            val psiFile = FlexibleSearchElementFactory.createFile(project, query)
            FxSQueryAnalyzer.analyze(psiFile, headers)
        }

        val params = FxSImpExHeaderBuilder.buildParams(queryInfo, project)
        val impexContent = buildImpEx(queryInfo, params, rows)

        return mapper.map(
            FxSImpExResult(
                connection = connection.connectionName,
                success = true,
                primaryType = queryInfo.primaryType,
                columnCount = params.size,
                rowCount = rows.size,
                impex = impexContent,
            )
        )
    }

    private fun buildImpEx(
        queryInfo: FxSQueryInfo,
        params: List<sap.commerce.toolset.flexibleSearch.impex.FxSImpExParam>,
        rows: List<List<String>>,
    ): String {
        val columnIndexMap = queryInfo.columns
            .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }

        return buildString {
            append("INSERT_UPDATE ${queryInfo.primaryType}")
            params.forEach { param -> append("; ${param.render()}") }
            appendLine()

            rows.forEach { row ->
                append("")
                columnIndexMap.forEach { srcIdx ->
                    val cell = row.getOrNull(srcIdx) ?: ""
                    val value = if (cell == "null") "" else cell
                    append("; $value")
                }
                appendLine()
            }
        }
    }
}
