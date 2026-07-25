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
import sap.commerce.toolset.ai.mcp.McpConstants
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.solr.mcp.context.SolrListCoresMcpRequest
import sap.commerce.toolset.solr.mcp.context.SolrQueryExecMcpRequest

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
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = SolrQueryExecMcpRequest(connectionName, query, core, rows)
        val result = SolrMcpService.getInstance().executeQuery(request)
        return mapper.map(result)
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
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = SolrListCoresMcpRequest(connectionName)
        val cores = SolrMcpService.getInstance().listCores(request)
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
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val connections = SolrMcpService.getInstance().listConnections()
        return mapper.map(connections)
    }
}
