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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.http.HttpStatus
import sap.commerce.toolset.solr.exec.SolrExecClient
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.context.SolrQueryExecContext
import sap.commerce.toolset.solr.mcp.dto.SolrConnectionDto
import sap.commerce.toolset.solr.mcp.dto.SolrConnectionListResponse
import sap.commerce.toolset.solr.mcp.dto.SolrCoreDto
import sap.commerce.toolset.solr.mcp.dto.SolrCoreListResponse

@Service(Service.Level.PROJECT)
class SolrMcpService(private val project: Project) {

    fun listConnections(): SolrConnectionListResponse {
        val connectionService = SolrExecConnectionService.getInstance(project)
        val activeUuid = connectionService.activeConnection.uuid
        val items = connectionService.connections.map {
            SolrConnectionDto(it.connectionName, it.generatedURL, it.uuid == activeUuid)
        }
        return SolrConnectionListResponse(
            matched = items.size,
            total = items.size,
            items = items,
        )
    }

    fun listCores(context: SolrListCoresMcpContext): SolrCoreListResponse {
        val connection = resolveSolrConnection(project, context.connectionName)
        val connectionService = SolrExecConnectionService.getInstance(project)
        val credentials = connectionService.getCredentials(connection)
        val username = credentials.userName ?: ""
        val password = credentials.getPasswordAsString() ?: ""

        val items = SolrExecClient.getInstance(project).coresData(connection, username, password)
            .map { SolrCoreDto(it.core, it.docs) }

        return SolrCoreListResponse(
            connection = connection.connectionName,
            matched = items.size,
            total = items.size,
            items = items,
        )
    }

    suspend fun executeQuery(context: SolrQueryMcpContext): String {
        val connection = resolveSolrConnection(project, context.connectionName)
        val execContext = SolrQueryExecContext(
            connection = connection,
            content = context.query,
            core = context.core,
            rows = context.rows.coerceIn(1, 500),
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

    companion object {
        fun getInstance(project: Project): SolrMcpService = project.service()
    }
}
