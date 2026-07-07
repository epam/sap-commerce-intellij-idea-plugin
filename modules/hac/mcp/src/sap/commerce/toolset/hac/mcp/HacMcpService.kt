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

package sap.commerce.toolset.hac.mcp

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.AuthMode
import sap.commerce.toolset.hac.mcp.dto.HacConnectionDto
import sap.commerce.toolset.hac.mcp.dto.HacConnectionListResponse

@Service(Service.Level.PROJECT)
class HacMcpService(private val project: Project) {

    fun listConnections(): HacConnectionListResponse {
        val connectionService = HacExecConnectionService.getInstance(project)
        val activeUuid = connectionService.activeConnection.uuid
        val items = connectionService.connections.map { conn ->
            HacConnectionDto(
                name = conn.connectionName,
                url = conn.generatedURL,
                active = conn.uuid == activeUuid,
                authMode = conn.authMode.name,
                supportedByMcp = conn.authMode == AuthMode.AUTOMATIC,
            )
        }
        return HacConnectionListResponse(matched = items.size, total = items.size, items = items)
    }

    companion object {
        fun getInstance(project: Project): HacMcpService = project.service()
    }
}
