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

package sap.commerce.toolset.flexibleSearch.exec.context

import com.intellij.openapi.util.Key
import org.apache.commons.lang3.BooleanUtils
import sap.commerce.toolset.exec.context.ExecContext
import sap.commerce.toolset.hac.HacExecConstants
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.settings.state.TransactionMode

data class FlexibleSearchExecContext(
    val connection: HacConnectionSettingsState,
    private val content: String = "",
    private val transactionMode: TransactionMode = TransactionMode.ROLLBACK,
    private val queryMode: QueryMode = QueryMode.FlexibleSearch,
    val settings: Settings,
) : ExecContext {

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

    data class Settings(
        val maxCount: Int,
        val locale: String,
        val dataSource: String,
        val user: String,
        override val timeout: Int
    ) : ExecContext.Settings {
        override fun mutable() = Mutable(
            maxCount = maxCount,
            locale = locale,
            dataSource = dataSource,
            user = user,
            timeout = timeout,
        )

        data class Mutable(
            var maxCount: Int,
            var locale: String,
            var dataSource: String,
            var user: String,
            override var timeout: Int
        ) : ExecContext.Settings.Mutable {
            override fun immutable() = Settings(
                maxCount = maxCount,
                locale = locale,
                dataSource = dataSource,
                user = user,
                timeout = timeout,
            )
        }
    }

    companion object {
        val KEY_EXECUTION_SETTINGS = Key.create<Settings>("sap.cx.fxs.execution.settings")

        fun defaultSettings(connectionSettings: HacConnectionSettingsState? = null) = Settings(
            maxCount = 200,
            locale = "en",
            dataSource = "master",
            user = "from active connection",
            timeout = connectionSettings?.timeout ?: HacExecConstants.DEFAULT_TIMEOUT,
        )
    }
}