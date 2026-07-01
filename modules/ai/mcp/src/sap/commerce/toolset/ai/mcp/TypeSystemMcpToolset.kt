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

package sap.commerce.toolset.ai.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.*
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import sap.commerce.toolset.typeSystem.meta.model.*

/**
 * Exposes the SAP Commerce Type System — as shown in the "Type System" tool window — as MCP tools.
 *
 * The type system is the project's LOCAL model: it is parsed from the `*-items.xml` definitions in
 * the project (via [TSMetaModelStateService]), not fetched from a remote server, so these tools do
 * not require (or use) a HAC connection.
 */
class TypeSystemMcpToolset : McpToolset {

    /** How much information [listItemTypes] returns per item type. */
    private enum class ItemTypeDetail { TYPES, ATTRIBUTES, FULL }

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
        val project = currentCoroutineContext().mcpProject

        val detailLevel = ItemTypeDetail.entries.find { it.name.equals(detail.trim(), ignoreCase = true) }
            ?: error("Invalid detail '$detail'. Valid values: ${ItemTypeDetail.entries.joinToString { it.name }}")

        val normalizedFilter = filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val extensionFilter = parseExtensionFilter(extensions)

        ensureTypeSystemReady(project)

        val payload = readAction {
            val allItems = TSMetaModelAccess.getInstance(project).getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
                .filter { it.name != null }
            val matched = allItems
                .filter { item -> matcher?.invoke(item.name!!) ?: true }
                .filter { item -> extensionFilter == null || item.extensionName.lowercase() in extensionFilter }
                .sortedBy { it.name }

            buildJsonObject {
                put("detail", detailLevel.name)
                normalizedFilter?.let { put("filter", it) }
                extensionFilter?.let { exts -> putJsonArray("extensions") { exts.sorted().forEach { add(it) } } }
                put("matched", matched.size)
                put("total", allItems.size)
                putJsonArray("itemTypes") {
                    matched.forEach { add(itemTypeJson(it, detailLevel)) }
                }
            }
        }

