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

package sap.commerce.toolset.businessProcess.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import sap.commerce.toolset.ai.mcp.McpConstants
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.businessProcess.mcp.context.BpSearchRequest
import sap.commerce.toolset.businessProcess.mcp.context.SearchScope

class BpMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_list_business_processes")
    @McpDescription(
        """Lists the Business Processes defined in the current project's SAP Commerce (Hybris) as xml files with the specific schema.
        |This is the project's LOCAL model, parsed from the `*.xml` definitions.
        |Returns a JSON object representing list of business processes names and absolute file location for every xml file."""
    )
    suspend fun listItemTypes(
        @McpDescription(
            """Optional scope filter used to shrink the response and save tokens, with possible options:
            |- CUSTOM: only business processes found in the custom extensions
            |- ALL: business processes found in the custom extensions
            |If the value is a valid regular expression it is matched against each item type name with a regex search (e.g. '^Product$' for an exact match, 'Product' for partial, or '(?i)catalog' for case-insensitivity);
            |otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all item types."""
        )
        scope: String = SearchScope.CUSTOM.name,
        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to item types owned by those extensions (e.g. 'core,basecommerce' or 'myprojectcore').
            |Matched case-insensitively and exactly against each item type's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include item types from all extensions."""
        )
        extensions: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val searchScope = SearchScope.resolve(scope)
        val request = BpSearchRequest(searchScope, extensions)
        val result = BpMcpService.getInstance().searchBusinessProcesses(request)
        return mapper.map(result)
    }

}
