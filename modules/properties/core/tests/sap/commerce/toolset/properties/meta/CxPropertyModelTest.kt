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
 * Unit tests for the resolution of a property value against the SAP Commerce property chain.
 */
class CxPropertyModelTest {

    private val advanced = CxPropertySource("advanced.properties", CxPropertyScope.ADVANCED)
    private val platformProject = CxPropertySource("project.properties", CxPropertyScope.PROJECT, rank = 0, extension = "platform")
    private val customProject = CxPropertySource("project.properties", CxPropertyScope.PROJECT, rank = 7, extension = "custom")
    private val local = CxPropertySource("local.properties", CxPropertyScope.LOCAL)
    private val optionalConfig = CxPropertySource("10-cloud.properties", CxPropertyScope.OPTIONAL_CONFIG)

    @Test
    fun `a property declared in a single file resolves to its value`() {
        val model = model(platformProject to ("db.url" to "jdbc:platform"))

        assertEquals("jdbc:platform", model.resolve("db.url"))
    }

    @Test
    fun `local properties override the project properties`() {
        val model = model(
            customProject to ("db.url" to "jdbc:custom"),
            local to ("db.url" to "jdbc:local"),
        )

        assertEquals("jdbc:local", model.resolve("db.url"))
    }

    @Test
    fun `the optional config directory overrides the local properties`() {
        val model = model(
            local to ("db.url" to "jdbc:local"),
            optionalConfig to ("db.url" to "jdbc:cloud"),
        )

        assertEquals("jdbc:cloud", model.resolve("db.url"))
    }

    @Test
    fun `the project properties override the advanced properties`() {
        val model = model(
            advanced to ("db.url" to "jdbc:advanced"),
            platformProject to ("db.url" to "jdbc:platform"),
        )

        assertEquals("jdbc:platform", model.resolve("db.url"))
    }

    @Test
    fun `a later loaded extension overrides an earlier one`() {
        val model = model(
            customProject to ("db.url" to "jdbc:custom"),
            platformProject to ("db.url" to "jdbc:platform"),
        )

        assertEquals("jdbc:custom", model.resolve("db.url"))
    }

    @Test
    fun `resolving an undeclared property yields no value`() {
        val model = model(local to ("db.url" to "jdbc:local"))

        assertNull(model.resolve("db.driver"))
        assertFalse("db.driver" in model)
        assertTrue("db.url" in model)
    }

