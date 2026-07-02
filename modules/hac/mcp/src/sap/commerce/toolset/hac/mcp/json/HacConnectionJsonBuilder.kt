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

package sap.commerce.toolset.hac.mcp.json

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import sap.commerce.toolset.ai.mcp.json.McpJsonBuilder
import sap.commerce.toolset.hac.exec.settings.state.AuthMode
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

/**
 * Renders a HAC connection as `{name, url, active, authMode, supportedByMcp}`:
 * - `active` — whether this is the currently [activeConnection] (compared by uuid);
 * - `authMode` — AUTOMATIC (credentials persisted in the IDE) or MANUAL (interactive browser login);
 * - `supportedByMcp` — whether MCP tools can use it (only AUTOMATIC connections, as MANUAL requires
 *   an interactive browser login the model cannot perform).
 *
 * The active connection is passed in (rather than a global) so the builder stays a pure strategy.
 */
class HacConnectionJsonBuilder(private val activeConnection: HacConnectionSettingsState) : McpJsonBuilder<HacConnectionSettingsState> {

    override fun build(item: HacConnectionSettingsState): JsonObject = buildJsonObject {
        put("name", item.connectionName)
        put("url", item.generatedURL)
        put("active", item.uuid == activeConnection.uuid)
        put("authMode", item.authMode.name)
        put("supportedByMcp", item.authMode == AuthMode.AUTOMATIC)
    }
}
