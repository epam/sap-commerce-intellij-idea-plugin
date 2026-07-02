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

package sap.commerce.toolset.typeSystem.mcp.json

import kotlinx.serialization.json.*
import sap.commerce.toolset.ai.mcp.json.McpJsonBuilder
import sap.commerce.toolset.typeSystem.mcp.ItemTypeDetail
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaPersistence

/**
 * Renders an item type at the requested [detail] level:
 * - [ItemTypeDetail.TYPES]: identity only — `name, extends?, typeCode?, extension?` plus the
 *   `abstract`/`custom`/`deprecated` flags (each only when true). No attributes.
 * - [ItemTypeDetail.ATTRIBUTES]: the above plus the DECLARED attributes as `{name, type}`.
 * - [ItemTypeDetail.FULL]: the above plus all available per-attribute meta-information.
 *
 * A distinct instance is used per detail level (the level is the strategy's only state).
 */
class ItemTypeJsonBuilder(private val detail: ItemTypeDetail) : McpJsonBuilder<TSGlobalMetaItem> {

    override fun build(item: TSGlobalMetaItem): JsonObject = buildJsonObject {
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
                    .forEach { add(attributeJson(it)) }
            }
        }
    }

    private fun attributeJson(attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute): JsonObject = buildJsonObject {
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
}
