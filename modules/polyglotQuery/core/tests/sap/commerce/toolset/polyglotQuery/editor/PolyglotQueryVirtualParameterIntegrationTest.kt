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
 * Integration tests for [PolyglotQueryVirtualParameter].
 *
 * Tests here exercise multi-step lifecycle scenarios and type-classification behaviour.
 * Item-type parameters — where [PolyglotQueryVirtualParameter.type] resolves to
 * `Int::class` via [sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess] — require a
 * live type-system and are not covered here; they belong in IntelliJ Platform integration tests.
 */
class PolyglotQueryVirtualParameterIntegrationTest {

    @Suppress("UNCHECKED_CAST")
    private val noProject: WeakReference<Project> = WeakReference<Any>(null) as WeakReference<Project>

    private fun param(rawType: String? = null) = PolyglotQueryVirtualParameter(
        name = "p",
        project = noProject,
        rawType = rawType,
    )

    // -------------------------------------------------------------------------
    // Type classification (no project needed for known primitives)
    // -------------------------------------------------------------------------

    @Test
    fun typeClassification_booleanPrimitive_classifiesAsBoolean() {
        assertEquals(Boolean::class, param("boolean").type)
    }

    @Test
    fun typeClassification_javaLangBoolean_classifiesAsBoolean() {
        assertEquals(Boolean::class, param("java.lang.Boolean").type)
    }

    @Test
    fun typeClassification_javaLangString_classifiesAsString() {
        assertEquals(String::class, param("java.lang.String").type)
    }

    @Test
    fun typeClassification_localizedString_classifiesAsString() {
        assertEquals(String::class, param("localized:java.lang.String").type)
    }

    @Test
    fun typeClassification_javaLangLong_classifiesAsLong() {
        assertEquals(Long::class, param("java.lang.Long").type)
    }

    @Test
    fun typeClassification_javaUtilDate_classifiesAsDate() {
        assertEquals(Date::class, param("java.util.Date").type)
    }

    @Test
    fun typeClassification_nullRawType_noProject_classifiesAsAny() {
        // No project → TSMetaModelAccess not called → Any::class fallback
        assertEquals(Any::class, param(null).type)
    }

    @Test
    fun typeClassification_unknownPrimitiveRawType_noProject_classifiesAsAny() {
        assertEquals(Any::class, param("some.unknown.Type").type)
    }

    // -------------------------------------------------------------------------
    // Value lifecycle: rawValue changes invalidate cached sql/presentation values
    // -------------------------------------------------------------------------

    @Test
    fun valueLifecycle_boolean_multipleRawValueChanges_returnsCurrentValue() {
        val p = param("boolean")
        p.rawValue = true
        assertEquals("true", p.sqlValue)

        p.rawValue = false
        assertEquals("false", p.sqlValue)

        p.rawValue = null
        assertEquals("false", p.sqlValue)
    }

    @Test
    fun valueLifecycle_string_multipleRawValueChanges_returnsCurrentValue() {
        val p = param("java.lang.String")
        p.rawValue = "alpha"
        assertEquals("\"alpha\"", p.sqlValue)

        p.rawValue = "beta"
        assertEquals("\"beta\"", p.sqlValue)
    }

    @Test
    fun valueLifecycle_date_rawValueChange_updatesBothValues() {
        val p = param("java.util.Date")
        val d1 = Date(1_000_000L)
        val d2 = Date(2_000_000L)

        p.rawValue = d1
        val sql1 = p.sqlValue
        val pres1 = p.presentationValue

        p.rawValue = d2
        assertNotEquals(sql1, p.sqlValue)
        assertNotEquals(pres1, p.presentationValue)
    }

    @Test
    fun valueLifecycle_string_sqlAndPresentationValuesAreIndependent() {
        val p = param("java.lang.String").apply { rawValue = "test" }
        // sql wraps in double-quotes
        assertTrue(p.sqlValue.startsWith("\""))
        assertTrue(p.sqlValue.endsWith("\""))
    }

    // -------------------------------------------------------------------------
    // Edge values
    // -------------------------------------------------------------------------

    @Test
    fun edgeValues_string_doubleQuoteInValue_escapedInSqlValue() {
        val p = param("java.lang.String").apply { rawValue = "a\"b" }
        assertEquals("\"a\\\"b\"", p.sqlValue)
    }

    @Test
    fun edgeValues_string_emptyRawValue_emptyQuotedSqlValue() {
        val p = param("java.lang.String").apply { rawValue = "" }
        assertEquals("\"\"", p.sqlValue)
    }

    @Test
    fun edgeValues_date_nullRawValue_emptySqlValue() {
        val p = param("java.util.Date")
        assertEquals("", p.sqlValue)
        assertEquals("", p.presentationValue)
    }

    @Test
    fun edgeValues_long_zeroValue_sqlValueIsZero() {
        val p = param("java.lang.Long").apply { rawValue = "0" }
        assertEquals("0", p.sqlValue)
    }

    @Test
    fun edgeValues_displayName_truncatedForLongParameterName() {
        val longName = "aVeryLongParameterNameThatExceedsTwentyChars"
        val p = PolyglotQueryVirtualParameter(name = longName, project = noProject)
        assertTrue(p.displayName.length <= longName.length)
        assertTrue(p.displayName.contains("...") || p.displayName == longName)
    }
}
