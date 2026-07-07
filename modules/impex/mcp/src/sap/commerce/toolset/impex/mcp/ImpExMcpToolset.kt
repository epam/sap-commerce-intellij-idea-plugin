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

package sap.commerce.toolset.impex.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveHacConnection
import sap.commerce.toolset.ai.mcp.resolveMapper

class ImpExMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_execute_impex")
    @McpDescription(
        """Executes an ImpEx script on a SAP Commerce (Hybris) server via the HAC.
        |ImpEx is the SAP Commerce import/export language for data manipulation.
        |Can either import data or validate the script without committing changes.
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun executeImpEx(
        @McpDescription("ImpEx script content to execute on the SAP Commerce server")
        content: String,
        @McpDescription("Whether to only validate the ImpEx without importing. Default is false (import)")
        validate: Boolean = false,
        @McpDescription("Optional HAC connection name. Uses the active connection if not specified")
        connectionName: String? = null,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val connection = resolveHacConnection(project, connectionName)
        val result = ImpExMcpService.getInstance(project).execute(connection, content, validate)
        return mapper.map(result)
    }
}
