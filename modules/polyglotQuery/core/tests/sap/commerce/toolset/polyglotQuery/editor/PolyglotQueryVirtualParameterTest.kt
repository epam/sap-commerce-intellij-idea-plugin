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

package sap.commerce.toolset.polyglotQuery.editor

import com.intellij.openapi.project.Project
import java.lang.ref.WeakReference
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [PolyglotQueryVirtualParameter.sqlValue] and [PolyglotQueryVirtualParameter.presentationValue].
 *
 * All tests use a null project reference — sufficient for primitive rawTypes because the type
 * resolution `when` matches before the `else` branch that would call TSMetaModelAccess.
 * Item-type parameters (rawType = SAP Commerce type name) require a live type system and
 * are therefore covered by integration tests instead.
 */
class PolyglotQueryVirtualParameterTest {

    @Suppress("UNCHECKED_CAST")
    private val noProject: WeakReference<Project> = WeakReference<Any>(null) as WeakReference<Project>

    private fun param(rawType: String? = null) = PolyglotQueryVirtualParameter(
        name = "p",
        project = noProject,
        rawType = rawType,
    )

    // -------------------------------------------------------------------------
    // Boolean
    // -------------------------------------------------------------------------

    @Test
    fun sqlValue_boolean_true() {
        val p = param("boolean").apply { rawValue = true }
        assertEquals("true", p.sqlValue)
    }

    @Test
    fun sqlValue_boolean_false() {
        val p = param("boolean").apply { rawValue = false }
        assertEquals("false", p.sqlValue)
    }

    @Test
    fun sqlValue_boolean_nullRawValue_defaultsFalse() {
        val p = param("boolean")
        assertEquals("false", p.sqlValue)
    }

    @Test
    fun presentationValue_boolean_delegatesToSqlValue() {
        val p = param("boolean").apply { rawValue = true }
        assertEquals(p.sqlValue, p.presentationValue)
    }

    @Test
    fun sqlValue_javaLangBoolean_true() {
        val p = param("java.lang.Boolean").apply { rawValue = true }
        assertEquals("true", p.sqlValue)
    }

    // -------------------------------------------------------------------------
    // String
    // -------------------------------------------------------------------------

    @Test
    fun sqlValue_string_quotesValue() {
        val p = param("java.lang.String").apply { rawValue = "hello" }
        assertEquals("\"hello\"", p.sqlValue)
    }

    @Test
    fun sqlValue_string_escapesInnerDoubleQuotes() {
        val p = param("java.lang.String").apply { rawValue = "say \"hi\"" }
        assertEquals("\"say \\\"hi\\\"\"", p.sqlValue)
    }

    @Test
    fun sqlValue_string_nullRawValue_emptyQuotedString() {
        val p = param("java.lang.String")
        assertEquals("\"\"", p.sqlValue)
    }

    @Test
    fun sqlValue_localizedString_quotesValue() {
        val p = param("localized:java.lang.String").apply { rawValue = "local" }
        assertEquals("\"local\"", p.sqlValue)
    }

    // -------------------------------------------------------------------------
    // Numeric primitives (else branch — null project causes no meta lookup)
    // -------------------------------------------------------------------------

    @Test
    fun sqlValue_long_rawStringPassthrough() {
        val p = param("java.lang.Long").apply { rawValue = "12345" }
        assertEquals("12345", p.sqlValue)
    }

    @Test
    fun sqlValue_int_rawStringPassthrough() {
        val p = param("java.lang.Integer").apply { rawValue = "7" }
        assertEquals("7", p.sqlValue)
    }

    @Test
    fun presentationValue_long_sameAsSqlValue() {
        val p = param("java.lang.Long").apply { rawValue = "12345" }
        assertEquals(p.sqlValue, p.presentationValue)
    }

    // -------------------------------------------------------------------------
    // Date
    // -------------------------------------------------------------------------

    @Test
    fun sqlValue_date_wrapsAsNewJavaDate() {
        val millis = 1_700_000_000_000L
        val p = param("java.util.Date").apply { rawValue = Date(millis) }
        assertEquals("new java.util.Date($millis)", p.sqlValue)
    }

    @Test
    fun sqlValue_date_nullRawValue_emptyString() {
        val p = param("java.util.Date")
        assertEquals("", p.sqlValue)
    }

    @Test
    fun presentationValue_date_formattedDifferentlyFromSqlValue() {
        val p = param("java.util.Date").apply { rawValue = Date(1_700_000_000_000L) }
        assertTrue(p.presentationValue.isNotEmpty())
        assertNotEquals(p.sqlValue, p.presentationValue)
    }

    @Test
    fun presentationValue_date_matchesDateFormat() {
        val p = param("java.util.Date").apply { rawValue = Date(0L) }
        // Format: yyyy-MM-dd HH:mm:ss.SSS — verify rough shape
        assertTrue(p.presentationValue.matches(Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}""")))
    }

    // -------------------------------------------------------------------------
    // rawValue mutation invalidates cached values
    // -------------------------------------------------------------------------

    @Test
    fun sqlValue_updatesWhenRawValueChanges() {
        val p = param("boolean").apply { rawValue = true }
        assertEquals("true", p.sqlValue)
        p.rawValue = false
        assertEquals("false", p.sqlValue)
    }

    @Test
    fun sqlValue_string_updatesWhenRawValueChanges() {
        val p = param("java.lang.String").apply { rawValue = "first" }
        assertEquals("\"first\"", p.sqlValue)
        p.rawValue = "second"
        assertEquals("\"second\"", p.sqlValue)
    }
}
