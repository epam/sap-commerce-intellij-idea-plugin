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

package sap.commerce.toolset.properties.ui

import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CxPropertyCounterpartTest {

    private val counterpart = CxPropertyCounterpart.remote(mapOf("a" to "1", "b" to "2"))

    @Test
    fun `a property with the same value on the other side agrees`() {
        assertNull(counterpart.disagreementWith(CxPropertyPresentation("a", "1")))
    }

    @Test
    fun `a property with another value on the other side disagrees with that value`() {
        assertEquals("2", counterpart.disagreementWith(CxPropertyPresentation("b", "3")))
    }

    @Test
    fun `a property the other side does not have is not a disagreement`() {
        assertNull(counterpart.disagreementWith(CxPropertyPresentation("c", "1")))
    }

    @Test
    fun `only the disagreeing properties are counted`() {
        val properties = listOf(CxPropertyPresentation("a", "1"), CxPropertyPresentation("b", "3"), CxPropertyPresentation("c", "1"))

        assertEquals(1, counterpart.countDisagreements(properties))
    }
}
