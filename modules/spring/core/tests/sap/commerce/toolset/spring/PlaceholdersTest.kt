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

package sap.commerce.toolset.spring

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholdersTest {

    private val properties = Properties().apply { setProperty("security.config", "custom/security-spring.xml") }

    @Test
    fun `placeholder is replaced with the defined property value`() {
        assertEquals("custom/security-spring.xml", "\${security.config}".resolvePlaceholders(properties))
    }

    @Test
    fun `defined property wins over the default value of the placeholder`() {
        assertEquals("custom/security-spring.xml", "\${security.config:config\\security-spring.xml}".resolvePlaceholders(properties))
    }

    @Test
    fun `placeholder falls back to its default value when the property is not defined`() {
        assertEquals("config\\security-spring.xml", "\${unknown.config:config\\security-spring.xml}".resolvePlaceholders(properties))
    }

    @Test
    fun `placeholder without a default stays untouched when the property is not defined`() {
        assertEquals("\${unknown.config}", "\${unknown.config}".resolvePlaceholders(properties))
    }

    @Test
    fun `placeholder with an empty default is replaced with an empty value`() {
        assertEquals("", "\${unknown.config:}".resolvePlaceholders(properties))
    }

    @Test
    fun `text around placeholders is preserved`() {
        assertEquals("a/custom/security-spring.xml/b.xml", "a/\${security.config}/b.xml".resolvePlaceholders(properties))
    }

    @Test
    fun `value without placeholders is returned as is`() {
        assertEquals("classpath:/my-spring.xml", "classpath:/my-spring.xml".resolvePlaceholders(properties))
    }
}
