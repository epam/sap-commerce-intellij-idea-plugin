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

package sap.commerce.toolset.logging.mcp.json

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import sap.commerce.toolset.ai.mcp.json.McpJsonBuilder
import sap.commerce.toolset.ai.mcp.json.putIfNotBlank
import sap.commerce.toolset.logging.CxLogConstants
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation

/**
 * Renders a logger as `{name, level, parent?}`. `parent` is omitted for root-level loggers (a blank
 * parent, or the root logger itself).
 */
object LoggerJsonBuilder : McpJsonBuilder<CxLoggerPresentation> {

    override fun build(item: CxLoggerPresentation): JsonObject = buildJsonObject {
        put("name", item.name)
        put("level", item.level.name)
        putIfNotBlank("parent", item.parentName?.takeIf { it != CxLogConstants.ROOT_LOGGER_NAME })
    }
}
