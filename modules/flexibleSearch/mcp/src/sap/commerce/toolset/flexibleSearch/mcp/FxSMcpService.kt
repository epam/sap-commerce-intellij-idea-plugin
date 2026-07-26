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

package sap.commerce.toolset.flexibleSearch.mcp

import com.intellij.mcpserver.project
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import org.apache.http.HttpStatus
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLanguage
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.flexibleSearch.mcp.context.FxSExecMcpRequest
import sap.commerce.toolset.flexibleSearch.mcp.context.FxSTransformMcpRequest
import sap.commerce.toolset.flexibleSearch.mcp.dto.FxSExecResultDto
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchElementFactory
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.transform.Transformer

@Service(Service.Level.PROJECT)
class FxSMcpService(private val project: Project) {

    suspend fun execute(request: FxSExecMcpRequest): FxSExecResultDto {
        val connection = request.connection(project)
        val result = execute(request, connection)

        return if (result.statusCode != HttpStatus.SC_OK) {
            FxSExecResultDto(
                connectionName = connection.connectionName,
                success = false,
                error = result.errorMessage,
                errorDetail = result.errorDetailMessage,
            )
        } else {
            FxSExecResultDto(
                connectionName = connection.connectionName,
                success = true,
                output = result.output?.takeIf { it.isNotBlank() },
            )
        }
    }

    suspend fun transform(request: FxSTransformMcpRequest): FxSExecResultDto {
        val transformer = Transformer.EP.extensionList
            .find { it.isApplicable(FlexibleSearchLanguage) && it.id.equals(request.transformerId, true) }
            ?: error("No applicable '${request.transformerId}' transformer found for FlexibleSearch")

        val psiFile = readAction { FlexibleSearchElementFactory.createFile(project, request.query) }
        val execRequest = request.execRequest
        val connection = execRequest.connection(project)

        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_TYPE_SYSTEM_UNIQUE, request.includeTypeSystemUnique)
        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_DATA, request.includeData)
        psiFile.putUserData(FlexibleSearchExecConstants.Transform.CONNECTION, connection)
        psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_SETTINGS, execRequest.execSettings(connection))

        if (request.includeData) {
            val result = execute(execRequest, connection)
            psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_RESULTS, result)
        }

        val transformationResult = transformer.transform(psiFile)

        return FxSExecResultDto(
            connectionName = connection.connectionName,
            success = true,
            output = transformationResult.content,
            description = transformationResult.description,
        )
    }

    private suspend fun execute(
        request: FxSExecMcpRequest,
        connection: HacConnectionSettingsState
    ): FlexibleSearchExecResult {
        val execSettings = request.execSettings(connection)

        val execContext = FlexibleSearchExecContext(
            connection = connection,
            content = request.query,
            queryMode = request.queryMode,
            settings = execSettings
        )

        return FlexibleSearchExecClient.getInstance(project).execute(execContext)
    }

    private fun FxSExecMcpRequest.execSettings(connection: HacConnectionSettingsState) = FlexibleSearchExecContext.Settings(
        maxCount = maxCount,
        locale = locale,
        dataSource = dataSource,
        user = user,
        timeout = timeout ?: connection.timeout
    )

    companion object {
        suspend fun getInstance(): FxSMcpService = currentCoroutineContext().project.service()
    }
}
