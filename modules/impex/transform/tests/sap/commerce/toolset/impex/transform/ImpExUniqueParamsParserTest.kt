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

package sap.commerce.toolset.impex.transform

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the PSI-free helper functions in [ImpExUniqueParamsParser].
 *
 * [ImpExUniqueParamsParser.resolveLeafValue] and [ImpExUniqueParamsParser.formatPredicate]
 * are pure functions — no IntelliJ platform or type-system services required.
 */
class ImpExUniqueParamsParserTest {

    // -------------------------------------------------------------------------
    // resolveLeafValue
    // -------------------------------------------------------------------------

    @Test
    fun resolveLeafValue_nonBlankPositional_returnsPositional() {
        assertEquals("myValue", ImpExUniqueParamsParser.resolveLeafValue("myValue", "default"))
    }

    @Test
    fun resolveLeafValue_blankPositional_fallsBackToDefault() {
        assertEquals("default", ImpExUniqueParamsParser.resolveLeafValue("  ", "default"))
    }

    @Test
    fun resolveLeafValue_nullPositional_fallsBackToDefault() {
        assertEquals("default", ImpExUniqueParamsParser.resolveLeafValue(null, "default"))
    }

    @Test
    fun resolveLeafValue_nullPositional_singleQuotedDefault_stripsQuotes() {
        assertEquals("Staged", ImpExUniqueParamsParser.resolveLeafValue(null, "'Staged'"))
    }

    @Test
    fun resolveLeafValue_nullPositional_emptyDefault_returnsSentinel() {
        assertEquals("?", ImpExUniqueParamsParser.resolveLeafValue(null, ""))
    }

    @Test
    fun resolveLeafValue_emptyPositional_emptyDefault_returnsSentinel() {
        assertEquals("?", ImpExUniqueParamsParser.resolveLeafValue("", ""))
    }

    @Test
    fun resolveLeafValue_blankPositional_emptyDefault_returnsSentinel() {
        assertEquals("?", ImpExUniqueParamsParser.resolveLeafValue("   ", ""))
    }

    // -------------------------------------------------------------------------
    // formatPredicate
    // -------------------------------------------------------------------------

    @Test
    fun formatPredicate_booleanPrimitive_trueLiteral() {
        assertEquals("= 1", ImpExUniqueParamsParser.formatPredicate("true", "boolean"))
    }

    @Test
    fun formatPredicate_booleanPrimitive_trueUpperCase() {
        assertEquals("= 1", ImpExUniqueParamsParser.formatPredicate("TRUE", "boolean"))
    }

    @Test
    fun formatPredicate_booleanPrimitive_numericOne() {
        assertEquals("= 1", ImpExUniqueParamsParser.formatPredicate("1", "boolean"))
    }

    @Test
    fun formatPredicate_booleanPrimitive_falseLiteral() {
        assertEquals("= 0", ImpExUniqueParamsParser.formatPredicate("false", "boolean"))
    }

    @Test
    fun formatPredicate_booleanPrimitive_numericZero() {
        assertEquals("= 0", ImpExUniqueParamsParser.formatPredicate("0", "boolean"))
    }

    @Test
    fun formatPredicate_javaLangBoolean_trueLiteral() {
        assertEquals("= 1", ImpExUniqueParamsParser.formatPredicate("true", "java.lang.Boolean"))
    }

    @Test
    fun formatPredicate_javaLangBoolean_falseLiteral() {
        assertEquals("= 0", ImpExUniqueParamsParser.formatPredicate("false", "java.lang.Boolean"))
    }

    @Test
    fun formatPredicate_javaLangString_quotesValue() {
        assertEquals("= 'hello'", ImpExUniqueParamsParser.formatPredicate("hello", "java.lang.String"))
    }

    @Test
    fun formatPredicate_javaLangString_escapesApostrophe() {
        assertEquals("= 'it''s'", ImpExUniqueParamsParser.formatPredicate("it's", "java.lang.String"))
    }

    @Test
    fun formatPredicate_javaLangString_emptyValue() {
        assertEquals("= ''", ImpExUniqueParamsParser.formatPredicate("", "java.lang.String"))
    }

    @Test
    fun formatPredicate_nullType_treatedAsString() {
        assertEquals("= 'fallback'", ImpExUniqueParamsParser.formatPredicate("fallback", null))
    }

    @Test
    fun formatPredicate_nullType_escapesApostrophe() {
        assertEquals("= 'can''t'", ImpExUniqueParamsParser.formatPredicate("can't", null))
    }

    @Test
    fun formatPredicate_numericType_unquoted() {
        assertEquals("= 42", ImpExUniqueParamsParser.formatPredicate("42", "java.lang.Long"))
    }

    @Test
    fun formatPredicate_otherType_unquoted() {
        assertEquals("= APPROVED", ImpExUniqueParamsParser.formatPredicate("APPROVED", "ArticleApprovalStatus"))
    }

    @Test
    fun formatPredicate_valueWithSurroundingDoubleQuotes_strippedBeforeFormatting() {
        assertEquals("= 'code'", ImpExUniqueParamsParser.formatPredicate("\"code\"", "java.lang.String"))
    }

    @Test
    fun formatPredicate_valueWithLeadingTrailingWhitespace_trimmed() {
        assertEquals("= 'trimmed'", ImpExUniqueParamsParser.formatPredicate("  trimmed  ", "java.lang.String"))
    }
}
