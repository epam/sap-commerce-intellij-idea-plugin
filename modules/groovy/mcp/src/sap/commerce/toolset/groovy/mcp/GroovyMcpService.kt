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
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.util.asSafely
import kotlinx.coroutines.currentCoroutineContext
import org.apache.http.HttpStatus
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.groovy.mcp.context.GroovyExecMcpRequest
import sap.commerce.toolset.groovy.mcp.context.GroovyTransformMcpRequest
import sap.commerce.toolset.groovy.mcp.dto.GroovyExecResultDto
import sap.commerce.toolset.groovy.mcp.dto.GroovyTransformResultDto
import sap.commerce.toolset.transform.Transformer

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

    suspend fun transform(request: GroovyTransformMcpRequest): GroovyTransformResultDto {
        val transformer = Transformer.EP.extensionList
            .find { it.isApplicable(GroovyLanguage) && it.id.equals(request.transformerId, ignoreCase = true) }
            ?: error("No applicable '${request.transformerId}' transformer found for Groovy")

        val psiFile = readAction {
            PsiFileFactory.getInstance(project)
                .createFileFromText("transform.groovy", GroovyLanguage, request.script)
                .asSafely<GroovyFile>()
                ?: error("cannot create GroovyFile PSI from script")
        }

        psiFile.putUserData(GroovyConstants.Transform.SCRIPT_NAME, request.scriptName)

        val result = transformer.transform(psiFile)

        return GroovyTransformResultDto(
            success = true,
            content = result.content,
            description = result.description,
        )
    }

    companion object {
        suspend fun getInstance(): GroovyMcpService = currentCoroutineContext().project.service()
    }
}
