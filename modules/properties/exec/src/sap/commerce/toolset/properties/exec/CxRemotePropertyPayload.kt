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

package sap.commerce.toolset.properties.exec

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.serialization.json.*
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation

/**
 * Reads what the state script returns into a [CxRemotePropertyStatePage].
 *
 * The script has been paged since it was written, but an instance running an older copy of it still answers with a
 * bare array, so both shapes are accepted and a missing page is treated as one page holding everything.
 */
object CxRemotePropertyPayload {

    fun parse(payload: String): CxRemotePropertyStatePage? = try {
        when (val json = Json.parseToJsonElement(payload)) {
            is JsonObject -> parsePaged(json)
            is JsonArray -> parseLegacy(json)
            else -> null
        }
    } catch (e: Exception) {
        thisLogger().warn("Unable to parse properties payload", e)
        null
    }

    private fun parsePaged(json: JsonObject): CxRemotePropertyStatePage? {
        val page = json["page"]?.jsonPrimitive?.intOrNull ?: return null
        val pageSize = json["pageSize"]?.jsonPrimitive?.intOrNull ?: return null
        val totalItems = json["totalItems"]?.jsonPrimitive?.intOrNull ?: return null
        val items = json["items"]?.jsonArray ?: return null

        return CxRemotePropertyStatePage(
            lastLoadedPage = page,
            pageSize = pageSize,
            totalItems = totalItems,
            keyFilter = json["keyFilter"]?.jsonPrimitive?.content.orEmpty(),
            valueFilter = json["valueFilter"]?.jsonPrimitive?.content.orEmpty(),
            properties = parseItems(items),
        )
    }

    private fun parseLegacy(items: JsonArray) = CxRemotePropertyStatePage(
        lastLoadedPage = 1,
        pageSize = items.size.coerceAtLeast(1),
        totalItems = items.size,
        keyFilter = "",
        valueFilter = "",
        properties = parseItems(items),
    )

    private fun parseItems(items: Iterable<JsonElement>): List<CxPropertyPresentation> = items
        .mapNotNull {
            val obj = it.jsonObject
            val key = obj["key"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val value = obj["value"]?.jsonPrimitive?.content
            CxPropertyPresentation.of(key, value)
        }
        .sortedBy { it.key }
}
