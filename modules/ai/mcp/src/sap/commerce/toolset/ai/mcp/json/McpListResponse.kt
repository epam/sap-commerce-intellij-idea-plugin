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
 * Renders the standard MCP "list" response object shared by the `list*` tools:
 * `{<additionalFields>, "filter"?, "matched", "total", "items": [...]}`.
 *
 * This is purely a formatter: the caller does the fetching and filtering and passes the already
 * matched [items] to render (their count is reported as `matched`) together with the [total] number
 * of candidates considered before filtering.
 *
 * [filterText] — when non-blank — is echoed back as `"filter"` so the caller can see the effective
 * name filter. [additionalFields] contributes tool-specific leading entries (e.g. a `"detail"` level
 * or an echoed `"extensions"` list).
 */
fun <T> buildListResponse(
    items: Collection<T>,
    total: Int,
    itemBuilder: McpJsonBuilder<T>,
    filterText: String? = null,
    additionalFields: JsonObjectBuilder.() -> Unit = {},
): JsonObject = buildJsonObject {
    additionalFields()
    putIfNotBlank("filter", filterText)
    put("matched", items.size)
    put("total", total)
    putJsonArray("items") {
        items.forEach { add(itemBuilder.build(it)) }
    }
}
