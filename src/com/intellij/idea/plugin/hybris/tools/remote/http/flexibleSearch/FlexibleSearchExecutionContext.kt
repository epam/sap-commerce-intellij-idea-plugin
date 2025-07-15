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

package com.intellij.idea.plugin.hybris.tools.remote.http.flexibleSearch

import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.ExecutionContext
import org.apache.commons.lang3.BooleanUtils

data class FlexibleSearchExecutionContext(
    private val content: String = "",
    private val maxCount: Int = 200,
    private val locale: String = "en",
    private val dataSource: String = "master",
    private val transactionMode: FxSTransactionMode = FxSTransactionMode.ROLLBACK,
    private val queryMode: QueryMode = QueryMode.FlexibleSearch,
    private val user: String? = null,
    val timeout: Int = AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT
) : ExecutionContext {
    fun params(settings: RemoteConnectionSettings): Map<String, String> = buildMap {
        put("scriptType", "flexibleSearch")
        put("commit", BooleanUtils.toStringTrueFalse(transactionMode == FxSTransactionMode.COMMIT))
        put("maxCount", maxCount.toString())
        put("user", user ?: settings.username)
        put("dataSource", dataSource)
        put("locale", locale)

        if (queryMode == QueryMode.FlexibleSearch) {
            put("flexibleSearchQuery", content)
            put("sqlQuery", "")
        } else {
            put("flexibleSearchQuery", "")
            put("sqlQuery", content)
        }
    }
}

enum class QueryMode {
    SQL, FlexibleSearch
}

enum class FxSTransactionMode {
    COMMIT, ROLLBACK
}
