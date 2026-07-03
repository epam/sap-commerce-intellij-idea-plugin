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

import kotlinx.serialization.json.*

/**
 * Builds the standard MCP "list" response object shared by the `list*` tools:
 * `{<additionalFields>, "filter"?, "matched", "total", <arrayKey>: [...]}`.
 *
 * [items] is the full, unfiltered candidate set. An item is dropped when [nameOf] returns `null`;
 * the survivors form the reported `total`. Each remaining item is kept when it passes the optional
 * name [matcher] (see [sap.commerce.toolset.ai.mcp.regexOrContainsMatcher]) AND every predicate in
 * [filters]; the kept items form the reported `matched`, are sorted by [nameOf] and rendered through
 * [itemBuilder].
 *
 * [filterText] — when non-null — is echoed back as `"filter"` so the caller can see the effective
 * name filter. [additionalFields] contributes tool-specific leading entries (e.g. a `"detail"` level
 * or an echoed `"extensions"` list).
 */
fun <T> buildListResponse(
    items: Collection<T>,
    arrayKey: String,
    nameOf: (T) -> String?,
    itemBuilder: McpJsonBuilder<T>,
    matcher: ((String) -> Boolean)? = null,
    filters: List<(T) -> Boolean> = emptyList(),
    filterText: String? = null,
    additionalFields: JsonObjectBuilder.() -> Unit = {},
): JsonObject {
    val candidates = items.filter { nameOf(it) != null }
    val matched = candidates
        .filter { item -> matcher?.invoke(nameOf(item)!!) ?: true }
        .filter { item -> filters.all { it(item) } }
        .sortedBy { nameOf(it) }

    return buildJsonObject {
        additionalFields()
        filterText?.let { put("filter", it) }
        put("matched", matched.size)
        put("total", candidates.size)
        putJsonArray(arrayKey) {
            matched.forEach { add(itemBuilder.build(it)) }
        }
    }
}
