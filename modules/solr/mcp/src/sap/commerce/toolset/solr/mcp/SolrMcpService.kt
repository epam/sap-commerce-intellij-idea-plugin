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

import com.intellij.mcpserver.project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import org.apache.http.HttpStatus
import sap.commerce.toolset.solr.exec.SolrExecClient
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.context.SolrQueryExecContext
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import sap.commerce.toolset.solr.mcp.context.SolrListCoresMcpRequest
import sap.commerce.toolset.solr.mcp.context.SolrQueryExecMcpRequest
import sap.commerce.toolset.solr.mcp.dto.SolrConnectionDto
import sap.commerce.toolset.solr.mcp.dto.SolrConnectionsDto
import sap.commerce.toolset.solr.mcp.dto.SolrCoreDto
import sap.commerce.toolset.solr.mcp.dto.SolrCoresDto

@Service(Service.Level.PROJECT)
class SolrMcpService(private val project: Project) {

    fun listConnections(): SolrConnectionsDto {
        val connectionService = SolrExecConnectionService.getInstance(project)
        val activeUuid = connectionService.activeConnection.uuid
        val items = connectionService.connections.map {
            SolrConnectionDto(it.connectionName, it.generatedURL, it.uuid == activeUuid)
        }
        return SolrConnectionsDto(
            matched = items.size,
            total = items.size,
            items = items,
        )
    }

    fun listCores(request: SolrListCoresMcpRequest): SolrCoresDto {
        val connection = request.connection(project)
        val connectionService = SolrExecConnectionService.getInstance(project)
        val credentials = connectionService.getCredentials(connection)
        val username = credentials.userName ?: ""
        val password = credentials.getPasswordAsString() ?: ""

        val items = SolrExecClient.getInstance(project).coresData(connection, username, password)
            .map { SolrCoreDto(it.core, it.docs) }

        return SolrCoresDto(
            connection = connection.connectionName,
            matched = items.size,
            total = items.size,
            items = items,
        )
    }

    suspend fun executeQuery(request: SolrQueryExecMcpRequest): String {
        val connection = request.connection(project)
        val execContext = SolrQueryExecContext(
            connection = connection,
            content = request.query,
            core = request.core,
            rows = request.rows.coerceIn(1, 500),
        )

        val result = SolrExecClient.getInstance(project).execute(execContext)

        return buildString {
            if (result.statusCode != HttpStatus.SC_OK) {
                appendLine("Error (${result.statusCode}):")
                result.errorMessage?.let { appendLine(it) }
                result.errorDetailMessage?.let { appendLine(it) }
            } else {
                result.output?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
                if (result.output.isNullOrBlank()) appendLine("Query executed successfully with no results.")
            }
        }.trim()
    }

    /**
     * Resolves the Solr connection targeted by a Solr MCP tool call: the connection whose name matches
     * [connectionName] (case-insensitively, against either the display or configured name), or the
     * active connection when [connectionName] is null/blank. Errors with the list of available
     * connection names when no match is found.
     */
    fun resolveConnection(connectionName: String?): SolrConnectionSettingsState {
        val connectionService = SolrExecConnectionService.getInstance(project)

        if (connectionName.isNullOrBlank()) return connectionService.activeConnection

        return connectionService.connections.find {
            it.connectionName.equals(connectionName, ignoreCase = true)
                || it.name?.equals(connectionName, ignoreCase = true) == true
        }
            ?: error("Solr connection '$connectionName' not found. Available: ${connectionService.connections.joinToString { it.connectionName }}")
    }

    companion object {
        fun getInstance(project: Project): SolrMcpService = project.service()
        suspend fun getInstance(): SolrMcpService = currentCoroutineContext().project.service()
    }
}
