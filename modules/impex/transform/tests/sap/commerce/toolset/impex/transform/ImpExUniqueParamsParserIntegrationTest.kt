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
 * Integration tests for [ImpExUniqueParamsParser.formatPredicate] and
 * [ImpExUniqueParamsParser.resolveLeafValue].
 *
 * Each test exercises a realistic SAP Commerce attribute-type scenario: the functions
 * are composed in the same way the parser does — resolve the leaf value first, then format the
 * predicate — so these tests validate the complete formatting pipeline rather than individual
 * edge cases (which are covered by [ImpExUniqueParamsParserTest]).
 */
class ImpExUniqueParamsParserIntegrationTest {

    // -------------------------------------------------------------------------
    // Product.code — flat String unique key
    // -------------------------------------------------------------------------

    @Test
    fun productCode_presentValue_producesQuotedStringPredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue("myProduct", "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "java.lang.String")
        assertEquals("= 'myProduct'", predicate)
    }

    @Test
    fun productCode_emptyCell_noDefault_producesSentinelPredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue(null, "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "java.lang.String")
        assertEquals("= '?'", predicate)
    }

    @Test
    fun productCode_emptyCell_withDefault_usesDefault() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue(null, "'defaultCode'")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "java.lang.String")
        assertEquals("= 'defaultCode'", predicate)
    }

    @Test
    fun productCode_valueContainsApostrophe_escapedInPredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue("O'Reilly", "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "java.lang.String")
        assertEquals("= 'O''Reilly'", predicate)
    }

    // -------------------------------------------------------------------------
    // CatalogVersion — nested path catalog(id):version split by ":"
    // -------------------------------------------------------------------------

    /**
     * Cell value `"productCatalog:Staged"` split by `:` gives two positional segments.
     * First leaf (`id` of `catalog`) gets `"productCatalog"`, second (`version`) gets `"Staged"`.
     */
    @Test
    fun catalogVersion_catalogId_firstSegment_producesQuotedStringPredicate() {
        val segments = "productCatalog:Staged".split(":").toMutableList()
        val catalogIdValue = ImpExUniqueParamsParser.resolveLeafValue(segments.removeFirst(), "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(catalogIdValue, "java.lang.String")
        assertEquals("= 'productCatalog'", predicate)
    }

    @Test
    fun catalogVersion_version_secondSegment_producesQuotedStringPredicate() {
        val segments = "productCatalog:Staged".split(":").toMutableList()
        segments.removeFirst() // consume catalog id
        val versionValue = ImpExUniqueParamsParser.resolveLeafValue(segments.removeFirst(), "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(versionValue, "java.lang.String")
        assertEquals("= 'Staged'", predicate)
    }

    @Test
    fun catalogVersion_missingSecondSegment_fallsBackToStagedDefault() {
        // Cell contains only "productCatalog" — no version segment
        val segments = "productCatalog".split(":").toMutableList()
        segments.removeFirst() // consume catalog id; list now empty
        val versionValue = ImpExUniqueParamsParser.resolveLeafValue(segments.removeFirstOrNull(), "'Staged'")
        val predicate = ImpExUniqueParamsParser.formatPredicate(versionValue, "java.lang.String")
        assertEquals("= 'Staged'", predicate)
    }

    @Test
    fun catalogVersion_emptyCell_noSegments_noDefault_returnsSentinel() {
        val versionValue = ImpExUniqueParamsParser.resolveLeafValue(null, "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(versionValue, "java.lang.String")
        assertEquals("= '?'", predicate)
    }

    // -------------------------------------------------------------------------
    // Boolean attribute (e.g. Product.active)
    // -------------------------------------------------------------------------

    @Test
    fun booleanAttribute_trueValue_producesNumericOnePredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue("true", "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "boolean")
        assertEquals("= 1", predicate)
    }

    @Test
    fun booleanAttribute_falseValue_producesNumericZeroPredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue("false", "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "boolean")
        assertEquals("= 0", predicate)
    }

    @Test
    fun booleanAttribute_emptyCell_defaultTrue_producesNumericOnePredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue(null, "true")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "boolean")
        assertEquals("= 1", predicate)
    }

    // -------------------------------------------------------------------------
    // Enum / numeric FK (e.g. ArticleApprovalStatus, PK)
    // -------------------------------------------------------------------------

    @Test
    fun enumAndNumeric_approvalStatus_enumCode_unquotedPredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue("APPROVED", "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "ArticleApprovalStatus")
        assertEquals("= APPROVED", predicate)
    }

    @Test
    fun enumAndNumeric_longFk_numericValue_unquotedPredicate() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue("8796093054978", "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, "java.lang.Long")
        assertEquals("= 8796093054978", predicate)
    }

    @Test
    fun enumAndNumeric_unknownType_null_treatedAsString() {
        val leafValue = ImpExUniqueParamsParser.resolveLeafValue("someValue", "")
        val predicate = ImpExUniqueParamsParser.formatPredicate(leafValue, null)
        assertEquals("= 'someValue'", predicate)
    }
}
