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

package sap.commerce.toolset.properties.exec

import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the paging arithmetic driving the infinite-scroll trigger.
 */
class CxRemotePropertyStatePageTest {

    @Test
    fun `loaded count is the amount of accumulated properties`() {
        assertEquals(2, page(totalItems = 10, loaded = 2).loadedCount)
    }

    @Test
    fun `more properties can be fetched while fewer are loaded than the server reports`() {
        assertTrue(page(totalItems = 10, loaded = 2).hasMore)
    }

    @Test
    fun `no more properties can be fetched once every reported property is loaded`() {
        assertFalse(page(totalItems = 2, loaded = 2).hasMore)
    }

    @Test
    fun `no more properties can be fetched when the filter matched nothing`() {
        assertFalse(page(totalItems = 0, loaded = 0).hasMore)
    }

    private fun page(totalItems: Int, loaded: Int) = CxRemotePropertyStatePage(
        lastLoadedPage = 1,
        pageSize = 50,
        totalItems = totalItems,
        keyFilter = "",
        valueFilter = "",
        properties = List(loaded) { CxPropertyPresentation("key$it", "value$it") },
    )
}
