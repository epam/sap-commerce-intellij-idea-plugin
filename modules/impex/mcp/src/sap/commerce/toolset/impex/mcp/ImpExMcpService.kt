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

package sap.commerce.toolset.impex.mcp

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.http.HttpStatus
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.impex.exec.ImpExExecClient
import sap.commerce.toolset.impex.exec.context.ImpExExecContext
import sap.commerce.toolset.impex.exec.context.ImpExExecutionMode
import sap.commerce.toolset.impex.mcp.dto.ImpExResult

@Service(Service.Level.PROJECT)
class ImpExMcpService(private val project: Project) {

    suspend fun execute(connection: HacConnectionSettingsState, content: String, validate: Boolean): ImpExResult {
        val context = ImpExExecContext(
            connection = connection,
            content = content,
            executionMode = if (validate) ImpExExecutionMode.VALIDATE else ImpExExecutionMode.IMPORT,
            settings = ImpExExecContext.defaultSettings(connection),
        )

        val result = ImpExExecClient.getInstance(project).execute(context)
        val action = if (validate) "Validation" else "Import"

        return if (result.statusCode != HttpStatus.SC_OK) {
            ImpExResult(
                connection = connection.connectionName,
                action = action,
                success = false,
                error = result.errorMessage,
                errorDetail = result.errorDetailMessage,
            )
        } else {
            ImpExResult(
                connection = connection.connectionName,
                action = action,
                success = true,
                output = result.output?.takeIf { it.isNotBlank() },
            )
        }
    }

    companion object {
        fun getInstance(project: Project): ImpExMcpService = project.service()
    }
}
