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

import com.intellij.testFramework.LightVirtualFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CxPropertyWriteTargetsTest {

    @Test
    fun `local properties come before the custom extensions`() {
        val targets = targetsOf(project("zeta"), local(), project("alpha"))

        assertEquals(CxPropertyScope.LOCAL, targets.first().scope)
    }

    @Test
    fun `custom extensions are sorted by name`() {
        val targets = targetsOf(project("zeta"), project("alpha"))

        assertEquals(listOf("alpha", "zeta"), targets.map { it.extension })
    }

    @Test
    fun `an extension which is not custom is no target`() {
        val targets = targetsOf(project("core"), customExtensions = emptySet())

        assertTrue(targets.isEmpty())
    }

    @Test
    fun `a source without a file is no target`() {
        val targets = targetsOf(CxPropertySource("local.properties", CxPropertyScope.LOCAL))

        assertTrue(targets.isEmpty())
    }

    @Test
    fun `a file listed twice is one target`() {
        val local = local()

        assertEquals(1, targetsOf(local, local).size)
    }

    private fun targetsOf(vararg sources: CxPropertySource, customExtensions: Set<String> = setOf("alpha", "zeta")) =
        CxPropertyWriteService.targetsOf(sources.toList(), customExtensions)

    private fun local() = CxPropertySource("local.properties", CxPropertyScope.LOCAL, file = LOCAL_FILE)

    private fun project(extension: String) = CxPropertySource(
        name = "project.properties",
        scope = CxPropertyScope.PROJECT,
        extension = extension,
        file = LightVirtualFile("project.properties"),
    )

    companion object {
        private val LOCAL_FILE = LightVirtualFile("local.properties")
    }
}
