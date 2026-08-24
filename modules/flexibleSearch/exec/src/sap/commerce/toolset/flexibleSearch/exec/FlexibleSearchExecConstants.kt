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

package sap.commerce.toolset.flexibleSearch.exec

import com.intellij.openapi.util.Key
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState

object FlexibleSearchExecConstants {

    object Defaults {
        const val MAX_COUNT = 200
        const val LOCALE = "EN"
        const val DATA_SOURCE = "master"
    }

    object Limits {
        /**
         * Number of the rows the HAC FlexibleSearch console is expected to return, a query asking for more rows
         * is executed on the Service Layer via Groovy instead.
         */
        const val HAC_MAX_COUNT = 200
    }

    object Scripts {
        const val EXECUTE = "scripts/flexibleSearch-execute.groovy"
        const val PLACEHOLDER_QUERY = "placeholder_query"
        const val PLACEHOLDER_COLUMN_COUNT = "placeholder_columnCount"
        const val PLACEHOLDER_MAX_COUNT = "placeholder_maxCount"
        const val PLACEHOLDER_LOCALE = "placeholder_locale"
        const val PLACEHOLDER_USER = "placeholder_user"

        /**
         * The query is injected into a triple quoted Groovy string, therefore it cannot carry one itself.
         */
        const val TRIPLE_QUOTE = "'''"
    }

    object Transform {
        val CONNECTION = Key.create<HacConnectionSettingsState>("flexibleSearch.transform.connection")
        val EXEC_SETTINGS = Key.create<FlexibleSearchExecContext.Settings>("flexibleSearch.transform.execSettings")
        val EXEC_RESULTS = Key.create<FlexibleSearchExecResult>("flexibleSearch.transform.execResults")
    }
}