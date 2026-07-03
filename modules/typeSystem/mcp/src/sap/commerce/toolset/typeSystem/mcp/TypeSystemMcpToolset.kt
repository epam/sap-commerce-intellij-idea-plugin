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

package sap.commerce.toolset.typeSystem.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool

/**
 * Exposes the SAP Commerce Type System — as shown in the "Type System" tool window — as MCP tools.
 *
 * The type system is the project's LOCAL model: it is parsed from the `*-items.xml` definitions in
 * the project, not fetched from a remote server, so these tools do not require (or use) a HAC
 * connection. Each tool delegates to the matching [TSTypeLister], which holds the shared listing
 * pipeline.
 */
class TypeSystemMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_list_item_types")
    @McpDescription(
        """Lists the Item types defined in the current project's SAP Commerce (Hybris) type system, as shown in the "Type System" tool window.
        |This is the project's LOCAL model, parsed from the `*-items.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"detail", "filter", "matched", "total", "items": [...]}. Boolean flags (custom, abstract, deprecated) are present only when true and omitted otherwise.
        |A project can define thousands of item types, so narrow the result with 'filter' (by name) and/or 'extensions' (by owning extension), and use 'detail' to control how much per-type information is returned, keeping the response (and token usage) small."""
    )
    suspend fun listItemTypes(
        @McpDescription(
            """Optional item-type-name filter used to shrink the response and save tokens.
            |If the value is a valid regular expression it is matched against each item type name with a regex search (e.g. '^Product$' for an exact match, 'Product' for partial, or '(?i)catalog' for case-insensitivity);
            |otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all item types."""
        )
        filter: String? = null,
        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to item types owned by those extensions (e.g. 'core,basecommerce' or 'myprojectcore').
            |Matched case-insensitively and exactly against each item type's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include item types from all extensions."""
        )
        extensions: String? = null,
        @McpDescription(
            """Controls how much information is returned per item type, to balance completeness against token usage:
            |- TYPES: item type identity only (name, extends, typeCode, extension, and the custom/abstract/deprecated flags). No attributes.
            |- ATTRIBUTES: the above plus each type's declared attributes as {name, type}.
            |- FULL: the above plus all available attribute meta-information: the extension it is 'declaredIn' and any extensions it is 'redeclaredIn', the localized/dynamic/deprecated/autoCreate/generate flags, defaultValue, selectionOf, flattenType, description, the active 'modifiers' (which include 'optional' — a mandatory attribute is simply one without it) and 'persistence' details. Only non-empty values are included.
            |Default: TYPES. Prefer the smallest level that answers the question. Attributes are the type's DECLARED attributes, not inherited ones."""
        )
        detail: String = "TYPES",
    ): String {
        val detailLevel = ItemTypeDetail.entries.find { it.name.equals(detail.trim(), ignoreCase = true) }
            ?: error("Invalid detail '$detail'. Valid values: ${ItemTypeDetail.entries.joinToString { it.name }}")

        return ItemTypeLister(detailLevel).list(filter, extensions)
    }

    @McpTool(name = "sap_commerce_list_atomic_types")
    @McpDescription(
        """Lists the Atomic types defined in the current project's SAP Commerce (Hybris) type system, as shown in the "Type System" tool window.
        |Atomic types are the primitive/scalar building blocks (e.g. 'java.lang.String', 'java.lang.Boolean', 'java.util.Date'); their name and 'extends' are fully-qualified Java class names.
        |This is the project's LOCAL model, parsed from the `*-items.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"filter", "extensions", "matched", "total", "items": [{"name", "extends", "extension", "custom", "autoCreate", "generate"}]}. Boolean flags are present only when true and omitted otherwise.
        |Use 'filter' (by name) and/or 'extensions' (by owning extension) to narrow the result and keep the response (and token usage) small."""
    )
    suspend fun listAtomicTypes(
        @McpDescription(
            """Optional atomic-type-name filter used to shrink the response and save tokens.
            |If the value is a valid regular expression it is matched against each atomic type name with a regex search (e.g. '^java\.lang\.' or '(?i)date'); otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all atomic types."""
        )
        filter: String? = null,
        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to atomic types owned by those extensions (e.g. 'core,basecommerce').
            |Matched case-insensitively and exactly against each atomic type's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include atomic types from all extensions."""
        )
        extensions: String? = null,
    ): String = AtomicTypeLister.list(filter, extensions)

    @McpTool(name = "sap_commerce_list_collection_types")
    @McpDescription(
        """Lists the Collection types defined in the current project's SAP Commerce (Hybris) type system, as shown in the "Type System" tool window.
        |A collection type wraps an element type as a 'collection', 'list' or 'set' (its 'kind').
        |This is the project's LOCAL model, parsed from the `*-items.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"filter", "extensions", "matched", "total", "items": [{"name", "kind", "elementType", "extension", "custom", "autoCreate", "generate"}]}. Boolean flags are present only when true and omitted otherwise.
        |Use 'filter' (by name) and/or 'extensions' (by owning extension) to narrow the result and keep the response (and token usage) small."""
    )
    suspend fun listCollectionTypes(
        @McpDescription(
            """Optional collection-type-name filter used to shrink the response and save tokens.
            |If the value is a valid regular expression it is matched against each collection type name with a regex search (e.g. '(?i)product'); otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all collection types."""
        )
        filter: String? = null,
        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to collection types owned by those extensions (e.g. 'core,basecommerce').
            |Matched case-insensitively and exactly against each collection type's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include collection types from all extensions."""
        )
        extensions: String? = null,
    ): String = CollectionTypeLister.list(filter, extensions)
}
