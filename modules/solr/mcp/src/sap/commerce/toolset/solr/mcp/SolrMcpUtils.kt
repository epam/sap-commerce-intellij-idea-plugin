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

package sap.commerce.toolset.solr.mcp

import com.intellij.openapi.project.Project
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState

/**
 * Resolves the Solr connection targeted by a Solr MCP tool call: the connection whose name matches
 * [connectionName] (case-insensitively, against either the display or configured name), or the
 * active connection when [connectionName] is null/blank. Errors with the list of available
 * connection names when no match is found.
 */
fun resolveSolrConnection(project: Project, connectionName: String?): SolrConnectionSettingsState {
    val connectionService = SolrExecConnectionService.getInstance(project)

    if (connectionName.isNullOrBlank()) return connectionService.activeConnection

    return connectionService.connections.find {
        it.connectionName.equals(connectionName, ignoreCase = true)
            || it.name?.equals(connectionName, ignoreCase = true) == true
    }
        ?: error("Solr connection '$connectionName' not found. Available: ${connectionService.connections.joinToString { it.connectionName }}")
}
