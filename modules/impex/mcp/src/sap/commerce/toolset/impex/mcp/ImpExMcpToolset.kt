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
import kotlinx.coroutines.currentCoroutineContext
import org.apache.http.HttpStatus
import sap.commerce.toolset.ai.mcp.mcpProject
import sap.commerce.toolset.ai.mcp.resolveHacConnection
import sap.commerce.toolset.impex.exec.ImpExExecClient
import sap.commerce.toolset.impex.exec.context.ImpExExecContext
import sap.commerce.toolset.impex.exec.context.ImpExExecutionMode

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
    ): String {
        val project = currentCoroutineContext().mcpProject
        val connection = resolveHacConnection(project, connectionName)

        val defaultSettings = ImpExExecContext.defaultSettings(connection)
        val context = ImpExExecContext(
            connection = connection,
            content = content,
            executionMode = if (validate) ImpExExecutionMode.VALIDATE else ImpExExecutionMode.IMPORT,
            settings = defaultSettings,
        )

        val result = ImpExExecClient.getInstance(project).execute(context)

        val action = if (validate) "Validation" else "Import"
        return buildString {
            if (result.statusCode != HttpStatus.SC_OK) {
                appendLine("$action Error (${result.statusCode}):")
                result.errorMessage?.let { appendLine(it) }
                result.errorDetailMessage?.let { appendLine(it) }
            } else {
                result.output?.takeIf { it.isNotBlank() }?.let {
                    appendLine("$action Result:")
                    appendLine(it)
                }
                if (result.output.isNullOrBlank()) {
                    appendLine("$action completed successfully.")
                }
            }
        }.trim()
    }
}
