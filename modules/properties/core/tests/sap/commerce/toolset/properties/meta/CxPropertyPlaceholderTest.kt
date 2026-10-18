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

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for finding the `${...}` references inside a property value.
 */
class CxPropertyPlaceholderTest {

    @Test
    fun `a value without a placeholder has none`() {
        assertEquals(emptyList(), CxPropertyPlaceholder.of("jdbc:mysql://localhost/hybris"))
    }

    @Test
    fun `a placeholder is found with the range it occupies`() {
        val placeholder = CxPropertyPlaceholder.of("\${HYBRIS_BIN_DIR}/custom").single()

        assertEquals("HYBRIS_BIN_DIR", placeholder.key)
        assertEquals(0..16, placeholder.range)
        assertEquals(2..15, placeholder.keyRange)
    }

    @Test
    fun `the key range covers the key alone`() {
        val value = "prefix-\${db.url}-suffix"
        val placeholder = CxPropertyPlaceholder.of(value).single()

        assertEquals("\${db.url}", value.substring(placeholder.range.first, placeholder.range.last + 1))
        assertEquals("db.url", value.substring(placeholder.keyRange.first, placeholder.keyRange.last + 1))
    }

    @Test
    fun `every placeholder of a value is found, in the order they are written`() {
        val placeholders = CxPropertyPlaceholder.of("\${a}/\${b}/\${c}")

        assertEquals(listOf("a", "b", "c"), placeholders.map { it.key })
    }

    @Test
    fun `an unterminated placeholder is not one`() {
        assertEquals(emptyList(), CxPropertyPlaceholder.of("\${unterminated"))
        assertEquals(listOf("closed"), CxPropertyPlaceholder.of("\${closed} and \${not").map { it.key })
    }

    @Test
    fun `an empty placeholder is still a placeholder, and refers to nothing`() {
        assertEquals(listOf(""), CxPropertyPlaceholder.of("\${}").map { it.key })
    }

    @Test
    fun `a lone dollar or brace is not a placeholder`() {
        assertEquals(emptyList(), CxPropertyPlaceholder.of("100$ {not.a.key}"))
    }
}
