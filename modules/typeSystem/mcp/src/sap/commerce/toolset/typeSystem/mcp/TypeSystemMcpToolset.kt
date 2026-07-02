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
import com.intellij.mcpserver.project
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.*
import sap.commerce.toolset.ai.mcp.json.McpJsonBuilder
import sap.commerce.toolset.ai.mcp.json.buildListResponse
import sap.commerce.toolset.ai.mcp.regexOrContainsMatcher
import sap.commerce.toolset.typeSystem.mcp.json.AtomicTypeJsonBuilder
import sap.commerce.toolset.typeSystem.mcp.json.CollectionTypeJsonBuilder
import sap.commerce.toolset.typeSystem.mcp.json.ItemTypeJsonBuilder
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaAtomic
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaCollection
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType

/**
 * Exposes the SAP Commerce Type System — as shown in the "Type System" tool window — as MCP tools.
 *
 * The type system is the project's LOCAL model: it is parsed from the `*-items.xml` definitions in
 * the project (via [TSMetaModelStateService]), not fetched from a remote server, so these tools do
 * not require (or use) a HAC connection.
 */
class TypeSystemMcpToolset : McpToolset {

    private val json = Json { prettyPrint = false }

    @McpTool(name = "sap_commerce_list_item_types")
    @McpDescription(
        """Lists the Item types defined in the current project's SAP Commerce (Hybris) type system, as shown in the "Type System" tool window.
        |This is the project's LOCAL model, parsed from the `*-items.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"detail", "filter", "matched", "total", "itemTypes": [...]}. Boolean flags (custom, abstract, deprecated) are present only when true and omitted otherwise.
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

        return listTypes(
            filter = filter,
            extensions = extensions,
            arrayKey = "itemTypes",
            fetch = { getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM) },
            nameOf = { it.name },
            extensionOf = { it.extensionName },
            envelope = { put("detail", detailLevel.name) },
            itemBuilder = ItemTypeJsonBuilder(detailLevel),
        )
    }

    @McpTool(name = "sap_commerce_list_atomic_types")
    @McpDescription(
        """Lists the Atomic types defined in the current project's SAP Commerce (Hybris) type system, as shown in the "Type System" tool window.
        |Atomic types are the primitive/scalar building blocks (e.g. 'java.lang.String', 'java.lang.Boolean', 'java.util.Date'); their name and 'extends' are fully-qualified Java class names.
        |This is the project's LOCAL model, parsed from the `*-items.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"filter", "extensions", "matched", "total", "atomicTypes": [{"name", "extends", "extension", "custom", "autoCreate", "generate"}]}. Boolean flags are present only when true and omitted otherwise.
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
    ): String = listTypes(
        filter = filter,
        extensions = extensions,
        arrayKey = "atomicTypes",
        fetch = { getAll<TSGlobalMetaAtomic>(TSMetaType.META_ATOMIC) },
        nameOf = { it.name },
        extensionOf = { it.extensionName },
        itemBuilder = AtomicTypeJsonBuilder,
    )

    @McpTool(name = "sap_commerce_list_collection_types")
    @McpDescription(
        """Lists the Collection types defined in the current project's SAP Commerce (Hybris) type system, as shown in the "Type System" tool window.
        |A collection type wraps an element type as a 'collection', 'list' or 'set' (its 'kind').
        |This is the project's LOCAL model, parsed from the `*-items.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"filter", "extensions", "matched", "total", "collectionTypes": [{"name", "kind", "elementType", "extension", "custom", "autoCreate", "generate"}]}. Boolean flags are present only when true and omitted otherwise.
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
    ): String = listTypes(
        filter = filter,
        extensions = extensions,
        arrayKey = "collectionTypes",
        fetch = { getAll<TSGlobalMetaCollection>(TSMetaType.META_COLLECTION) },
        nameOf = { it.name },
        extensionOf = { it.extensionName },
        itemBuilder = CollectionTypeJsonBuilder,
    )

    /**
     * Shared implementation behind the `list*` tools: normalizes the name [filter] and [extensions]
     * filter, ensures the type-system model is ready, then (inside a read action) fetches the types
     * via [fetch] and hands them to [buildListResponse], which applies both filters, sorts by name
     * and renders the standard `{detail?, filter?, extensions?, matched, total, <arrayKey>}` envelope
     * using the supplied per-type [itemBuilder] strategy.
     *
     * [fetch] runs against [TSMetaModelAccess]; [nameOf] may return null (such entries are dropped).
     */
    private suspend fun <T> listTypes(
        filter: String?,
        extensions: String?,
        arrayKey: String,
        fetch: TSMetaModelAccess.() -> Collection<T>,
        nameOf: (T) -> String?,
        extensionOf: (T) -> String,
        itemBuilder: McpJsonBuilder<T>,
        envelope: JsonObjectBuilder.() -> Unit = {},
    ): String {
        val project = currentCoroutineContext().project

        val normalizedFilter = filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val extensionFilter = parseExtensionFilter(extensions)

        ensureTypeSystemReady(project)

        val payload = readAction {
            buildListResponse(
                items = TSMetaModelAccess.getInstance(project).fetch(),
                arrayKey = arrayKey,
                nameOf = nameOf,
                itemBuilder = itemBuilder,
                matcher = matcher,
                filters = listOfNotNull(
                    extensionFilter?.let { exts -> { item: T -> extensionOf(item).lowercase() in exts } }
                ),
                filterText = normalizedFilter,
                envelope = {
                    envelope()
                    extensionFilter?.let { exts -> putJsonArray("extensions") { exts.sorted().forEach { add(it) } } }
                },
            )
        }

        return json.encodeToString(JsonObject.serializer(), payload)
    }

    private fun parseExtensionFilter(extensions: String?): Set<String>? = extensions
        ?.split(',')
        ?.map { it.trim().lowercase() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?.takeIf { it.isNotEmpty() }

    /**
     * Turns the common "not ready" states of the type-system model (indexing / not-yet-built) into
     * actionable tool errors up front, before the model is queried via [TSMetaModelAccess].
     *
     * Retrieval still resolves through [TSMetaModelStateService.state], which may throw
     * [com.intellij.openapi.progress.ProcessCanceledException] if a rebuild is in flight; that must
     * NOT be swallowed, so it is intentionally left to propagate.
     */
    private fun ensureTypeSystemReady(project: Project) {
        if (DumbService.isDumb(project)) error("Project indexing is in progress; retry once indexing completes.")

        val service = TSMetaModelStateService.getInstance(project)
        if (!service.initialized()) {
            service.init()
            error("The type system model has not been built yet — a build has been triggered. Retry in a few seconds.")
        }
    }
}
