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

package sap.commerce.toolset.groovy.mcp

import com.intellij.mcpserver.project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import org.apache.http.HttpStatus
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.mcp.context.GroovyExecMcpRequest
import sap.commerce.toolset.groovy.mcp.dto.GroovyExecResultDto

@Service(Service.Level.PROJECT)
class GroovyMcpService(private val project: Project) {

    suspend fun execute(request: GroovyExecMcpRequest): GroovyExecResultDto {
        val connection = request.connection(project)
        val execContext = GroovyExecContext(
            connection = connection,
            content = request.script,
            timeout = connection.timeout,
            transactionMode = request.transactionMode,
        )

        val result = GroovyExecClient.getInstance(project).execute(execContext)

        return if (result.statusCode != HttpStatus.SC_OK) {
            GroovyExecResultDto(
                connectionName = connection.connectionName,
                success = false,
                error = result.errorMessage,
                errorDetail = result.errorDetailMessage,
            )
        } else {
            GroovyExecResultDto(
                connectionName = connection.connectionName,
                success = true,
                output = result.output?.takeIf { it.isNotBlank() },
                result = result.result?.takeIf { it.isNotBlank() },
            )
        }
    }

    companion object {
        suspend fun getInstance(): GroovyMcpService = currentCoroutineContext().project.service()
    }
}
