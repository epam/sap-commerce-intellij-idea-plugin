/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch

import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.TransactionMode
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.BooleanUtils

data class FlexibleSearchExecutionContext(
    private val content: String = "",
    private val transactionMode: TransactionMode = TransactionMode.ROLLBACK,
    private val queryMode: QueryMode = QueryMode.FlexibleSearch,
    private val settings: Settings,
    val timeout: Int = HybrisHacHttpClient.DEFAULT_HAC_TIMEOUT,
) : ExecutionContext {

    override val executionTitle: String
        get() = "Executing ${queryMode.title} on the remote SAP Commerce instance…"

    fun params(): Map<String, String> = buildMap {
        put("scriptType", "flexibleSearch")
        put("commit", BooleanUtils.toStringTrueFalse(transactionMode == TransactionMode.COMMIT))
        put("maxCount", settings.maxCount.toString())
        put("user", settings.user)
        put("dataSource", settings.dataSource)
        put("locale", settings.locale)

        if (queryMode == QueryMode.SQL) {
            put("flexibleSearchQuery", "")
            put("sqlQuery", content)
        } else {
            put("flexibleSearchQuery", content)
            put("sqlQuery", "")
        }
    }

    data class Settings(val maxCount: Int, val locale: String, val dataSource: String, val user: String) : ExecutionContext.Settings {
        override fun modifiable() = ModifiableSettings(
            maxCount = maxCount,
            locale = locale,
            dataSource = dataSource,
            user = user
        )
    }

    data class ModifiableSettings(var maxCount: Int, var locale: String, var dataSource: String, var user: String) : ExecutionContext.ModifiableSettings {
        override fun immutable() = Settings(
            maxCount = maxCount,
            locale = locale,
            dataSource = dataSource,
            user = user
        )
    }

    companion object {
        val DEFAULT_SETTINGS by lazy {
            Settings(
                maxCount = 200,
                locale = "en",
                dataSource = "master",
                user = ""
            )
        }

        // Slow operation, do not invoke on EDT
        fun defaultSettings(project: Project) = DEFAULT_SETTINGS.modifiable()
            .apply {
                user = RemoteConnectionService.getInstance(project)
                    .getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
                    .username
            }
            .immutable()
    }
}

enum class QueryMode(val title: String) {
    SQL("SQL"),
    FlexibleSearch("FlexibleSearch"),
    PolyglotQuery("Polyglot Query")
}
