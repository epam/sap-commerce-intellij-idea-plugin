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

package sap.commerce.toolset.properties

import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyState
import sap.commerce.toolset.properties.custom.settings.state.CxCustomPropertyTemplateState
import kotlin.test.Test
import kotlin.test.assertEquals

class CxCustomPropertyTemplateUtilTest {

    @Test
    fun `adding a new property puts it last`() {
        val template = template("a" to "1").withProperty("b", "2")

        assertEquals(listOf("a" to "1", "b" to "2"), template.pairs())
    }

    @Test
    fun `adding a property which already exists replaces its value in place`() {
        val template = template("a" to "1", "b" to "2").withProperty("a", "3")

        assertEquals(listOf("a" to "3", "b" to "2"), template.pairs())
    }

    @Test
    fun `updating the value of a property keeps the other properties`() {
        val template = template("a" to "1", "b" to "2").withValue("b", "3")

        assertEquals(listOf("a" to "1", "b" to "3"), template.pairs())
    }

    @Test
    fun `updating the value of a missing property adds nothing`() {
        val template = template("a" to "1").withValue("b", "2")

        assertEquals(listOf("a" to "1"), template.pairs())
    }

    @Test
    fun `deleting a property removes only that property`() {
        val template = template("a" to "1", "b" to "2").withoutProperty("a")

        assertEquals(listOf("b" to "2"), template.pairs())
    }

    @Test
    fun `replacing a template keeps its position in the list`() {
        val first = template(uuid = "1")
        val second = template(uuid = "2")
        val renamed = second.copy(name = "renamed")

        assertEquals(listOf(first, renamed, template(uuid = "3")), listOf(first, second, template(uuid = "3")).replacing(renamed))
    }

    @Test
    fun `replacing a template which is not in the list appends it`() {
        val first = template(uuid = "1")
        val other = template(uuid = "2")

        assertEquals(listOf(first, other), listOf(first).replacing(other))
    }

    private fun template(vararg properties: Pair<String, String>, uuid: String = "uuid") = CxCustomPropertyTemplateState(
        uuid = uuid,
        name = "template",
        properties = properties.map { (key, value) -> CxCustomPropertyState(key, value) },
    )

    private fun CxCustomPropertyTemplateState.pairs() = properties.map { it.key to it.value }
}
