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

package sap.commerce.toolset.properties.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import sap.commerce.toolset.ai.mcp.McpConstants
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.hac.mcp.HacMcpConstants
import sap.commerce.toolset.properties.mcp.context.CxComparePropertiesMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxListRemotePropertiesMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxPropertyReportMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxValidatePropertiesMcpRequest
import sap.commerce.toolset.properties.mcp.context.CxWriteRemotePropertyMcpRequest
import sap.commerce.toolset.properties.meta.CxPropertyRuleSeverity

class PropertiesMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_list_properties")
    @McpDescription(
        """Lists the configuration properties a running SAP Commerce (Hybris) instance currently has, read from its configstore via the HAC.
        |Returns {"connection", "keyFilter"?, "valueFilter"?, "returned", "total", "truncated", "items": [{"key", "value"}]}.
        |These are the values the instance is running with right now — NOT what the project's files declare. Use sap_commerce_report_property or sap_commerce_compare_properties to see the two together.
        |An instance typically has several thousand properties, so always pass 'keyFilter' unless you genuinely need all of them: the filters are applied by the instance itself, which is the difference between one small response and a very large one. 'truncated' is true when a narrower filter or a larger 'limit' would return more.
        |Requires a configured and authenticated HAC connection.
        |PRECONDITION: only call this tool against a connection whose authMode is AUTOMATIC (supportedByMcp = true in sap_commerce_list_hac_connections). If the user asks to use a connection that uses MANUAL (browser) authentication, do NOT call this tool — instead tell the user that connection is not supported by MCP tools yet and offer an AUTOMATIC one. Calling it against a MANUAL connection will fail."""
    )
    suspend fun listProperties(
        @McpDescription(CxPropertiesMcpConstants.Descriptions.KEY_FILTER)
        keyFilter: String? = null,
        @McpDescription("Optional case-insensitive substring the property VALUE must contain. Applied by the instance. Omit to filter by key only.")
        valueFilter: String? = null,
        @McpDescription(CxPropertiesMcpConstants.Descriptions.LIMIT)
        limit: Int = CxPropertiesMcpConstants.DEFAULT_LIMIT,
        @McpDescription(HacMcpConstants.Descriptions.CONNECTION_NAME_AUTH)
        connectionName: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = CxListRemotePropertiesMcpRequest(connectionName, keyFilter, valueFilter, positiveLimit(limit))
        val result = PropertiesMcpService.getInstance().listRemoteProperties(request)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_set_property")
    @McpDescription(
        """Stores a single property in the configstore of a running SAP Commerce (Hybris) instance, creating it if it does not exist.
        |MODIFIES THE RUNNING INSTANCE. The change takes effect immediately and outlives this conversation; it is not written to any file in the project, so it will be lost on the next deployment unless the project declares it too.
        |Ask the user before calling this against anything other than a local or development instance, and tell them which connection you are about to write to.
        |Returns {"connection", "key", "success", "value"?, "error"?}.
        |PRECONDITION: only call this tool against a connection whose authMode is AUTOMATIC (supportedByMcp = true in sap_commerce_list_hac_connections). If the user asks to use a connection that uses MANUAL (browser) authentication, do NOT call this tool — instead tell the user that connection is not supported by MCP tools yet and offer an AUTOMATIC one. Calling it against a MANUAL connection will fail."""
    )
    suspend fun setProperty(
        @McpDescription("Property key to store, e.g. 'storefrontContextRoot'. Must not be blank or contain whitespace.")
        key: String,
        @McpDescription("Value to store. Pass an empty string to set the property to an empty value.")
        value: String,
        @McpDescription(HacMcpConstants.Descriptions.CONNECTION_NAME_AUTH)
        connectionName: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = CxWriteRemotePropertyMcpRequest(connectionName, key, value)
        val result = PropertiesMcpService.getInstance().setRemoteProperty(request)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_delete_property")
    @McpDescription(
        """Removes a single property from the configstore of a running SAP Commerce (Hybris) instance.
        |MODIFIES THE RUNNING INSTANCE AND CANNOT BE UNDONE. The instance falls back to whatever its property files declare, which may be nothing at all.
        |Always ask the user before calling this, name the connection and the key, and do not call it speculatively — read the current value with sap_commerce_list_properties first if you are unsure what is about to be lost.
        |Returns {"connection", "key", "success", "error"?}.
        |PRECONDITION: only call this tool against a connection whose authMode is AUTOMATIC (supportedByMcp = true in sap_commerce_list_hac_connections). If the user asks to use a connection that uses MANUAL (browser) authentication, do NOT call this tool — instead tell the user that connection is not supported by MCP tools yet and offer an AUTOMATIC one. Calling it against a MANUAL connection will fail."""
    )
    suspend fun deleteProperty(
        @McpDescription("Property key to remove from the instance.")
        key: String,
        @McpDescription(HacMcpConstants.Descriptions.CONNECTION_NAME_AUTH)
        connectionName: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = CxWriteRemotePropertyMcpRequest(connectionName, key)
        val result = PropertiesMcpService.getInstance().deleteRemoteProperty(request)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_report_property")
    @McpDescription(
        """Reports where a single property comes from: every file of this project which declares it, which declaration the platform actually applies, and what a running instance currently has.
        |Returns {"key", "declared", "effectiveValue"?, "rawValue"?, "declarations": [{"value", "file", "scope", "wins", "ignored", "extension"?, "path"?}], "rule"?, "connection"?, "remoteValue"?, "differsFromRemote"?, "remoteError"?}.
        |'declarations' lists EVERY declaration, not only the winning one, in chain order — 'wins' marks the one that applies and 'ignored' marks a file the running system never reads (an extension localextensions.xml does not list). 'rawValue' is present only when the winning declaration contains ${'$'}{...} placeholders, and 'effectiveValue' is the same value with them expanded.
        |'rule' is present when SAP Commerce Cloud documents the property as one the project should not be declaring — see sap_commerce_validate_properties.
        |This is the tool to use to answer "where does this property come from?", "why is this value not taking effect?" or "which file should I change?"."""
    )
    suspend fun reportProperty(
        @McpDescription("Exact property key to report on, e.g. 'db.url'. Not a filter — pass the key itself.")
        key: String,
        @McpDescription(
            """Whether to also read the property from a running instance, which costs one HAC request.
            |Default true. Pass false to report on the project's files alone, with no connection needed.
            |A connection which cannot be reached does not fail the call: the local half of the report still comes back, with 'remoteError' explaining what went wrong."""
        )
        compareWithRemote: Boolean = true,
        @McpDescription(HacMcpConstants.Descriptions.CONNECTION_NAME_AUTH)
        connectionName: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = CxPropertyReportMcpRequest(connectionName, key, compareWithRemote)
        val result = PropertiesMcpService.getInstance().reportProperty(request)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_compare_properties")
    @McpDescription(
        """Compares a running SAP Commerce (Hybris) instance with the property chain this project declares, and reports what the two disagree about.
        |Returns {"connection", "remoteProperties", "projectProperties", "missingInProjectTotal", "missingOnRemoteTotal", "differingTotal", "truncated", "missingInProject": [{"key","value"}], "missingOnRemote": [{"key","value"}], "differing": [{"key","project","remote"}]}.
        |'missingInProject' is configured on the instance but declared nowhere the project's platform would read — typically something set by hand through the HAC, or by the environment. 'missingOnRemote' is declared by the project but absent from the instance. 'differing' is declared by both with different values.
        |The totals describe the whole comparison; the lists are capped by 'limit', so 'truncated' tells you when there is more.
        |A project declares thousands of properties, so pass 'keyFilter' when you are asking about one area (e.g. 'solr' or '^db\.') — it is applied to BOTH sides, so the comparison stays meaningful.
        |PRECONDITION: only call this tool against a connection whose authMode is AUTOMATIC (supportedByMcp = true in sap_commerce_list_hac_connections). If the user asks to use a connection that uses MANUAL (browser) authentication, do NOT call this tool — instead tell the user that connection is not supported by MCP tools yet and offer an AUTOMATIC one. Calling it against a MANUAL connection will fail."""
    )
    suspend fun compareProperties(
        @McpDescription(CxPropertiesMcpConstants.Descriptions.KEY_FILTER)
        keyFilter: String? = null,
        @McpDescription("Maximum entries per section of the report. Default 100. The totals are always exact regardless of this.")
        limit: Int = CxPropertiesMcpConstants.DEFAULT_LIMIT,
        @McpDescription(HacMcpConstants.Descriptions.CONNECTION_NAME_AUTH)
        connectionName: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = CxComparePropertiesMcpRequest(connectionName, keyFilter, positiveLimit(limit))
        val result = PropertiesMcpService.getInstance().compareProperties(request)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_validate_properties")
    @McpDescription(
        """Holds every property this project declares against the rules SAP Commerce Cloud documents, and reports the declarations which break one.
        |Returns {"sources", "properties", "errors", "warnings", "truncated", "violations": [{"key", "severity", "message", "rule", "file", "documentationUrl", "extension"?, "path"?}]}.
        |Two things are reported. An 'error' is a property SAP Commerce Cloud injects per environment (a database URL, a storage account, an initial password) being declared in an extension's project.properties: that file is in every build of every environment, the deployed value wins anyway, and a password written there is a secret in the repository — it belongs in local.properties. A 'warning' is a property the build process writes itself, or one only safe to edit in the Cloud Portal; SAP documents that changing those may cause builds to fail.
        |This is the same judgement the IDE inspections make in the editor, asked of the whole project at once. No HAC connection is needed — nothing here talks to an instance.
        |Call this before adding a property to project.properties, and use sap_commerce_list_property_rules to see the full catalogue without waiting for a violation."""
    )
    suspend fun validateProperties(
        @McpDescription("Optional severity filter: 'error' or 'warning'. Omit for both.")
        severity: String? = null,
        @McpDescription("Optional extension name, e.g. 'mystore'. Reports only the declarations made by that extension's project.properties.")
        extension: String? = null,
        @McpDescription("Maximum violations to list. Default 100. The 'errors' and 'warnings' counts are always exact regardless of this.")
        limit: Int = CxPropertiesMcpConstants.DEFAULT_LIMIT,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val request = CxValidatePropertiesMcpRequest(
            severity = severity?.trim()?.takeIf { it.isNotEmpty() }?.let { requested ->
                CxPropertyRuleSeverity.entries.find { it.title.equals(requested, ignoreCase = true) || it.name.equals(requested, ignoreCase = true) }
                    ?: error("Invalid severity '$severity'. Valid values: ${CxPropertyRuleSeverity.entries.joinToString { it.title }}")
            },
            extension = extension?.trim()?.takeIf { it.isNotEmpty() },
            limit = positiveLimit(limit),
        )
        val result = PropertiesMcpService.getInstance().validateProperties(request)
        return mapper.map(result)
    }

    @McpTool(name = "sap_commerce_list_property_rules")
    @McpDescription(
        """Lists the catalogue of properties SAP Commerce Cloud documents as not being the project's to declare, with the keys and key prefixes each rule covers.
        |Returns {"items": [{"rule", "title", "severity", "reportedIn", "documentationUrl", "keys", "keyPrefixes"}]}.
        |Use this to check whether a property is safe to declare BEFORE writing it into project.properties or local.properties; use sap_commerce_validate_properties to find the ones already written.
        |Static catalogue — no HAC connection and no project indexing needed."""
    )
    suspend fun listPropertyRules(
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val result = PropertiesMcpService.getInstance().listRules()
        return mapper.map(result)
    }

    private fun positiveLimit(limit: Int) = limit.takeIf { it > 0 }
        ?: error("'limit' must be greater than 0.")
}
