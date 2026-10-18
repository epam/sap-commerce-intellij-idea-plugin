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
import kotlin.test.assertFalse

class CxGroovyStringLiteralTest {

    @Test
    fun `a plain property key is kept as it is`() {
        assertEquals("db.url", CxGroovyStringLiteral.escape("db.url"))
    }

    @Test
    fun `a single quote cannot end the literal`() {
        assertEquals("a\\u0027b", CxGroovyStringLiteral.escape("a'b"))
    }

    @Test
    fun `a backslash is escaped instead of starting an escape sequence`() {
        assertEquals("a\\u005cb", CxGroovyStringLiteral.escape("a\\b"))
    }

    @Test
    fun `a line break is escaped instead of breaking the literal`() {
        assertEquals("a\\u000ab", CxGroovyStringLiteral.escape("a\nb"))
    }

    @Test
    fun `a placeholder token in the input is not left for a later substitution`() {
        assertFalse(CxGroovyStringLiteral.escape("[valueFilterPlaceholder]").contains("[valueFilterPlaceholder]"))
    }
}
