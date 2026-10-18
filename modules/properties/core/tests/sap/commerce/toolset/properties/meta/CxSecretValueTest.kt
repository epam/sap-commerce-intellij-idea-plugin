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

class CxSecretValueTest {

    @Test
    fun `a value which is actually there is a secret`() {
        assertTrue(CxSecretValue.isSecret("hunter2"))
    }

    @Test
    fun `an empty value only documents that something is needed`() {
        assertFalse(CxSecretValue.isSecret(""))
    }

    @Test
    fun `a blank value only documents that something is needed`() {
        assertFalse(CxSecretValue.isSecret("   "))
    }

    @Test
    fun `a value which is entirely a placeholder has already been externalised`() {
        assertFalse(CxSecretValue.isSecret("\${SMTP_PASSWORD}"))
    }

    @Test
    fun `a placeholder surrounded by whitespace has already been externalised`() {
        assertFalse(CxSecretValue.isSecret("  \${SMTP_PASSWORD}  "))
    }

    @Test
    fun `a placeholder which is only part of the value leaves the rest written down`() {
        assertTrue(CxSecretValue.isSecret("\${prefix}-actual-secret"))
    }

    @Test
    fun `changeme is a stand-in whatever its case`() {
        assertFalse(CxSecretValue.isSecret("CHANGEME"))
    }

    @Test
    fun `a todo is a stand-in`() {
        assertFalse(CxSecretValue.isSecret("TODO"))
    }

    @Test
    fun `a bracketed hint is a stand-in`() {
        assertFalse(CxSecretValue.isSecret("<your api key here>"))
    }
}
