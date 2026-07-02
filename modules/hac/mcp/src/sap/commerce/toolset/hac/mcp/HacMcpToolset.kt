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

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.*
import sap.commerce.toolset.ai.mcp.mcpProject
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.AuthMode

class HacMcpToolset : McpToolset {

    private val json = Json { prettyPrint = true }

    @McpTool(name = "sap_commerce_list_hac_connections")
    @McpDescription(
        """Lists all configured HAC (Hybris Administration Console) connections for the current project as a JSON object.
        |Shape: {"connections": [{"name": String, "url": String, "active": Boolean, "authMode": "AUTOMATIC" | "MANUAL", "supportedByMcp": Boolean}]}.
        | - name: pass it to other HAC tools to target a specific server;
        | - active: whether it is the currently active connection;
        | - authMode: AUTOMATIC (credentials persisted in the IDE) or MANUAL (interactive browser-based authentication);
        | - supportedByMcp: whether this connection can currently be used by MCP tools.
        |IMPORTANT: connections with authMode "MANUAL" are NOT supported for LLM/MCP usage right now (supportedByMcp = false), because they require an interactive browser login that the model cannot perform; calling other HAC tools against such a connection will fail. Support for MANUAL authentication is planned for a later version of the plugin."""
    )
    suspend fun listHacConnections(): String {
        val project = currentCoroutineContext().mcpProject
        val connectionService = HacExecConnectionService.getInstance(project)
        val activeConnection = connectionService.activeConnection

        val payload = buildJsonObject {
            putJsonArray("connections") {
                connectionService.connections.forEach { connection ->
                    addJsonObject {
                        put("name", connection.connectionName)
                        put("url", connection.generatedURL)
                        put("active", connection.uuid == activeConnection.uuid)
                        put("authMode", connection.authMode.name)
                        put("supportedByMcp", connection.authMode == AuthMode.AUTOMATIC)
                    }
                }
            }
        }

        return json.encodeToString(JsonObject.serializer(), payload)
    }
}
