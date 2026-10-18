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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for reading what the state script returns.
 */
class CxRemotePropertyPayloadTest {

    @Test
    fun `a paged payload is read into the page it describes`() {
        val page = CxRemotePropertyPayload.parse(
            """{"page":2,"pageSize":50,"totalItems":137,"keyFilter":"db","valueFilter":"","items":[{"key":"db.url","value":"jdbc:mine"}]}"""
        )

        assertEquals(2, page?.lastLoadedPage)
        assertEquals(50, page?.pageSize)
        assertEquals(137, page?.totalItems)
        assertEquals("db", page?.keyFilter)
        assertEquals("", page?.valueFilter)
        assertEquals(listOf("db.url"), page?.properties?.map { it.key })
        assertEquals(listOf("jdbc:mine"), page?.properties?.map { it.value })
    }

    @Test
    fun `an instance running an older script answers with a bare array, which counts as one whole page`() {
        val page = CxRemotePropertyPayload.parse("""[{"key":"b","value":"2"},{"key":"a","value":"1"}]""")

        assertEquals(1, page?.lastLoadedPage)
        assertEquals(2, page?.totalItems)
        assertEquals(2, page?.pageSize)
        assertEquals(false, page?.hasMore)
    }

    @Test
    fun `properties come back ordered by key whichever shape they arrived in`() {
        val page = CxRemotePropertyPayload.parse("""[{"key":"z","value":"1"},{"key":"a","value":"2"}]""")

        assertEquals(listOf("a", "z"), page?.properties?.map { it.key })
    }

    @Test
    fun `a property without a value is kept rather than dropped`() {
        val page = CxRemotePropertyPayload.parse("""[{"key":"empty.one"}]""")

        assertEquals(listOf("empty.one"), page?.properties?.map { it.key })
    }

    @Test
    fun `an entry without a key is not a property and is left out`() {
        val page = CxRemotePropertyPayload.parse("""[{"value":"orphan"},{"key":"real","value":"1"}]""")

        assertEquals(listOf("real"), page?.properties?.map { it.key })
    }

    @Test
    fun `an empty instance is an empty page, not a failure`() {
        val page = CxRemotePropertyPayload.parse("""{"page":1,"pageSize":50,"totalItems":0,"items":[]}""")

        assertEquals(emptyList(), page?.properties)
        assertEquals(0, page?.totalItems)
    }

    @Test
    fun `a payload missing the counts a page is made of cannot be read`() {
        assertNull(CxRemotePropertyPayload.parse("""{"page":1,"items":[]}"""))
    }

    @Test
    fun `something which is not a payload at all cannot be read`() {
        assertNull(CxRemotePropertyPayload.parse("not json"))
        assertNull(CxRemotePropertyPayload.parse("42"))
    }
}
