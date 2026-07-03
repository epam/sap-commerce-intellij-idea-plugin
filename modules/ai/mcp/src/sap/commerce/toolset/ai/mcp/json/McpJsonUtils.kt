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

package sap.commerce.toolset.ai.mcp.json

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Shared conventions for rendering MCP tool responses as compact JSON: fields are emitted only when
 * they carry information, so consumers never have to reason about `false`/empty/blank noise (which
 * also keeps the response, and its token cost, small).
 *
 * These centralize the emit-only-when-meaningful patterns that the per-entity [McpJsonResponseElementBuilder]s
 * would otherwise repeat inline.
 */

/** Emits `"name": true` only when [value] is `true` (boolean flags are omitted when `false`). */
fun JsonObjectBuilder.putFlag(name: String, value: Boolean) {
    if (value) put(name, true)
}

/** Emits `"name": value` only when [value] is non-null and not blank. */
fun JsonObjectBuilder.putIfNotBlank(name: String, value: String?) {
    value?.takeIf { it.isNotBlank() }?.let { put(name, it) }
}

/** Emits `"name": [...]` as a JSON string array only when [values] is non-empty. */
fun JsonObjectBuilder.putStringArrayIfNotEmpty(name: String, values: Collection<String>) {
    if (values.isNotEmpty()) putJsonArray(name) { values.forEach { add(it) } }
}
