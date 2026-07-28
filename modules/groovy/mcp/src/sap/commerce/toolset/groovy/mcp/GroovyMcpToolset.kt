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

package sap.commerce.toolset.groovy.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import sap.commerce.toolset.ai.mcp.McpConstants
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.groovy.mcp.context.GroovyExecMcpRequest
import sap.commerce.toolset.groovy.mcp.context.GroovyTransformMcpRequest

class GroovyMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_execute_groovy")
    @McpDescription(
        """Executes a Groovy script on a SAP Commerce (Hybris) server via the HAC (Hybris Administration Console).
        |Returns the script's console output and execution result.
        |The script runs in the server's context with access to all SAP Commerce APIs and Spring beans.
        |Requires a configured and authenticated HAC connection."""
    )
    suspend fun executeGroovy(
        @McpDescription("Groovy script source code to execute on the SAP Commerce server")
        script: String,
        @McpDescription("Whether to commit the transaction after execution. Default is false (rollback)")
        commit: Boolean = false,
        @McpDescription(McpConstants.Descriptions.HAC_CONNECTION_NAME)
        connectionName: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = GroovyExecMcpRequest(connectionName, script, commit)
        val result = GroovyMcpService.getInstance().execute(request)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_transform_groovy")
    @McpDescription(
        """Transforms a Groovy script into a SAP Commerce ImpEx using the specified transformer.
        |The generated ImpEx contains an INSERT_UPDATE Script item with the script content and
        |an INSERT_UPDATE ScriptingJob item referencing it via model:// URI.
        |Use sap_commerce_list_transformers to discover available Groovy transformers and their IDs.
        |No HAC connection is required — this is a local, offline transformation."""
    )
    suspend fun transformGroovy(
        @McpDescription("ID of the Groovy-applicable transformer (e.g. 'groovy-to-impex-script'). Use sap_commerce_list_transformers to list available IDs.")
        transformerId: String,
        @McpDescription("Groovy script source code to transform")
        script: String,
        @McpDescription("Name used as the Script item code and ScriptingJob code prefix (e.g. 'removeTestUserScript')")
        scriptName: String,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = GroovyTransformMcpRequest(
            transformerId = transformerId,
            script = script,
            scriptName = scriptName,
        )
        val result = GroovyMcpService.getInstance().transform(request)
        return mapper.map(result)
    }
}
