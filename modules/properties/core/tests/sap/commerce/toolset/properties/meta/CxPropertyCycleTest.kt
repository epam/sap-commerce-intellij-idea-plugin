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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CxPropertyCycleTest {

    @Test
    fun `a property referring to itself leads back to itself`() {
        assertTrue(model("a" to "\${a}").leadsBackTo("a", "a"))
    }

    @Test
    fun `two properties referring to each other lead back to the first`() {
        assertTrue(model("a" to "\${b}", "b" to "\${a}").leadsBackTo("b", "a"))
    }

    @Test
    fun `a chain of references ending in a plain value does not lead back`() {
        assertFalse(model("a" to "\${b}", "b" to "\${c}", "c" to "plain").leadsBackTo("b", "a"))
    }

    @Test
    fun `a reference to an undeclared property does not lead back`() {
        assertFalse(model("a" to "\${missing}").leadsBackTo("missing", "a"))
    }

    @Test
    fun `a cycle elsewhere does not count as leading back`() {
        assertFalse(model("a" to "\${b}", "b" to "\${c}", "c" to "\${b}").leadsBackTo("b", "a"))
    }

    private fun model(vararg declarations: Pair<String, String>) = CxPropertyModel.of(
        listOf(SOURCE),
        declarations.map { (key, value) -> CxPropertyDeclaration(key, value, SOURCE) },
    )

    companion object {
        private val SOURCE = CxPropertySource("local.properties", CxPropertyScope.LOCAL)
    }
}
