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

package sap.commerce.toolset.impex.codeInsight.daemon

import sap.commerce.toolset.typeSystem.TSConstants
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the pure value-resolution logic of [ImpExToFlexibleSearchLineMarkerProvider].
 *
 * The full [ImpExToFlexibleSearchLineMarkerProvider.toUniqueSelectQuery] pipeline is PSI- and
 * type-system-bound and cannot be exercised without a loaded SAP Commerce project. These tests
 * cover the two pieces that decide the WHERE-clause value of a nested leaf parameter:
 * [ImpExToFlexibleSearchLineMarkerProvider.resolveLeafValue] and
 * [ImpExToFlexibleSearchLineMarkerProvider.formatPredicate].
 */
class ImpExToFlexibleSearchLineMarkerProviderTest {

    private val provider = ImpExToFlexibleSearchLineMarkerProvider()

    // -------------------------------------------------------------------------
    // resolveLeafValue — positional value wins
    // -------------------------------------------------------------------------

    @Test
    fun resolveLeafValue_positionalPresent_usedVerbatim() {
        assertEquals("26002000", provider.resolveLeafValue(positional = "26002000", default = ""))
    }

    @Test
    fun resolveLeafValue_positionalPresent_defaultIgnored() {
        // A positional value always takes precedence, even when a default modifier exists.
        assertEquals("26002000", provider.resolveLeafValue(positional = "26002000", default = "'Staged'"))
    }

    @Test
    fun resolveLeafValue_positionalEmptySegment_fallsBackToDefault() {
        // `26002000::Staged` yields an empty middle segment — in ImpEx an empty value
        // triggers the [default=...] modifier.
        assertEquals("Staged", provider.resolveLeafValue(positional = "", default = "'Staged'"))
    }

    @Test
    fun resolveLeafValue_positionalBlankSegment_fallsBackToDefault() {
        // Whitespace-only segments (`26002000 :  : Staged`) are empty values too.
        assertEquals("Staged", provider.resolveLeafValue(positional = "  ", default = "'Staged'"))
    }

    @Test
    fun resolveLeafValue_positionalBlankSegment_noDefault_fallsBackToSentinel() {
        assertEquals("?", provider.resolveLeafValue(positional = "", default = ""))
    }

    // -------------------------------------------------------------------------
    // resolveLeafValue — falls back to default modifier (the reported bug)
    // -------------------------------------------------------------------------

    @Test
    fun resolveLeafValue_noPositional_macroResolvedDefaultUsed() {
        // `id[default=$productCatalog]` — the macro is already expanded upstream to `mcProductCatalog`.
        assertEquals("mcProductCatalog", provider.resolveLeafValue(positional = null, default = "mcProductCatalog"))
    }

    @Test
    fun resolveLeafValue_noPositional_singleQuotedDefaultUnwrapped() {
        // `version[default='Staged']` — surrounding single quotes must be stripped.
        assertEquals("Staged", provider.resolveLeafValue(positional = null, default = "'Staged'"))
    }

    @Test
    fun resolveLeafValue_noPositional_emptyDefault_fallsBackToSentinel() {
        assertEquals("?", provider.resolveLeafValue(positional = null, default = ""))
    }

    // -------------------------------------------------------------------------
    // resolveLeafValue + formatPredicate — the end-to-end WHERE predicate
    // -------------------------------------------------------------------------

    @Test
    fun predicate_nestedDefault_macro_producesQuotedNaturalKey() {
        val value = provider.resolveLeafValue(positional = null, default = "mcProductCatalog")
        assertEquals("= 'mcProductCatalog'", provider.formatPredicate(value, TSConstants.Type.JAVA_STRING))
    }

    @Test
    fun predicate_nestedDefault_singleQuoted_notDoubleQuoted() {
        // Regression: without unwrapping this produced `= '''Staged'''`.
        val value = provider.resolveLeafValue(positional = null, default = "'Staged'")
        assertEquals("= 'Staged'", provider.formatPredicate(value, TSConstants.Type.JAVA_STRING))
    }

    @Test
    fun predicate_noPositionalNoDefault_producesSentinelPredicate() {
        val value = provider.resolveLeafValue(positional = null, default = "")
        assertEquals("= '?'", provider.formatPredicate(value, TSConstants.Type.JAVA_STRING))
    }

    // -------------------------------------------------------------------------
    // formatPredicate — type-aware quoting
    // -------------------------------------------------------------------------

    @Test
    fun formatPredicate_stringType_isQuotedAndEscaped() {
        assertEquals("= 'O''Brien'", provider.formatPredicate("O'Brien", TSConstants.Type.JAVA_STRING))
    }

    @Test
    fun formatPredicate_stringType_stripsSurroundingDoubleQuotes() {
        assertEquals("= 'quoted'", provider.formatPredicate("\"quoted\"", TSConstants.Type.JAVA_STRING))
    }

    @Test
    fun formatPredicate_nullType_defaultsToQuotedString() {
        assertEquals("= 'mcProductCatalog'", provider.formatPredicate("mcProductCatalog", null))
    }

    @Test
    fun formatPredicate_booleanType_trueRenderedAsOne() {
        assertEquals("= 1", provider.formatPredicate("true", TSConstants.Primitive.BOOLEAN))
    }

    @Test
    fun formatPredicate_booleanType_falseRenderedAsZero() {
        assertEquals("= 0", provider.formatPredicate("false", TSConstants.Primitive.BOOLEAN))
    }

    @Test
    fun formatPredicate_numericLikeType_leftUnquoted() {
        // Any non-String, non-Boolean type (numeric/enum/date) is used as-is, unquoted.
        assertEquals("= 42", provider.formatPredicate("42", "java.lang.Integer"))
    }
}