        return json.encodeToString(JsonObject.serializer(), payload)
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
    ): String {
        val project = currentCoroutineContext().mcpProject

        val normalizedFilter = filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }
        val extensionFilter = parseExtensionFilter(extensions)

        ensureTypeSystemReady(project)

        val payload = readAction {
            val allAtomics = TSMetaModelAccess.getInstance(project).getAll<TSGlobalMetaAtomic>(TSMetaType.META_ATOMIC)
            val matched = allAtomics
                .filter { atomic -> matcher?.invoke(atomic.name) ?: true }
                .filter { atomic -> extensionFilter == null || atomic.extensionName.lowercase() in extensionFilter }
                .sortedBy { it.name }

            buildJsonObject {
                normalizedFilter?.let { put("filter", it) }
                extensionFilter?.let { exts -> putJsonArray("extensions") { exts.sorted().forEach { add(it) } } }
                put("matched", matched.size)
                put("total", allAtomics.size)
                putJsonArray("atomicTypes") {
                    matched.forEach { add(atomicTypeJson(it)) }
                }
            }
        }

        return json.encodeToString(JsonObject.serializer(), payload)
    }

    private fun atomicTypeJson(atomic: TSGlobalMetaAtomic): JsonObject = buildJsonObject {
        put("name", atomic.name)
        atomic.extends.takeIf { it.isNotBlank() }?.let { put("extends", it) }
        atomic.extensionName.takeIf { it.isNotBlank() }?.let { put("extension", it) }
        if (atomic.isCustom) put("custom", true)
        if (atomic.isAutoCreate) put("autoCreate", true)
        if (atomic.isGenerate) put("generate", true)
    }

    private fun parseExtensionFilter(extensions: String?): Set<String>? = extensions
        ?.split(',')
        ?.map { it.trim().lowercase() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?.takeIf { it.isNotEmpty() }

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
    ): String {
        val project = currentCoroutineContext().mcpProject

        val normalizedFilter = filter?.trim()?.takeIf { it.isNotEmpty() }
        val matcher = normalizedFilter?.let { regexOrContainsMatcher(it) }

        val extensionFilter = extensions
            ?.split(',')
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }

        ensureTypeSystemReady(project)

        val payload = readAction {
            val allCollections = TSMetaModelAccess.getInstance(project).getAll<TSGlobalMetaCollection>(TSMetaType.META_COLLECTION)
                .filter { it.name != null }
            val matched = allCollections
                .filter { collection -> matcher?.invoke(collection.name!!) ?: true }
                .filter { collection -> extensionFilter == null || collection.extensionName.lowercase() in extensionFilter }
                .sortedBy { it.name }

            buildJsonObject {
                normalizedFilter?.let { put("filter", it) }
                extensionFilter?.let { exts -> putJsonArray("extensions") { exts.sorted().forEach { add(it) } } }
                put("matched", matched.size)
                put("total", allCollections.size)
                putJsonArray("collectionTypes") {
                    matched.forEach { add(collectionTypeJson(it)) }
                }
            }
        }

        return json.encodeToString(JsonObject.serializer(), payload)
    }

    private fun collectionTypeJson(collection: TSGlobalMetaCollection): JsonObject = buildJsonObject {
        put("name", collection.name!!)
        put("kind", collection.type.value)
        collection.elementType.takeIf { it.isNotBlank() }?.let { put("elementType", it) }
        collection.extensionName.takeIf { it.isNotBlank() }?.let { put("extension", it) }
        if (collection.isCustom) put("custom", true)
        if (collection.isAutoCreate) put("autoCreate", true)
        if (collection.isGenerate) put("generate", true)
    }

    private fun itemTypeJson(item: TSGlobalMetaItem, detail: ItemTypeDetail): JsonObject = buildJsonObject {
        put("name", item.name!!)
        item.extendedMetaItemName?.let { put("extends", it) }
        item.deployment?.typeCode?.let { put("typeCode", it) }
        item.extensionName.takeIf { it.isNotBlank() }?.let { put("extension", it) }
        if (item.isAbstract) put("abstract", true)
        if (item.isCustom) put("custom", true)
        if (item.isDeprecated) put("deprecated", true)

        if (detail != ItemTypeDetail.TYPES) {
            putJsonArray("attributes") {
                item.attributes.values
                    .sortedBy { it.name }
                    .forEach { add(attributeJson(it, detail)) }
            }
        }
    }

    private fun attributeJson(attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute, detail: ItemTypeDetail): JsonObject = buildJsonObject {
        put("name", attribute.name)
        attribute.type?.let { put("type", it) }

        if (detail != ItemTypeDetail.FULL) return@buildJsonObject

        // Which extension originally declares the attribute, and which ones redeclare it.
        val (redeclared, declared) = attribute.declarations
            .filter { it.extensionName.isNotBlank() }
            .partition { it.isRedeclare }

        val declaredIn = declared.firstOrNull()?.extensionName
            ?: attribute.extensionName.takeIf { it.isNotBlank() }
        declaredIn?.let { put("declaredIn", it) }

        redeclared.map { it.extensionName }
            .distinct()
            .sorted()
            .takeIf { it.isNotEmpty() }
            ?.let { exts -> putJsonArray("redeclaredIn") { exts.forEach { add(it) } } }

        if (attribute.isLocalized) put("localized", true)
        if (attribute.isDynamic) put("dynamic", true)
        if (attribute.isDeprecated) put("deprecated", true)
        if (attribute.isAutoCreate) put("autoCreate", true)
        if (attribute.isGenerate) put("generate", true)

        attribute.defaultValue?.takeIf { it.isNotBlank() }?.let { put("defaultValue", it) }
        attribute.isSelectionOf?.takeIf { it.isNotBlank() }?.let { put("selectionOf", it) }
        attribute.flattenType?.takeIf { it.isNotBlank() }?.let { put("flattenType", it) }
        attribute.description?.takeIf { it.isNotBlank() }?.let { put("description", it) }

        attribute.modifiers.activeModifiers()
            .takeIf { it.isNotEmpty() }
            ?.let { modifiers -> putJsonArray("modifiers") { modifiers.forEach { add(it) } } }

        persistenceJson(attribute.persistence)
            .takeIf { it.isNotEmpty() }
            ?.let { put("persistence", it) }
    }

    private fun persistenceJson(persistence: TSMetaPersistence): JsonObject = buildJsonObject {
        persistence.type?.let { put("type", it.name) }
        persistence.qualifier?.takeIf { it.isNotBlank() }?.let { put("qualifier", it) }
        persistence.attributeHandler?.takeIf { it.isNotBlank() }?.let { put("attributeHandler", it) }
    }

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
