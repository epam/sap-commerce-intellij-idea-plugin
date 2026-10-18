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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for a single property and the declarations it is assembled from.
 */
class CxPropertyTest {

    private val platformProject = CxPropertySource("project.properties", CxPropertyScope.PROJECT, rank = 0, extension = "platform")
    private val customProject = CxPropertySource("project.properties", CxPropertyScope.PROJECT, rank = 7, extension = "custom")
    private val local = CxPropertySource("local.properties", CxPropertyScope.LOCAL)

    @Test
    fun `declarations are ordered from the lowest to the highest precedence`() {
        val property = CxProperty.of(
            "db.url", listOf(
                declaration(local, "jdbc:local"),
                declaration(platformProject, "jdbc:platform"),
                declaration(customProject, "jdbc:custom"),
            )
        )

        assertEquals(listOf(platformProject, customProject, local), property.sources)
    }

    @Test
    fun `the winning declaration is the one of the highest source`() {
        val property = CxProperty.of(
            "db.url", listOf(
                declaration(local, "jdbc:local"),
                declaration(platformProject, "jdbc:platform"),
            )
        )

        assertEquals("jdbc:local", property.rawValue)
        assertEquals(local, property.declaration?.source)
    }

    @Test
    fun `shadowed declarations exclude the winning one`() {
        val property = CxProperty.of(
            "db.url", listOf(
                declaration(platformProject, "jdbc:platform"),
                declaration(customProject, "jdbc:custom"),
                declaration(local, "jdbc:local"),
            )
        )

        assertTrue(property.isShadowed)
        assertEquals(listOf("jdbc:platform", "jdbc:custom"), property.shadowedDeclarations.map { it.value })
    }

    @Test
    fun `a property declared in a single file is not shadowed`() {
        val property = CxProperty.of("db.url", listOf(declaration(local, "jdbc:local")))

        assertFalse(property.isShadowed)
        assertEquals(emptyList(), property.shadowedDeclarations)
    }

    @Test
    fun `the value of a property in a given file is the one written there`() {
        val property = CxProperty.of(
            "db.url", listOf(
                declaration(platformProject, "jdbc:platform"),
                declaration(local, "jdbc:local"),
            )
        )

        assertEquals("jdbc:platform", property.valueIn(platformProject))
        assertEquals("jdbc:local", property.valueIn(local))
        assertNull(property.valueIn(customProject))
    }

    @Test
    fun `declarations can be narrowed down to a single scope`() {
        val property = CxProperty.of(
            "db.url", listOf(
                declaration(platformProject, "jdbc:platform"),
                declaration(customProject, "jdbc:custom"),
                declaration(local, "jdbc:local"),
            )
        )

        assertEquals(listOf("jdbc:platform", "jdbc:custom"), property.declaredIn(CxPropertyScope.PROJECT).map { it.value })
    }

    @Test
    fun `the last declaration of a file wins over its earlier duplicates`() {
        val property = CxProperty.of(
            "db.url", listOf(
                declaration(local, "jdbc:first"),
                declaration(local, "jdbc:last"),
            )
        )

        assertEquals("jdbc:last", property.rawValue)
    }

    @Test
    fun `a property without declarations has no value`() {
        val property = CxProperty.of("db.url", emptyList())

        assertNull(property.declaration)
        assertNull(property.rawValue)
    }

    private fun declaration(source: CxPropertySource, value: String) = CxPropertyDeclaration("db.url", value, source)
}
