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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for the accumulated remote property snapshot backing the infinite-scroll view.
 */
class CxRemotePropertyStateTest {

    @Test
    fun `state exposes no snapshot before the first page is loaded`() {
        val state = CxRemotePropertyState()

        assertFalse(state.initialized())
        assertNull(state.get())
    }

    @Test
    fun `state exposes the snapshot once the first page replaces it`() {
        val state = CxRemotePropertyState()

        state.replace(page(lastLoadedPage = 1, totalItems = 2, properties = listOf(property("a"), property("b"))))

        assertTrue(state.initialized())
        assertEquals(listOf("a", "b"), state.get()?.properties?.map { it.key })
    }

    @Test
    fun `appending the next page keeps the previously loaded properties`() {
        val state = CxRemotePropertyState()
        state.replace(page(lastLoadedPage = 1, totalItems = 4, properties = listOf(property("a"), property("b"))))

        state.append(page(lastLoadedPage = 2, totalItems = 4, properties = listOf(property("c"), property("d"))))

        assertEquals(listOf("a", "b", "c", "d"), state.get()?.properties?.map { it.key })
        assertEquals(2, state.get()?.lastLoadedPage)
    }

    @Test
    fun `appending keeps the accumulated properties sorted by key`() {
        val state = CxRemotePropertyState()
        state.replace(page(lastLoadedPage = 1, totalItems = 3, properties = listOf(property("b"))))

        state.append(page(lastLoadedPage = 2, totalItems = 3, properties = listOf(property("c"), property("a"))))

        assertEquals(listOf("a", "b", "c"), state.get()?.properties?.map { it.key })
    }

    @Test
    fun `appending a property which is already loaded does not duplicate it`() {
        val state = CxRemotePropertyState()
        state.replace(page(lastLoadedPage = 1, totalItems = 2, properties = listOf(property("a", "one"))))

        state.append(page(lastLoadedPage = 2, totalItems = 2, properties = listOf(property("a", "two"), property("b"))))

        assertEquals(listOf("a", "b"), state.get()?.properties?.map { it.key })
        assertEquals("one", state.get()?.properties?.first()?.value)
    }

    @Test
    fun `appending a page fetched for another filter discards the stale accumulation`() {
        val state = CxRemotePropertyState()
        state.replace(page(lastLoadedPage = 1, totalItems = 2, properties = listOf(property("a"), property("b"))))

        state.append(page(lastLoadedPage = 1, totalItems = 1, properties = listOf(property("z")), keyFilter = "z"))

        assertEquals(listOf("z"), state.get()?.properties?.map { it.key })
    }

    @Test
    fun `appending a page fetched with another page size discards the stale accumulation`() {
        val state = CxRemotePropertyState()
        state.replace(page(lastLoadedPage = 1, totalItems = 3, properties = listOf(property("a"))))

        state.append(page(lastLoadedPage = 1, totalItems = 3, properties = listOf(property("b")), pageSize = 100))

        assertEquals(listOf("b"), state.get()?.properties?.map { it.key })
    }

    @Test
    fun `clearing the state hides the previously loaded snapshot`() {
        val state = CxRemotePropertyState()
        state.replace(page(lastLoadedPage = 1, totalItems = 1, properties = listOf(property("a"))))

        state.clear()

        assertFalse(state.initialized())
        assertNull(state.get())
    }

    @Test
    fun `state seeded with an initial page is immediately readable`() {
        val state = CxRemotePropertyState(page(lastLoadedPage = 1, totalItems = 1, properties = listOf(property("a"))))

        assertTrue(state.initialized())
        assertEquals(listOf("a"), state.get()?.properties?.map { it.key })
    }

    private fun property(key: String, value: String = "value") = CxPropertyPresentation(key, value)

    private fun page(
        lastLoadedPage: Int,
        totalItems: Int,
        properties: List<CxPropertyPresentation>,
        pageSize: Int = 50,
        keyFilter: String = "",
        valueFilter: String = "",
    ) = CxRemotePropertyStatePage(
        lastLoadedPage = lastLoadedPage,
        pageSize = pageSize,
        totalItems = totalItems,
        keyFilter = keyFilter,
        valueFilter = valueFilter,
        properties = properties,
    )
}
