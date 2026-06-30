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

package sap.commerce.toolset.ai.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.ai.mcp.mcpProject
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import kotlin.coroutines.coroutineContext

class HacMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_list_hac_connections")
    @McpDescription(
        """Lists all configured HAC (Hybris Administration Console) connections for the current project.
        |Returns connection names and URLs. Use a connection name with other HAC tools to target a specific server."""
    )
    suspend fun listHacConnections(): String {
        val project = currentCoroutineContext().mcpProject
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
}
