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

package sap.commerce.toolset.hac.mcp

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.AuthMode
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.mcp.dto.HacConnectionDto
import sap.commerce.toolset.hac.mcp.dto.HacConnectionListResponse

@Service(Service.Level.PROJECT)
class HacMcpService(private val project: Project) {

    /**
     * Resolves the HAC connection targeted by a HAC-backed MCP tool call: the connection whose name
     * matches [connectionName] (case-insensitively, against either the display or configured name), or
     * the active connection when [connectionName] is null/blank. Errors with the list of available
     * connection names when no match is found.
     */
    fun resolveConnection(connectionName: String?): HacConnectionSettingsState {
        val connectionService = HacExecConnectionService.getInstance(project)

        val connection = if (connectionName.isNullOrBlank()) connectionService.activeConnection
        else connectionService.connections.find {
            it.connectionName.equals(connectionName, ignoreCase = true)
                || it.name?.equals(connectionName, ignoreCase = true) == true
        }
            ?: error("HAC connection '$connectionName' not found. Available: ${connectionService.connections.joinToString { it.connectionName }}")

        if (connection.authMode == AuthMode.AUTOMATIC) return connection

        error(
            "HAC connection '${connection.connectionName}' uses Manual (browser) authentication, " +
                "which is not yet supported by the logger MCP tools. " +
                "Switch the connection to '${AuthMode.AUTOMATIC.title}' to use this tool."
        )
    }


    fun listConnections(): HacConnectionListResponse {
        val connectionService = HacExecConnectionService.getInstance(project)
        val activeUuid = connectionService.activeConnection.uuid
        val items = connectionService.connections.map { conn ->
            HacConnectionDto(
                name = conn.connectionName,
                url = conn.generatedURL,
                active = conn.uuid == activeUuid,
                authMode = conn.authMode.name,
                supportedByMcp = conn.authMode == AuthMode.AUTOMATIC,
            )
        }
        return HacConnectionListResponse(matched = items.size, total = items.size, items = items)
    }

    companion object {
        fun getInstance(project: Project): HacMcpService = project.service()
    }
}
