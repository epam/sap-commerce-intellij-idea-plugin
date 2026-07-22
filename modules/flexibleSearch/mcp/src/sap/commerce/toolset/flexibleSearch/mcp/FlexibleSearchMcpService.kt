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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.http.HttpStatus
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.mcp.dto.FlexibleSearchMcpResult
import sap.commerce.toolset.hac.mcp.HacMcpService

@Service(Service.Level.PROJECT)
class FlexibleSearchMcpService(private val project: Project) {

    suspend fun execute(
        context: FlexibleSearchMcpContext,
    ): FlexibleSearchMcpResult {
        val connection = HacMcpService.getInstance(project).resolveConnection(context.connectionName)
        val execContext = FlexibleSearchExecContext(
            connection = connection,
            content = context.query,
            queryMode = context.queryMode,
            maxCount = context.maxCount.coerceIn(1, 200),
            locale = context.locale,
            dataSource = context.dataSource,
            user = context.user,
            timeout = connection.timeout,
        )

        val result = FlexibleSearchExecClient.getInstance(project).execute(execContext)

        return if (result.statusCode != HttpStatus.SC_OK) {
            FlexibleSearchMcpResult(
                connection = connection.connectionName,
                success = false,
                error = result.errorMessage,
                errorDetail = result.errorDetailMessage,
                rawResult = result
            )
        } else {
            FlexibleSearchMcpResult(
                connection = connection.connectionName,
                success = true,
                output = result.output?.takeIf { it.isNotBlank() },
                rawResult = result
            )
        }
    }

    companion object {
        fun getInstance(project: Project): FlexibleSearchMcpService = project.service()
    }
}
