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

package sap.commerce.toolset.shared.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper

class TransformerMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_list_transformers")
    @McpDescription(
        """Lists all registered result transformers for SAP Commerce query languages.
        |Transformers convert query results (e.g. FlexibleSearch) into other formats (e.g. ImpEx).
        |Results are grouped by the language each transformer is applicable to.
        |Use the optional 'languageId' parameter to filter by a specific language (e.g. 'FlexibleSearch').
        |When no transformers are registered for the given language, an empty list is returned.
        |Returns transformer id, name, description, and language details for each registered transformer."""
    )
    suspend fun listTransformers(
        @McpDescription("Optional language ID to filter by (e.g. 'FlexibleSearch'). Matched case-insensitively against language ID and display name. Returns transformers for all languages when omitted.")
        languageId: String? = null,
        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val project = currentCoroutineContext().project
        val result = TransformerMcpService.getInstance(project).list(languageId)
        return mapper.map(result)
    }
}
