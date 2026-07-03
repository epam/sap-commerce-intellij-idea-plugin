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

package sap.commerce.toolset.ai.mcp

import com.intellij.openapi.project.Project
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

/**
 * Builds a name predicate from a user-supplied [filter]: a case-insensitive regex search when
 * [filter] is a valid regular expression ([Regex.containsMatchIn]), otherwise a case-insensitive
 * substring ('contains') match.
 *
 * Matching is case-insensitive in BOTH branches: because almost any plain search word (e.g.
 * 'product') is itself a valid regex, it takes the regex branch — so that branch must ignore case
 * too, otherwise a lowercase query would fail to match capitalized names like 'ProductCollection'.
 */
fun regexOrContainsMatcher(filter: String): (String) -> Boolean =
    runCatching { filter.toRegex(RegexOption.IGNORE_CASE) }.getOrNull()
        ?.let { regex -> regex::containsMatchIn }
        ?: { name -> name.contains(filter, ignoreCase = true) }

/**
 * Resolves the HAC connection targeted by a HAC-backed MCP tool call: the connection whose name
 * matches [connectionName] (case-insensitively, against either the display or configured name), or
 * the active connection when [connectionName] is null/blank. Errors with the list of available
 * connection names when no match is found.
 */
fun resolveHacConnection(project: Project, connectionName: String?): HacConnectionSettingsState {
    val connectionService = HacExecConnectionService.getInstance(project)

    if (connectionName.isNullOrBlank()) return connectionService.activeConnection

    return connectionService.connections.find {
        it.connectionName.equals(connectionName, ignoreCase = true)
            || it.name?.equals(connectionName, ignoreCase = true) == true
    }
        ?: error("HAC connection '$connectionName' not found. Available: ${connectionService.connections.joinToString { it.connectionName }}")
}
