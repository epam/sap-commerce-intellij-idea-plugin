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
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.flexibleSearch.mcp.dto.FlexibleSearchResult
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

@Service(Service.Level.PROJECT)
class FlexibleSearchMcpService(private val project: Project) {

    suspend fun execute(
        connection: HacConnectionSettingsState,
        query: String,
        queryMode: QueryMode,
        maxCount: Int,
        locale: String,
        dataSource: String,
        user: String?,
    ): FlexibleSearchResult {
        val context = FlexibleSearchExecContext(
            connection = connection,
            content = query,
            queryMode = queryMode,
            maxCount = maxCount.coerceIn(1, 200),
            locale = locale,
            dataSource = dataSource,
            user = user,
            timeout = connection.timeout,
        )

        val result = FlexibleSearchExecClient.getInstance(project).execute(context)

        return if (result.statusCode != HttpStatus.SC_OK) {
            FlexibleSearchResult(
                connection = connection.connectionName,
                success = false,
                error = result.errorMessage,
                errorDetail = result.errorDetailMessage,
            )
        } else {
            FlexibleSearchResult(
                connection = connection.connectionName,
                success = true,
                output = result.output?.takeIf { it.isNotBlank() },
            )
        }
    }

    companion object {
        fun getInstance(project: Project): FlexibleSearchMcpService = project.service()
    }
}
