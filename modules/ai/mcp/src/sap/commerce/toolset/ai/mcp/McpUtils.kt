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

import com.intellij.mcpserver.project
import com.intellij.openapi.project.Project
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import kotlin.coroutines.CoroutineContext

/**
 * Retrieves the current [Project] from the MCP coroutine context.
 * The project is resolved by the MCP framework from the `projectPath` parameter
 * that is automatically injected into every MCP tool call.
 */
val CoroutineContext.mcpProject: Project
    get() = this.project

fun resolveHacConnection(project: Project, connectionName: String?): HacConnectionSettingsState {
    val connectionService = HacExecConnectionService.getInstance(project)

    if (connectionName.isNullOrBlank()) return connectionService.activeConnection

    return connectionService.connections.find {
        it.connectionName.equals(connectionName, ignoreCase = true)
            || it.name?.equals(connectionName, ignoreCase = true) == true
    }
        ?: error("HAC connection '$connectionName' not found. Available: ${connectionService.connections.joinToString { it.connectionName }}")
}

/**
 * Builds a name predicate from a user-supplied [filter]: a regex search when [filter] is a valid
 * regular expression ([Regex.containsMatchIn]), otherwise a case-insensitive substring ('contains') match.
 */
fun regexOrContainsMatcher(filter: String): (String) -> Boolean =
    runCatching { filter.toRegex() }.getOrNull()
        ?.let { regex -> regex::containsMatchIn }
        ?: { name -> name.contains(filter, ignoreCase = true) }

fun resolveSolrConnection(project: Project, connectionName: String?): SolrConnectionSettingsState {
    val connectionService = SolrExecConnectionService.getInstance(project)

    if (connectionName.isNullOrBlank()) return connectionService.activeConnection

    return connectionService.connections.find {
        it.connectionName.equals(connectionName, ignoreCase = true)
            || it.name?.equals(connectionName, ignoreCase = true) == true
    }
        ?: error("Solr connection '$connectionName' not found. Available: ${connectionService.connections.joinToString { it.connectionName }}")
}