    @Test
    fun `a placeholder is expanded from the winning declaration`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:\${db.host}"),
            local to ("db.host" to "localhost"),
        )

        assertEquals("jdbc:localhost", model.resolve("db.url"))
    }

    @Test
    fun `placeholders are expanded transitively`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:\${db.host}"),
            platformProject to ("db.host" to "\${db.name}.local"),
            platformProject to ("db.name" to "commerce"),
        )

        assertEquals("jdbc:commerce.local", model.resolve("db.url"))
    }

    @Test
    fun `the same placeholder is expanded at every occurrence`() {
        val model = model(
            platformProject to ("db.url" to "\${db.host}:\${db.host}"),
            platformProject to ("db.host" to "localhost"),
        )

        assertEquals("localhost:localhost", model.resolve("db.url"))
    }

    @Test
    fun `text around a placeholder is preserved`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:mysql://\${db.host}:3306/commerce"),
            platformProject to ("db.host" to "localhost"),
        )

        assertEquals("jdbc:mysql://localhost:3306/commerce", model.resolve("db.url"))
    }

    @Test
    fun `a placeholder of an undeclared property is left as it is written`() {
        val model = model(platformProject to ("db.url" to "jdbc:\${db.host}"))

        assertEquals("jdbc:\${db.host}", model.resolve("db.url"))
    }

    @Test
    fun `an unterminated placeholder is left as it is written`() {
        val model = model(platformProject to ("db.url" to "jdbc:\${db.host"))

        assertEquals("jdbc:\${db.host", model.resolve("db.url"))
    }

    @Test
    fun `a self referencing placeholder does not loop`() {
        val model = model(platformProject to ("db.url" to "\${db.url}/commerce"))

        assertEquals("\${db.url}/commerce", model.resolve("db.url"))
    }

    @Test
    fun `a placeholder cycle does not loop`() {
        val model = model(
            platformProject to ("one" to "\${two}"),
            platformProject to ("two" to "\${one}"),
        )

        assertEquals("\${one}", model.resolve("one"))
    }

    @Test
    fun `a placeholder is expanded against the overriding declaration`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:\${db.host}"),
            platformProject to ("db.host" to "platform"),
            local to ("db.host" to "localhost"),
        )

        assertEquals("jdbc:localhost", model.resolve("db.url"))
    }

    @Test
    fun `every declaration of a property is kept with the file it comes from`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:platform"),
            customProject to ("db.url" to "jdbc:custom"),
            local to ("db.url" to "jdbc:local"),
        )

        val property = model["db.url"]

        assertEquals(listOf("jdbc:platform", "jdbc:custom", "jdbc:local"), property?.declarations?.map { it.value })
        assertEquals(listOf("platform/project.properties", "custom/project.properties", "local.properties"), property?.sources?.map { it.presentableName })
    }

    @Test
    fun `declarations contributed by a single file are listed`() {
        val model = model(
            local to ("db.url" to "jdbc:local"),
            local to ("db.driver" to "com.mysql.Driver"),
            platformProject to ("db.url" to "jdbc:platform"),
        )

        assertEquals(listOf("db.driver" to "com.mysql.Driver", "db.url" to "jdbc:local"), model.declarationsIn(local).map { it.key to it.value })
    }

    @Test
    fun `the whole chain resolves to the winning values ordered by key`() {
        val model = model(
            platformProject to ("db.host" to "localhost"),
            platformProject to ("db.url" to "jdbc:\${db.host}"),
            local to ("db.url" to "jdbc:override"),
        )

        assertEquals(mapOf("db.host" to "localhost", "db.url" to "jdbc:override"), model.resolveAll())
        assertEquals(listOf("db.host", "db.url"), model.resolveAll().keys.toList())
    }

    @Test
    fun `resolving a given set of keys keeps only the ones the project declares`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:platform"),
            local to ("db.driver" to "com.mysql.Driver"),
        )

        val resolved = model.resolveProperties(listOf("db.url", "db.driver", "cluster.id"))

        assertEquals(listOf("db.url", "db.driver"), resolved.keys.toList())
        assertEquals(listOf("jdbc:platform", "com.mysql.Driver"), resolved.values.map { it.value })
    }

    @Test
    fun `a resolved property carries the file its winning value was declared in`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:platform"),
            local to ("db.url" to "jdbc:local"),
        )

        val resolved = model.resolveProperties(listOf("db.url"))["db.url"]

        assertEquals("jdbc:local", resolved?.value)
        assertEquals(local, resolved?.source)
        assertEquals("local.properties", resolved?.source?.presentableName)
    }

    @Test
    fun `a resolved property keeps the raw value apart from the expanded one`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:\${db.host}"),
            local to ("db.host" to "localhost"),
        )

        val resolved = model.resolveProperties(listOf("db.url"))["db.url"]

        assertEquals("jdbc:localhost", resolved?.value)
        assertEquals("jdbc:\${db.host}", resolved?.rawValue)
        assertEquals(platformProject, resolved?.source)
    }

    @Test
    fun `a resolved property keeps the declarations it shadows`() {
        val model = model(
            platformProject to ("db.url" to "jdbc:platform"),
            customProject to ("db.url" to "jdbc:custom"),
            local to ("db.url" to "jdbc:local"),
        )

        val resolved = model.resolveProperties(listOf("db.url"))["db.url"]

        assertEquals(listOf("jdbc:platform", "jdbc:custom"), resolved?.property?.shadowedDeclarations?.map { it.value })
    }

    @Test
    fun `resolving a repeated key yields it once`() {
        val model = model(local to ("db.url" to "jdbc:local"))

        assertEquals(listOf("db.url"), model.resolveProperties(listOf("db.url", "db.url")).keys.toList())
    }

    @Test
    fun `sources are ordered from the lowest to the highest precedence`() {
        val model = model(
            local to ("a" to "1"),
            optionalConfig to ("b" to "2"),
            advanced to ("c" to "3"),
            customProject to ("d" to "4"),
            platformProject to ("e" to "5"),
        )

        assertEquals(listOf(advanced, platformProject, customProject, local, optionalConfig), model.sources)
    }

    @Test
    fun `an empty chain resolves nothing`() {
        assertNull(CxPropertyModel.EMPTY.resolve("db.url"))
        assertEquals(emptySet(), CxPropertyModel.EMPTY.keys)
    }

    private fun model(vararg declarations: Pair<CxPropertySource, Pair<String, String>>): CxPropertyModel {
        val sources = declarations.map { it.first }
        val properties = declarations.map { (source, property) -> CxPropertyDeclaration(property.first, property.second, source) }

        return CxPropertyModel.of(sources, properties)
    }
}
