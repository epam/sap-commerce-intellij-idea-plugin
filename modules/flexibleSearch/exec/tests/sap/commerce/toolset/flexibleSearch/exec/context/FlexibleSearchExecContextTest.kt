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

package sap.commerce.toolset.flexibleSearch.exec.context

import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [FlexibleSearchExecContext.executableOnServiceLayer].
 *
 * The HAC FlexibleSearch console does not return more rows than its own limit, therefore a query asking for more
 * has to be executed on the Service Layer via Groovy instead.
 */
class FlexibleSearchExecContextTest {

    private fun context(
        content: String = "SELECT {pk} FROM {Product}",
        maxCount: Int = FlexibleSearchExecConstants.Limits.HAC_MAX_COUNT,
        queryMode: QueryMode = QueryMode.FlexibleSearch,
    ) = FlexibleSearchExecContext(
        connection = HacConnectionSettingsState(),
        content = content,
        queryMode = queryMode,
        settings = FlexibleSearchExecContext.defaultSettings().copy(maxCount = maxCount),
    )

    @Test
    fun `query within the console limit is executed on the HAC`() {
        assertFalse(context(maxCount = FlexibleSearchExecConstants.Limits.HAC_MAX_COUNT).executableOnServiceLayer)
    }

    @Test
    fun `query asking for more rows than the console limit is executed on the service layer`() {
        assertTrue(context(maxCount = FlexibleSearchExecConstants.Limits.HAC_MAX_COUNT + 1).executableOnServiceLayer)
    }

    @Test
    fun `raw SQL is never executed on the service layer`() {
        assertFalse(context(maxCount = 1_000, queryMode = QueryMode.SQL).executableOnServiceLayer)
    }

    @Test
    fun `query carrying a triple quote is never executed on the service layer`() {
        assertFalse(context(content = "SELECT {pk} FROM {Product} WHERE {code} = '''x'''", maxCount = 1_000).executableOnServiceLayer)
    }
}
