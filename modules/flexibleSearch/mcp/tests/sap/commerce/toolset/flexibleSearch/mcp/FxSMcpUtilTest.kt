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

import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for [rowCount] and [maxCountReached].
 *
 * The HAC does not report whether the result was capped by `maxCount`, therefore a result sized exactly as the
 * requested `maxCount` has to be reported as potentially capped.
 */
class FxSMcpUtilTest {

    private fun result(rows: Int?) = FlexibleSearchExecResult(
        rows = rows?.let { count -> List(count) { listOf("value") } },
    )

    @Test
    fun rowCount_noResultList_isNull() {
        assertNull(result(null).rowCount)
    }

    @Test
    fun rowCount_emptyResultList_isZero() {
        assertEquals(0, result(0).rowCount)
    }

    @Test
    fun rowCount_returnsAmountOfRows() {
        assertEquals(3, result(3).rowCount)
    }

    @Test
    fun maxCountReached_noResultList_isNull() {
        assertNull(result(null).maxCountReached(200))
    }

    @Test
    fun maxCountReached_lessRowsThanMaxCount_isFalse() {
        assertEquals(false, result(199).maxCountReached(200))
    }

    @Test
    fun maxCountReached_asManyRowsAsMaxCount_isTrue() {
        assertEquals(true, result(200).maxCountReached(200))
    }
}
