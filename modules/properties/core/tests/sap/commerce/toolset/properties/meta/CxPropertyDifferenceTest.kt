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

package sap.commerce.toolset.properties.meta

import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CxPropertyDifferenceTest {

    @Test
    fun `a property with the same value on both sides is not differing`() {
        assertTrue(CxPropertyComparison.differing(listOf(remote("a", "1")), mapOf("a" to "1")).isEmpty())
    }

    @Test
    fun `a property only the instance has is not differing`() {
        assertTrue(CxPropertyComparison.differing(listOf(remote("a", "1")), emptyMap()).isEmpty())
    }

    @Test
    fun `a property with different values carries both of them`() {
        assertEquals(listOf(CxPropertyDifference("a", "2", "1")), CxPropertyComparison.differing(listOf(remote("a", "1")), mapOf("a" to "2")))
    }

    @Test
    fun `differing properties are ordered by key`() {
        val differing = CxPropertyComparison.differing(listOf(remote("b", "1"), remote("a", "1")), mapOf("a" to "2", "b" to "2"))

        assertEquals(listOf("a", "b"), differing.map { it.key })
    }

    private fun remote(key: String, value: String) = CxPropertyPresentation(key, value)
}
