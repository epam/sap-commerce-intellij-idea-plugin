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

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.http.HttpStatus
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.flexibleSearch.mcp.context.FxSMcpExecRequest
import sap.commerce.toolset.flexibleSearch.mcp.context.FxSTransformMcpContext
import sap.commerce.toolset.flexibleSearch.mcp.dto.FxSMcpResult
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchElementFactory
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.mcp.HacMcpService
import sap.commerce.toolset.transform.Transformer

@Service(Service.Level.PROJECT)
class FxSMcpService(private val project: Project) {

    suspend fun execute(execRequest: FxSMcpExecRequest): FxSMcpResult {
        val connection = execRequest.connection
        val result = execute(execRequest, connection)

        return if (result.statusCode != HttpStatus.SC_OK) {
            FxSMcpResult(
                connectionName = connection.connectionName,
                success = false,
                error = result.errorMessage,
                errorDetail = result.errorDetailMessage,
            )
        } else {
            FxSMcpResult(
                connectionName = connection.connectionName,
                success = true,
                output = result.output?.takeIf { it.isNotBlank() },
            )
        }
    }

    suspend fun transform(context: FxSTransformMcpContext): FxSMcpResult {
        val psiFile = readAction { FlexibleSearchElementFactory.createFile(project, context.query) }

        val transformer = Transformer.EP.extensionList
            .find { it.isApplicable(psiFile) && it.id.equals(context.transformerId, true) }
            ?: error("No applicable '${context.transformerId}' transformer found for FlexibleSearch")

        val execRequest = context.execRequest
        val connection = execRequest.connection

        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_TYPE_SYSTEM_UNIQUE, context.includeTypeSystemUnique)
        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_DATA, context.includeData)
        psiFile.putUserData(FlexibleSearchExecConstants.Transform.CONNECTION, connection)
        psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_SETTINGS, execRequest.execSettings(connection))

        if (context.includeData) {
            val result = execute(execRequest, connection)
            psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_RESULTS, result)
        }

        val transformationResult = transformer.transform(psiFile)

        return FxSMcpResult(
            connectionName = connection.connectionName,
            success = true,
            output = transformationResult.content,
            description = transformationResult.description,
        )
    }

    private suspend fun execute(
        execRequest: FxSMcpExecRequest,
        connection: HacConnectionSettingsState
    ): FlexibleSearchExecResult {
        val execSettings = execRequest.execSettings(connection)

        val execContext = FlexibleSearchExecContext(
            connection = connection,
            content = execRequest.query,
            queryMode = execRequest.queryMode,
            settings = execSettings
        )

        return FlexibleSearchExecClient.getInstance(project).execute(execContext)
    }

    private val FxSMcpExecRequest.connection
        get() = HacMcpService.getInstance(project).resolveConnection(connectionName)

    private fun FxSMcpExecRequest.execSettings(connection: HacConnectionSettingsState) = FlexibleSearchExecContext.Settings(
        maxCount = maxCount,
        locale = locale,
        dataSource = dataSource,
        user = user,
        timeout = timeout ?: connection.timeout
    )

    companion object {
        fun getInstance(project: Project): FxSMcpService = project.service()
    }
}
