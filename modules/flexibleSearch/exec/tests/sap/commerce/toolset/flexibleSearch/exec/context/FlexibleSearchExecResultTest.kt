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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for [FlexibleSearchExecResult.rowCount].
 */
class FlexibleSearchExecResultTest {

    private fun result(rows: Int?) = FlexibleSearchExecResult(
        rows = rows?.let { count -> List(count) { listOf("value") } },
    )

    @Test
    fun `row count is unknown when the response carried no result list`() {
        assertNull(result(null).rowCount)
    }

    @Test
    fun `row count is zero when the result list is empty`() {
        assertEquals(0, result(0).rowCount)
    }

    @Test
    fun `row count is the amount of the returned data rows`() {
        assertEquals(3, result(3).rowCount)
    }
}
