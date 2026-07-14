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
import sap.commerce.toolset.ai.mcp.resolveMapper

class ImpExMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_execute_impex")
    @McpDescription(
        """Executes an ImpEx script on a SAP Commerce (Hybris) server via the HAC.
        |ImpEx is the SAP Commerce import/export language for data manipulation.
        |Can either import data or validate the script without committing changes.
        |Requires a configured and authenticated HAC connection.
        |The validate=true dry run exercises the real server-side import pipeline (type system, interceptors,
        |persistence-layer constraints), which the local 'sap_commerce_validate_impex_syntax' tool cannot reach —
        |but it does NOT catch pure ImpEx syntax/semantic mistakes (unknown macros, quoting issues, invalid
        |modifiers, missing header parameters) as precisely or as fast as that local tool, and it costs a
        |server round-trip. Prefer 'sap_commerce_validate_impex_syntax' first for authoring/linting an ImpEx
        |script; use this tool's validate=true when you specifically need to confirm server-side import behavior."""
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
        val context = ImpExMcpContext(connectionName, content, validate)
        val result = ImpExMcpService.getInstance(project).execute(context)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_validate_impex_syntax")
    @McpDescription(
        """Statically validates an ImpEx script the same way the IDE editor would: runs ImpEx syntax highlighting
        |and all registered ImpEx inspections. This is the project's LOCAL model — it does NOT query a remote
        |server and does NOT require a HAC connection, so it is instant and side-effect-free.
        |PREFER THIS TOOL over 'sap_commerce_execute_impex's validate=true dry run for authoring/linting an ImpEx
        |script: it catches a wider and more precise range of ImpEx-specific mistakes that the server-side dry
        |run does not flag as such, including unknown/unresolved macros and unused macro definitions, incorrect
        |${'$'}config macro usage, missing or extraneous quoting of value strings, unknown/unresolved item types and
        |attributes, invalid or unknown modifier values (mode, processor, translator, cellDecorator, lang,
        |disable.interceptor.types, boolean modifiers), missing header parameter names, incomplete header
        |abbreviations, missing/orphan value groups, and duplicate unique-value columns. It does NOT exercise the
        |actual server-side import pipeline (interceptors, persistence-layer constraints) — use
        |'sap_commerce_execute_impex' with validate=true when you need to confirm real server-side import behavior.
        |Provide either 'content' (validated in-memory, no file needs to exist on disk) or 'filePath' (validates an
        |existing project file, honoring any unsaved editor changes; 'content' is ignored when 'filePath' resolves).
        |Returns a JSON object: {"file"?, "valid", "issues": [{"severity", "message", "line", "column"}]}."""
    )
    suspend fun validateImpExSyntax(
        @McpDescription("ImpEx script content to validate in-memory. Ignored if 'filePath' is provided and resolves to an existing file.")
        content: String? = null,
        @McpDescription("Optional path (absolute, or relative to the project directory) to an existing ImpEx file in the project to validate instead of 'content'.")
        filePath: String? = null,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val context = ImpExValidationContext(content, filePath)
        val result = ImpExValidationMcpService.getInstance(project).validate(context)
        return mapper.map(result)
    }
}
