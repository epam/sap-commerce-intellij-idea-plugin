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

import kotlinx.serialization.json.JsonObject

/**
 * Strategy for rendering a single domain object [T] into a [JsonObject] for an MCP tool response.
 *
 * Each implementation owns the JSON shape of exactly ONE kind of entity (an item type, an atomic
 * type, a HAC connection, …). Keeping the shape in a dedicated, independently testable strategy lets
 * a toolset render a homogeneous collection through the same [buildListResponse] plumbing without
 * baking the per-entity shape into the toolset itself — and lets a different toolset reuse that same
 * plumbing simply by supplying its own builder.
 */
fun interface McpJsonBuilder<in T> {

    /** Renders [item] as a standalone JSON object. */
    fun build(item: T): JsonObject
}
