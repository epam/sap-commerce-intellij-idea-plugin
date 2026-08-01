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

package sap.commerce.toolset.businessProcess.mcp

import com.intellij.mcpserver.project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.businessProcess.mcp.context.BpSearchRequest
import sap.commerce.toolset.businessProcess.mcp.dto.BusinessProcessDto
import sap.commerce.toolset.businessProcess.mcp.dto.BusinessProcessesDto
import sap.commerce.toolset.businessProcess.meta.BPMetaCollector

@Service(Service.Level.PROJECT)
class BpMcpService(private val project: Project) {

    suspend fun searchBusinessProcesses(request: BpSearchRequest): BusinessProcessesDto {
        val result = BPMetaCollector.getInstance(project).collectDependencies()
        val extensions = request.extensions

        val items = result
            .filter { extensions == null || it.yContainer.lowercase() in extensions }
            .map {
                BusinessProcessDto(
                    container = it.container,
                    yContainer = it.yContainer,
                    name = it.name,
                    representationName = it.name,
                    absolutePath = it.virtualFile.path
                )
            }
        return BusinessProcessesDto(
            scope = request.scope,
            extensions = extensions?.sorted(),
            total = result.size,
            items = items,
        )
    }

    companion object {
        suspend fun getInstance(): BpMcpService = currentCoroutineContext().project.service()
    }
}
