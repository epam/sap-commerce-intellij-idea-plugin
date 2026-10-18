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

/**
 * Unit tests for the report comparing a remote instance against the project's property chain.
 */
class CxPropertyComparisonTest {

    private val local = CxPropertySource("local.properties", CxPropertyScope.LOCAL)
    private val unusedProject = CxPropertySource("project.properties", CxPropertyScope.PROJECT, active = false, extension = "unused")

    @Test
    fun `a remote property the project declares is not reported as missing in the project`() {
        val chain = chain("db.url" to "jdbc:local")

        assertEquals(emptyList(), CxPropertyComparison.missingInProject(listOf(remote("db.url")), chain))
    }

    @Test
    fun `a remote property the project never declares is reported as missing in the project`() {
        val chain = chain("db.url" to "jdbc:local")

        val missing = CxPropertyComparison.missingInProject(listOf(remote("db.url"), remote("cluster.id", "7")), chain)

        assertEquals(listOf("cluster.id"), missing.map { it.key })
        assertEquals(listOf("7"), missing.map { it.value })
    }

    @Test
    fun `a remote property declared only outside the active configuration is reported as missing in the project`() {
        val chain = CxPropertyModel.of(
            listOf(unusedProject),
            listOf(CxPropertyDeclaration("db.url", "jdbc:unused", unusedProject)),
        )

        assertEquals(listOf("db.url"), CxPropertyComparison.missingInProject(listOf(remote("db.url")), chain).map { it.key })
    }

    @Test
    fun `properties missing in the project are ordered by key`() {
        val chain = chain()

        val missing = CxPropertyComparison.missingInProject(listOf(remote("z.one"), remote("a.two")), chain)

        assertEquals(listOf("a.two", "z.one"), missing.map { it.key })
    }

    @Test
    fun `a project property the remote instance has is not reported as missing on the remote`() {
        val chain = chain("db.url" to "jdbc:local")

        assertEquals(emptyList(), CxPropertyComparison.missingOnRemote(listOf(remote("db.url")), chain))
    }

    @Test
    fun `a project property the remote instance lacks is reported with the value the project resolves to`() {
        val chain = chain("db.url" to "jdbc:local", "db.driver" to "com.mysql.Driver")

        val missing = CxPropertyComparison.missingOnRemote(listOf(remote("db.url")), chain)

        assertEquals(listOf("db.driver"), missing.map { it.key })
        assertEquals(listOf("com.mysql.Driver"), missing.map { it.value })
    }

    @Test
    fun `a project property missing on the remote is reported with its placeholders expanded`() {
        val chain = chain("db.host" to "localhost", "db.url" to "jdbc:\${db.host}")

        val missing = CxPropertyComparison.missingOnRemote(emptyList(), chain)

        assertEquals("jdbc:localhost", missing.find { it.key == "db.url" }?.value)
    }

    @Test
    fun `a property declared only outside the active configuration is not reported as missing on the remote`() {
        val chain = CxPropertyModel.of(
            listOf(unusedProject),
            listOf(CxPropertyDeclaration("db.url", "jdbc:unused", unusedProject)),
        )

        assertEquals(emptyList(), CxPropertyComparison.missingOnRemote(emptyList(), chain))
    }

    @Test
    fun `properties missing on the remote are ordered by key`() {
        val chain = chain("z.one" to "1", "a.two" to "2")

        assertEquals(listOf("a.two", "z.one"), CxPropertyComparison.missingOnRemote(emptyList(), chain).map { it.key })
    }

    private fun remote(key: String, value: String = "remote") = CxPropertyPresentation(key, value)

    private fun chain(vararg properties: Pair<String, String>) = CxPropertyModel.of(
        listOf(local),
        properties.map { (key, value) -> CxPropertyDeclaration(key, value, local) },
    )
}
