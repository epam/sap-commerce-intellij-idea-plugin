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
import sap.commerce.toolset.ai.mcp.json.McpJsonBuilder
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaAtomic

/**
 * Renders an atomic type as `{name, extends?, extension?, custom?, autoCreate?, generate?}`.
 * Atomic types are the primitive/scalar building blocks; `name`/`extends` are fully-qualified Java
 * class names, so `extends` is omitted when it is blank or equal to `name`.
 */
object AtomicTypeJsonBuilder : McpJsonBuilder<TSGlobalMetaAtomic> {

    override fun build(item: TSGlobalMetaAtomic): JsonObject = buildJsonObject {
        put("name", item.name)
        item.extends.takeIf { it.isNotBlank() && item.name != it }?.let { put("extends", it) }
        putExtensionAndFlags(item, item.isAutoCreate, item.isGenerate)
    }
}
