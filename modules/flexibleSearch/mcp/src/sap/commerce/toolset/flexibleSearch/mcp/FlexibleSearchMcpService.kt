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
import sap.commerce.toolset.flexibleSearch.mcp.context.FlexibleSearchMcpContext
import sap.commerce.toolset.flexibleSearch.mcp.context.FlexibleSearchTransformMcpContext
import sap.commerce.toolset.flexibleSearch.mcp.dto.FlexibleSearchMcpResult
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchElementFactory
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.hac.mcp.HacMcpService
import sap.commerce.toolset.transform.Transformer

@Service(Service.Level.PROJECT)
class FlexibleSearchMcpService(private val project: Project) {

    suspend fun execute(context: FlexibleSearchMcpContext): FlexibleSearchMcpResult {
        val connection = context.connection()
        val execSettings = context.execSettings(connection)

        val execContext = FlexibleSearchExecContext(
            connection = connection,
            content = context.query,
            queryMode = context.queryMode,
            settings = execSettings
        )

        val result = FlexibleSearchExecClient.getInstance(project).execute(execContext)

        return if (result.statusCode != HttpStatus.SC_OK) {
            FlexibleSearchMcpResult(
                connectionName = connection.connectionName,
                success = false,
                error = result.errorMessage,
                errorDetail = result.errorDetailMessage,
                rawResult = result
            )
        } else {
            FlexibleSearchMcpResult(
                connectionName = connection.connectionName,
                success = true,
                output = result.output?.takeIf { it.isNotBlank() },
                rawResult = result
            )
        }
    }

    suspend fun transform(context: FlexibleSearchTransformMcpContext): FlexibleSearchMcpResult {
        val psiFile = readAction { FlexibleSearchElementFactory.createFile(project, context.query) }

        val transformer = Transformer.EP.extensionList
            .find { it.isApplicable(psiFile) && it.name.equals(context.transformerName, true) }
            ?: error("No applicable '${context.transformerName}' transformer found for FlexibleSearch")

        val execContext = context.execContext
        val connection = execContext.connection()

        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_TYPE_SYSTEM_UNIQUE, context.includeTypeSystemUnique)
        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_DATA, context.includeData)
        psiFile.putUserData(FlexibleSearchExecConstants.Transform.CONNECTION, connection)
        psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_SETTINGS, execContext.execSettings(connection))

        if (context.includeData) {
            val result = execute(execContext)
            psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_RESULTS, result.rawResult)
        }

        val transformationResult = transformer.transform(psiFile)

        return FlexibleSearchMcpResult(
            connectionName = connection.connectionName,
            success = true,
            output = transformationResult.content,
            description = transformationResult.description,
        )
    }

    private fun FlexibleSearchMcpContext.connection() = HacMcpService.getInstance(project).resolveConnection(connectionName)

    private fun FlexibleSearchMcpContext.execSettings(connection: HacConnectionSettingsState) = FlexibleSearchExecContext.Settings(
        maxCount = maxCount,
        locale = locale,
        dataSource = dataSource,
        user = user,
        timeout = timeout ?: connection.timeout
    )

    companion object {
        fun getInstance(project: Project): FlexibleSearchMcpService = project.service()
    }
}
