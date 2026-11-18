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

package sap.commerce.toolset.ccv2.unscramble

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CCv2UnscrambleServiceTest {

    private val service = CCv2UnscrambleService()

    private val log = """
        {
          "message": "failed",
          "thrown": {
            "name": "java.lang.IllegalStateException",
            "message": "boom",
            "extendedStackTrace": [
              { "class": "a.B", "method": "run", "file": "B.java", "line": 12 }
            ],
            "cause": {
              "name": "java.io.IOException",
              "message": "disk",
              "extendedStackTrace": [
                { "class": "c.D", "method": "read", "file": "D.java", "line": 3 }
              ]
            }
          }
        }
    """.trimIndent()

    @Test
    fun `log with a thrown stack trace can be handled`() {
        assertTrue(service.canHandle(log))
    }

    @Test
    fun `plain text cannot be handled`() {
        assertFalse(service.canHandle("java.lang.Exception: boom"))
    }

    @Test
    fun `json number literal cannot be handled`() {
        assertFalse(service.canHandle("123"))
    }

    @Test
    fun `json string literal cannot be handled`() {
        assertFalse(service.canHandle("\"thrown\""))
    }

    @Test
    fun `json array cannot be handled`() {
        assertFalse(service.canHandle("[1, 2]"))
    }

    @Test
    fun `json object without thrown cannot be handled`() {
        assertFalse(service.canHandle("""{ "message": "failed" }"""))
    }

    @Test
    fun `json object with null thrown cannot be handled`() {
        assertFalse(service.canHandle("""{ "thrown": null }"""))
    }

    @Test
    fun `json object with non object thrown cannot be handled`() {
        assertFalse(service.canHandle("""{ "thrown": "boom" }"""))
    }

    @Test
    fun `thrown without a stack trace cannot be handled`() {
        assertFalse(service.canHandle("""{ "thrown": { "name": "java.lang.Exception" } }"""))
    }

    @Test
    fun `stack trace is not built for a json literal`() {
        assertNull(service.buildStackTraceString("123"))
    }

    @Test
    fun `stack trace includes the cause chain`() {
        val expected = """
            java.lang.IllegalStateException: boom
            ${'\t'}at a.B.run(B.java:12)
            Caused by: java.io.IOException: disk
            ${'\t'}at c.D.read(D.java:3)

        """.trimIndent()

        assertEquals(expected, service.buildStackTraceString(log))
    }

    @Test
    fun `stack trace is built when the cause is null`() {
        val text = """{ "thrown": { "name": "java.lang.Exception", "message": "boom", "extendedStackTrace": [], "cause": null } }"""

        assertEquals("java.lang.Exception: boom\n", service.buildStackTraceString(text))
    }
}
