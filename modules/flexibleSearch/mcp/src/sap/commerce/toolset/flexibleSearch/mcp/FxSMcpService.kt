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
                rowCount = result.rowCount,
                maxCountReached = maxCountReached(result, request.maxCount),
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

        val execResult = if (request.includeData) {
            execute(execRequest, connection)
                .also { psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_RESULTS, it) }
        } else null

        val transformationResult = transformer.transform(project, psiFile)

        return FxSExecResultDto(
            connectionName = connection.connectionName,
            success = true,
            output = transformationResult.content,
            rowCount = execResult?.rowCount,
            maxCountReached = execResult?.let { maxCountReached(it, execRequest.maxCount) },
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

    /**
     * Whether the [result] may have been capped by the [maxCount] limit of the request.
     *
     * The server does not report whether more rows were available, therefore an exactly-[maxCount] sized result
     * is reported as capped even when it happens to be complete. A false positive is intentional: it tells the
     * caller to re-run the query with a higher `maxCount`, whereas a silent cap is indistinguishable from a
     * complete result set.
     */
    private fun maxCountReached(result: FlexibleSearchExecResult, maxCount: Int): Boolean? = result.rowCount
        ?.let { it >= maxCount }

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
