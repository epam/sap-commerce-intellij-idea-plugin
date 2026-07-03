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

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import sap.commerce.toolset.ai.mcp.json.McpJsonResponseElementBuilder
import sap.commerce.toolset.ai.mcp.json.putFlag
import sap.commerce.toolset.ai.mcp.json.putIfNotBlank
import sap.commerce.toolset.ai.mcp.json.putStringArrayIfNotEmpty
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
class TSItemMcpJsonResponseElementBuilder(private val detail: ItemTypeDetail) : McpJsonResponseElementBuilder<TSGlobalMetaItem> {

    override fun build(item: TSGlobalMetaItem): JsonObject = buildJsonObject {
        put("name", item.name!!)
        putIfNotBlank("extends", item.extendedMetaItemName)
        putIfNotBlank("typeCode", item.deployment?.typeCode)
        putIfNotBlank("extension", item.extensionName)
        putFlag("abstract", item.isAbstract)
        putFlag("custom", item.isCustom)
        putFlag("deprecated", item.isDeprecated)

        if (detail == ItemTypeDetail.TYPES) return@buildJsonObject

        putJsonArray("attributes") {
            item.attributes.values
                .sortedBy { it.name }
                .forEach { add(attributeJson(it)) }
        }

    }

    private fun attributeJson(attribute: TSGlobalMetaItem.TSGlobalMetaItemAttribute): JsonObject = buildJsonObject {
        put("name", attribute.name)
        putIfNotBlank("type", attribute.type)

        if (detail != ItemTypeDetail.FULL) return@buildJsonObject

        // Which extension originally declares the attribute, and which ones redeclare it.
        val (redeclared, declared) = attribute.declarations
            .filter { it.extensionName.isNotBlank() }
            .partition { it.isRedeclare }

        putIfNotBlank("declaredIn", declared.firstOrNull()?.extensionName ?: attribute.extensionName)
        putStringArrayIfNotEmpty("redeclaredIn", redeclared.map { it.extensionName }.distinct().sorted())

        putFlag("localized", attribute.isLocalized)
        putFlag("dynamic", attribute.isDynamic)
        putFlag("deprecated", attribute.isDeprecated)
        putFlag("autoCreate", attribute.isAutoCreate)
        putFlag("generate", attribute.isGenerate)

        putIfNotBlank("defaultValue", attribute.defaultValue)
        putIfNotBlank("selectionOf", attribute.isSelectionOf)
        putIfNotBlank("flattenType", attribute.flattenType)
        putIfNotBlank("description", attribute.description)

        putStringArrayIfNotEmpty("modifiers", attribute.modifiers.activeModifiers())

        persistenceJson(attribute.persistence)
            .takeIf { it.isNotEmpty() }
            ?.let { put("persistence", it) }
    }

    private fun persistenceJson(persistence: TSMetaPersistence): JsonObject = buildJsonObject {
        putIfNotBlank("type", persistence.type?.name)
        putIfNotBlank("qualifier", persistence.qualifier)
        putIfNotBlank("attributeHandler", persistence.attributeHandler)
    }
}
