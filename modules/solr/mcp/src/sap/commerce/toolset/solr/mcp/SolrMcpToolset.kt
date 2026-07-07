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

package sap.commerce.toolset.solr.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper

class SolrMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_solr_query")
    @McpDescription(
        """Executes a Solr query against a SAP Commerce Solr server.
        |Returns the raw JSON response from Solr.
        |Requires a configured Solr connection with valid credentials."""
    )
    suspend fun solrQuery(
        @McpDescription("Solr query string, e.g. '*:*' or 'name:product1'")
        query: String,
        @McpDescription("Name of the Solr core to query against")
        core: String,
        @McpDescription("Maximum number of rows to return. Default is 10, max is 500")
        rows: Int = 10,
        @McpDescription("Optional Solr connection name. Uses the active connection if not specified")
        connectionName: String? = null,
    ): String {
        val project = currentCoroutineContext().project
        val context = SolrQueryMcpContext(connectionName, query, core, rows)
        return SolrMcpService.getInstance(project).executeQuery(context)
    }

    @McpTool(name = "sap_commerce_solr_list_cores")
    @McpDescription(
        """Lists all available Solr cores on a SAP Commerce Solr server.
        |Returns a JSON object: {"connection", "matched", "total", "items": [{"core", "docs"}]}.
        |Requires a configured Solr connection with valid credentials."""
    )
    suspend fun solrListCores(
        @McpDescription("Optional Solr connection name. Uses the active connection if not specified")
        connectionName: String? = null,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val context = SolrListCoresMcpContext(connectionName)
        val cores = SolrMcpService.getInstance(project).listCores(context)
        return mapper.map(cores)
    }

    @McpTool(name = "sap_commerce_list_solr_connections")
    @McpDescription(
        """Lists all configured Solr connections for the current project as a JSON object.
        |Shape: {"matched", "total", "items": [{"name", "url", "active"}]}.
        | - name: pass it to other Solr tools to target a specific server;
        | - active: whether it is the currently active connection."""
    )
    suspend fun listSolrConnections(
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val connections = SolrMcpService.getInstance(project).listConnections()
        return mapper.map(connections)
    }
}
