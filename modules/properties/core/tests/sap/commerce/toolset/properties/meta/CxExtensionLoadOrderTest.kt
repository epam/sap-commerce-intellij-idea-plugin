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
import kotlin.test.assertTrue

/**
 * Unit tests for the extension load order backing the precedence of the `project.properties` files.
 */
class CxExtensionLoadOrderTest {

    @Test
    fun `an extension is loaded after the extensions it requires`() {
        val order = CxExtensionLoadOrder.of(
            linkedMapOf(
                "customstorefront" to listOf("commerceservices"),
                "commerceservices" to emptyList(),
            )
        )

        assertEquals(listOf("commerceservices", "customstorefront"), order)
    }

    @Test
    fun `a transitively required extension is loaded first`() {
        val order = CxExtensionLoadOrder.of(
            linkedMapOf(
                "customstorefront" to listOf("commerceservices"),
                "commerceservices" to listOf("core"),
                "core" to emptyList(),
            )
        )

        assertEquals(listOf("core", "commerceservices", "customstorefront"), order)
    }

    @Test
    fun `requirements are honoured in their declaration order`() {
        val order = CxExtensionLoadOrder.of(
            linkedMapOf(
                "customstorefront" to listOf("commerceservices", "basecommerce"),
                "commerceservices" to emptyList(),
                "basecommerce" to emptyList(),
            )
        )

        assertEquals(listOf("commerceservices", "basecommerce", "customstorefront"), order)
    }

    @Test
    fun `a requirement cycle does not loop`() {
        val order = CxExtensionLoadOrder.of(
            linkedMapOf(
                "one" to listOf("two"),
                "two" to listOf("one"),
            )
        )

        assertEquals(setOf("one", "two"), order.toSet())
    }

    @Test
    fun `a requirement which is not part of the project is skipped`() {
        val order = CxExtensionLoadOrder.of(
            linkedMapOf(
                "customstorefront" to listOf("notconfigured"),
            )
        )

        assertEquals(listOf("customstorefront"), order)
    }

    @Test
    fun `every extension of the project is ordered exactly once`() {
        val order = CxExtensionLoadOrder.of(
            linkedMapOf(
                "one" to listOf("shared"),
                "two" to listOf("shared"),
                "shared" to emptyList(),
            )
        )

        assertEquals(listOf("shared", "one", "two"), order)
        assertTrue(order.distinct() == order)
    }

    @Test
    fun `ranks number the extensions in load order`() {
        val ranks = CxExtensionLoadOrder.ranks(
            linkedMapOf(
                "customstorefront" to listOf("commerceservices"),
                "commerceservices" to emptyList(),
            )
        )

        assertEquals(mapOf("commerceservices" to 0, "customstorefront" to 1), ranks)
    }
}
