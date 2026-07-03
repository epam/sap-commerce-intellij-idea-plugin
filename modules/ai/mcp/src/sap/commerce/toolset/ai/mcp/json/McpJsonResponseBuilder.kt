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
import sap.commerce.toolset.ai.mcp.McpResponseBuilder

private val json = Json { prettyPrint = false }

abstract class McpJsonResponseBuilder<T> : McpResponseBuilder<T, String, McpJsonResponseBuilderContext<T>> {

    protected abstract val itemBuilder: McpJsonResponseElementBuilder<T>

    override fun build(content: McpJsonResponseBuilderContext<T>, filterText: String?): String {
        val payload = buildJsonObject {
            content.additionalFieldsProvider.invoke(this)

            putIfNotBlank("filter", filterText)

            put("matched", content.items.size)
            put("total", content.total)

            putJsonArray("items") {
                content.items.forEach { add(itemBuilder.build(it)) }
            }
        }
        return json.encodeToString(JsonObject.serializer(), payload)
    }
}
