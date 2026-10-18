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

class CxEnvironmentPropertyTest {

    @Test
    fun `a single underscore becomes a dot`() {
        assertEquals("my.property", CxEnvironmentProperty.keyOf("my_property"))
    }

    @Test
    fun `a double underscore becomes a literal underscore`() {
        assertEquals("my_property", CxEnvironmentProperty.keyOf("my__property"))
    }

    @Test
    fun `single and double underscores combine`() {
        assertEquals("a_b.c", CxEnvironmentProperty.keyOf("a__b_c"))
    }

    @Test
    fun `a name without underscores is kept as it is`() {
        assertEquals("platformhome", CxEnvironmentProperty.keyOf("platformhome"))
    }
}
