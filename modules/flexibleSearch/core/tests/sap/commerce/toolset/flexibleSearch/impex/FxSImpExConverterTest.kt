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

package sap.commerce.toolset.flexibleSearch.impex

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [FxSImpExConverter.buildImpEx] covering the full ImpEx text generation pipeline.
 *
 * Each test constructs plain [FxSQueryInfo] / [FxSImpExParam] data-class instances — no PSI or
 * IntelliJ services required.
 */
class FxSImpExConverterTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun atomicParam(name: String, unique: Boolean = false, type: String = "java.lang.String") =
        FxSImpExParam(
            attributeName = name,
            attributeType = type,
            metaType = FxSAttributeMetaType.ATOMIC,
            modifiers = if (unique) listOf("unique=true") else emptyList(),
        )

    // -------------------------------------------------------------------------
    // Basic SELECT → INSERT_UPDATE
    // -------------------------------------------------------------------------

    /**
     * Simple query: SELECT {pk},{code},{name} FROM {Product} WHERE {code}='x'
     *
     * Expected:
     * ```
     * INSERT_UPDATE Product; code[unique=true]; name
     * ; "testProduct"; "Test Product"
     * ```
     */
    @Test
    fun buildImpEx_basicProduct_headerAndSingleRow() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            atomicParam("name"),
        )
        val rows = listOf(listOf("8796093054978", "testProduct", "Test Product"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]; name\n; \"testProduct\"; \"Test Product\"\n",
            result
        )
    }

    @Test
    fun buildImpEx_multipleRows() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(atomicParam("code", unique = true))
        val rows = listOf(
            listOf("pk1", "product-A"),
            listOf("pk2", "product-B"),
        )

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]\n; \"product-A\"\n; \"product-B\"\n",
            result
        )
    }

    @Test
    fun buildImpEx_pkOnlySelect_emptyValueRows() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true)),
            uniqueAttributeNames = emptySet(),
        )

        val result = FxSImpExConverter.buildImpEx("Product", emptyList(), emptyList(), queryInfo, listOf(listOf("pk1")))

        // Header only, data row has no fields
        assertEquals("INSERT_UPDATE Product\n\n", result)
    }

    // -------------------------------------------------------------------------
    // Header-only export (no rows)
    // -------------------------------------------------------------------------

    /**
     * Query not yet executed — no result rows, but columns are known from PSI.
     *
     * All SELECT columns must appear in the header even when [rows] is empty.
     *
     * Expected:
     * ```
     * INSERT_UPDATE Product; code[unique=true]; name
     * ```
     */
    @Test
    fun buildImpEx_noRows_allSelectColumnsInHeader() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            atomicParam("name"),
        )

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, emptyList())

        assertEquals("INSERT_UPDATE Product; code[unique=true]; name\n", result)
    }

    /**
     * No rows, with JOIN-unique synthetic columns — all SELECT columns plus join-unique must appear.
     *
     * Expected:
     * ```
     * INSERT_UPDATE Product; catalogVersion(catalog(id),version)[unique=true]; code; name
     * ```
     */
    @Test
    fun buildImpEx_noRows_withJoinUniqueColumns_allColumnsInHeader() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = emptySet(),
            joinUniqueColumns = listOf(
                FxSJoinUniqueColumn(
                    fkAttributeName = "catalogVersion",
                    naturalKeyAttr = "catalog(id),version",
                    constantValue = "productCatalog:Staged",
                )
            ),
        )
        val params = listOf(
            atomicParam("code"),
            atomicParam("name"),
        )
        val joinUniqueParams = listOf(
            FxSImpExParam(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            )
        )

        val result = FxSImpExConverter.buildImpEx("Product", params, joinUniqueParams, queryInfo, emptyList())

        assertEquals(
            "INSERT_UPDATE Product; catalogVersion(catalog(id),version)[unique=true]; code; name\n",
            result
        )
    }

    // -------------------------------------------------------------------------
    // NULL cell handling
    // -------------------------------------------------------------------------

    @Test
    fun buildImpEx_nullCellValue_renderedAsIgnore() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(atomicParam("name"))
        // HAC returns "null" string when the value is NULL
        val rows = listOf(listOf("pk1", "null"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        // "null" string → empty value → rendered as <ignore>
        assertEquals("INSERT_UPDATE Product; name\n; <ignore>\n", result)
    }

    // -------------------------------------------------------------------------
    // Localized attribute
    // -------------------------------------------------------------------------

    /**
     * Query with `{name[en]}` → header must include `name[lang=en]`.
     */
    @Test
    fun buildImpEx_localizedColumn() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false, isLocalized = true, langCode = "en"),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(
            FxSImpExParam(
                attributeName = "name",
                attributeType = "localizableString",
                metaType = FxSAttributeMetaType.ATOMIC,
                modifiers = listOf("lang=en"),
            )
        )
        val rows = listOf(listOf("pk1", "English Name"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals("INSERT_UPDATE Product; name[lang=en]\n; \"English Name\"\n", result)
    }

    // -------------------------------------------------------------------------
    // ComposedType FK reference
    // -------------------------------------------------------------------------

    /**
     * `catalogVersion` is a FK → CatalogVersion. Header: `catalogVersion(catalog(id),version)`.
     * Raw value from HAC for FK cells is the natural key compound, e.g. `Default:Staged`.
     */
    @Test
    fun buildImpEx_composedTypeFkColumn() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            FxSImpExParam(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                attributeType = "CatalogVersion",
                metaType = FxSAttributeMetaType.ITEM,
                modifiers = listOf("unique=true"),
            ),
        )
        val rows = listOf(listOf("pk1", "myProduct", "Default:Staged"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]; catalogVersion(catalog(id),version)[unique=true]\n" +
                "; \"myProduct\"; Default:Staged\n",
            result
        )
    }

    /**
     * Full-fidelity test for:
     * ```
     * SELECT {t.pk}, {t.sortableType}, {t.valueProviderParameter}, {t.type},
     *        {t.includeInResponse}, {t.ftsFuzzyQueryFuzziness}, {t.ftsWildcardQueryMinTermLength},
     *        {t.ftsQueryBoost}, {t.ftsWildcardQuery}, {t.rangeSet}, {t.customFacetSortProvider},
     *        {t.visible}, {t.ftsFuzzyQueryMinTermLength}, {t.sealed}, {t.priority},
     *        {t.categoryField}, {t.classAttributeAssignment}, {t.ftsQuery}, {t.ftsWildcardQueryType},
     *        {t.name}, {t.topValuesProvider}, {t.facetSort}, {t.ftsPhraseQuery}, {t.localized},
     *        {t.valueProviderParameters}, {t.ftsFuzzyQuery}, {t.useForAutocomplete},
     *        {t.ftsWildcardQueryBoost}, {t.useForSpellchecking}, {t.ftsPhraseQuerySlop},
     *        {t.fieldValueProvider}, {t.currency}, {t.ftsFuzzyQueryBoost}, {t.facetType},
     *        {t.facetDisplayNameProvider}, {t.ftsPhraseQueryBoost}, {t.multiValue},
     *        {t.useForHighlighting}, {t.exportId}, {t.facet}, {t.ftsQueryMinTermLength},
     *        {t.solrIndexedType}
     * FROM { SolrIndexedProperty AS t JOIN SolrIndexedType AS t0 ON {t0.pk} = {t.solrIndexedType} }
     * WHERE {t0.identifier} = 'mcProductType' AND {t.name} = 'feature-powersupply'
     * ```
     *
     * Key assertion: `{t.solrIndexedType}` is unique (resolved via JOIN t0 → t.solrIndexedType)
     * and `SolrIndexedType` has a composite type-system key `identifier,indexname`. However the
     * WHERE clause only constrains `t0.identifier`, so `joinNaturalKeyByAttr` maps
     * `"solrindexedtype" → "identifier"` and the header must emit
     * `solrIndexedType(identifier)[unique=true]` — NOT `(identifier,indexname)` — to avoid the
     * ImpEx import error "does not provide enough values".
     */
    @Test
    fun buildImpEx_solrIndexedPropertyQuery_joinFkUsesWhereNaturalKeyNotFullCompositeKey() {
        // FxSQueryAnalyzer.analyze() would produce this FxSQueryInfo for the query above:
        // - uniqueAttributeNames from WHERE: {name} (direct) + {solrIndexedType} (via t0 alias)
        // - joinNaturalKeyByAttr: {"solrindexedtype" → "identifier"} (from {t0.identifier} = '…')
        // - joinUniqueColumns: [] (solrIndexedType IS in SELECT → no synthetic column needed)
        val queryInfo = FxSQueryInfo(
            primaryType = "SolrIndexedProperty",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk",                       attributeName = "pk",                       isPk = true),
                FxSColumn(resultHeaderName = "sortableType",             attributeName = "sortableType",             isPk = false),
                FxSColumn(resultHeaderName = "valueProviderParameter",   attributeName = "valueProviderParameter",   isPk = false),
                FxSColumn(resultHeaderName = "type",                     attributeName = "type",                     isPk = false),
                FxSColumn(resultHeaderName = "includeInResponse",        attributeName = "includeInResponse",        isPk = false),
                FxSColumn(resultHeaderName = "ftsFuzzyQueryFuzziness",   attributeName = "ftsFuzzyQueryFuzziness",   isPk = false),
                FxSColumn(resultHeaderName = "ftsWildcardQueryMinTermLength", attributeName = "ftsWildcardQueryMinTermLength", isPk = false),
                FxSColumn(resultHeaderName = "ftsQueryBoost",            attributeName = "ftsQueryBoost",            isPk = false),
                FxSColumn(resultHeaderName = "ftsWildcardQuery",         attributeName = "ftsWildcardQuery",         isPk = false),
                FxSColumn(resultHeaderName = "rangeSet",                 attributeName = "rangeSet",                 isPk = false),
                FxSColumn(resultHeaderName = "customFacetSortProvider",  attributeName = "customFacetSortProvider",  isPk = false),
                FxSColumn(resultHeaderName = "visible",                  attributeName = "visible",                  isPk = false),
                FxSColumn(resultHeaderName = "ftsFuzzyQueryMinTermLength", attributeName = "ftsFuzzyQueryMinTermLength", isPk = false),
                FxSColumn(resultHeaderName = "sealed",                   attributeName = "sealed",                   isPk = false),
                FxSColumn(resultHeaderName = "priority",                 attributeName = "priority",                 isPk = false),
                FxSColumn(resultHeaderName = "categoryField",            attributeName = "categoryField",            isPk = false),
                FxSColumn(resultHeaderName = "classAttributeAssignment", attributeName = "classAttributeAssignment", isPk = false),
                FxSColumn(resultHeaderName = "ftsQuery",                 attributeName = "ftsQuery",                 isPk = false),
                FxSColumn(resultHeaderName = "ftsWildcardQueryType",     attributeName = "ftsWildcardQueryType",     isPk = false),
                FxSColumn(resultHeaderName = "name",                     attributeName = "name",                     isPk = false),
                FxSColumn(resultHeaderName = "topValuesProvider",        attributeName = "topValuesProvider",        isPk = false),
                FxSColumn(resultHeaderName = "facetSort",                attributeName = "facetSort",                isPk = false),
                FxSColumn(resultHeaderName = "ftsPhraseQuery",           attributeName = "ftsPhraseQuery",           isPk = false),
                FxSColumn(resultHeaderName = "localized",                attributeName = "localized",                isPk = false),
                FxSColumn(resultHeaderName = "valueProviderParameters",  attributeName = "valueProviderParameters",  isPk = false),
                FxSColumn(resultHeaderName = "ftsFuzzyQuery",            attributeName = "ftsFuzzyQuery",            isPk = false),
                FxSColumn(resultHeaderName = "useForAutocomplete",       attributeName = "useForAutocomplete",       isPk = false),
                FxSColumn(resultHeaderName = "ftsWildcardQueryBoost",    attributeName = "ftsWildcardQueryBoost",    isPk = false),
                FxSColumn(resultHeaderName = "useForSpellchecking",      attributeName = "useForSpellchecking",      isPk = false),
                FxSColumn(resultHeaderName = "ftsPhraseQuerySlop",       attributeName = "ftsPhraseQuerySlop",       isPk = false),
                FxSColumn(resultHeaderName = "fieldValueProvider",       attributeName = "fieldValueProvider",       isPk = false),
                FxSColumn(resultHeaderName = "currency",                 attributeName = "currency",                 isPk = false),
                FxSColumn(resultHeaderName = "ftsFuzzyQueryBoost",       attributeName = "ftsFuzzyQueryBoost",       isPk = false),
                FxSColumn(resultHeaderName = "facetType",                attributeName = "facetType",                isPk = false),
                FxSColumn(resultHeaderName = "facetDisplayNameProvider", attributeName = "facetDisplayNameProvider", isPk = false),
                FxSColumn(resultHeaderName = "ftsPhraseQueryBoost",      attributeName = "ftsPhraseQueryBoost",      isPk = false),
                FxSColumn(resultHeaderName = "multiValue",               attributeName = "multiValue",               isPk = false),
                FxSColumn(resultHeaderName = "useForHighlighting",       attributeName = "useForHighlighting",       isPk = false),
                FxSColumn(resultHeaderName = "exportId",                 attributeName = "exportId",                 isPk = false),
                FxSColumn(resultHeaderName = "facet",                    attributeName = "facet",                    isPk = false),
                FxSColumn(resultHeaderName = "ftsQueryMinTermLength",    attributeName = "ftsQueryMinTermLength",    isPk = false),
                FxSColumn(resultHeaderName = "solrIndexedType",          attributeName = "solrIndexedType",          isPk = false),
            ),
            uniqueAttributeNames = setOf("name", "solrindexedtype"),
            joinNaturalKeyByAttr = mapOf("solrindexedtype" to "identifier"),
        )
        // Params produced by FxSImpExHeaderBuilder.buildParams() for the queryInfo above.
        // solrIndexedType: unique=true, nestedPath from joinNaturalKeyByAttr = "identifier"
        //                  (NOT the full type-system composite key "identifier,indexname")
        val solrIndexedTypeParam = FxSImpExParam(
            attributeName = "solrIndexedType",
            nestedPath = "identifier",
            attributeType = "SolrIndexedType",
            metaType = FxSAttributeMetaType.ITEM,
            modifiers = listOf("unique=true"),
        )
        val params = listOf(
            atomicParam("sortableType"),
            atomicParam("valueProviderParameter"),
            atomicParam("type"),
            atomicParam("includeInResponse"),
            atomicParam("ftsFuzzyQueryFuzziness"),
            atomicParam("ftsWildcardQueryMinTermLength"),
            atomicParam("ftsQueryBoost"),
            atomicParam("ftsWildcardQuery"),
            atomicParam("rangeSet"),
            atomicParam("customFacetSortProvider"),
            atomicParam("visible"),
            atomicParam("ftsFuzzyQueryMinTermLength"),
            atomicParam("sealed"),
            atomicParam("priority"),
            atomicParam("categoryField"),
            atomicParam("classAttributeAssignment"),
            atomicParam("ftsQuery"),
            atomicParam("ftsWildcardQueryType"),
            atomicParam("name", unique = true),
            atomicParam("topValuesProvider"),
            atomicParam("facetSort"),
            atomicParam("ftsPhraseQuery"),
            atomicParam("localized"),
            atomicParam("valueProviderParameters"),
            atomicParam("ftsFuzzyQuery"),
            atomicParam("useForAutocomplete"),
            atomicParam("ftsWildcardQueryBoost"),
            atomicParam("useForSpellchecking"),
            atomicParam("ftsPhraseQuerySlop"),
            atomicParam("fieldValueProvider"),
            atomicParam("currency"),
            atomicParam("ftsFuzzyQueryBoost"),
            atomicParam("facetType"),
            atomicParam("facetDisplayNameProvider"),
            atomicParam("ftsPhraseQueryBoost"),
            atomicParam("multiValue"),
            atomicParam("useForHighlighting"),
            atomicParam("exportId"),
            atomicParam("facet"),
            atomicParam("ftsQueryMinTermLength"),
            solrIndexedTypeParam,
        )
        val rows = listOf(
            listOf(
                "8796095447642",   // pk (skipped)
                "<ignore>",        // sortableType
                "<ignore>",        // valueProviderParameter
                "text",            // type
                "true",            // includeInResponse
                "<ignore>",        // ftsFuzzyQueryFuzziness
                "0",               // ftsWildcardQueryMinTermLength
                "<ignore>",        // ftsQueryBoost
                "false",           // ftsWildcardQuery
                "<ignore>",        // rangeSet
                "<ignore>",        // customFacetSortProvider
                "true",            // visible
                "0",               // ftsFuzzyQueryMinTermLength (duplicate col)
                "<ignore>",        // sealed
                "0",               // priority
                "false",           // categoryField
                "8796095447650",   // classAttributeAssignment (raw pk — non-unique FK)
                "false",           // ftsQuery
                "POSTFIX",         // ftsWildcardQueryType
                "feature-powersupply", // name
                "<ignore>",        // topValuesProvider
                "<ignore>",        // facetSort
                "<ignore>",        // ftsPhraseQuery
                "false",           // localized
                "<ignore>",        // valueProviderParameters
                "false",           // ftsFuzzyQuery
                "false",           // useForAutocomplete
                "<ignore>",        // ftsWildcardQueryBoost
                "false",           // useForSpellchecking
                "<ignore>",        // ftsPhraseQuerySlop
                "commerceClassificationPropertyValueProvider", // fieldValueProvider
                "false",           // currency
                "<ignore>",        // ftsFuzzyQueryBoost
                "<ignore>",        // facetType
                "<ignore>",        // facetDisplayNameProvider
                "<ignore>",        // ftsPhraseQueryBoost
                "false",           // multiValue
                "false",           // useForHighlighting
                "<ignore>",        // exportId
                "false",           // facet
                "0",               // ftsQueryMinTermLength
                "mcProductType",   // solrIndexedType (resolved from PK via identifier lookup)
            )
        )

        val result = FxSImpExConverter.buildImpEx("SolrIndexedProperty", params, emptyList(), queryInfo, rows)

        // unique columns first: name, solrIndexedType(identifier) — then all non-unique in SELECT order
        val expected = """
            INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]; sortableType; valueProviderParameter; type; includeInResponse; ftsFuzzyQueryFuzziness; ftsWildcardQueryMinTermLength; ftsQueryBoost; ftsWildcardQuery; rangeSet; customFacetSortProvider; visible; ftsFuzzyQueryMinTermLength; sealed; priority; categoryField; classAttributeAssignment; ftsQuery; ftsWildcardQueryType; topValuesProvider; facetSort; ftsPhraseQuery; localized; valueProviderParameters; ftsFuzzyQuery; useForAutocomplete; ftsWildcardQueryBoost; useForSpellchecking; ftsPhraseQuerySlop; fieldValueProvider; currency; ftsFuzzyQueryBoost; facetType; facetDisplayNameProvider; ftsPhraseQueryBoost; multiValue; useForHighlighting; exportId; facet; ftsQueryMinTermLength
            ; "feature-powersupply"; mcProductType; <ignore>; <ignore>; "text"; "true"; <ignore>; "0"; <ignore>; "false"; <ignore>; <ignore>; "true"; "0"; <ignore>; "0"; "false"; "8796095447650"; "false"; "POSTFIX"; <ignore>; <ignore>; <ignore>; "false"; <ignore>; "false"; "false"; <ignore>; "false"; <ignore>; "commerceClassificationPropertyValueProvider"; "false"; <ignore>; <ignore>; <ignore>; <ignore>; "false"; "false"; <ignore>; "false"; "0"
        """.trimIndent() + "\n"

        assertEquals(expected, result)
    }

    // -------------------------------------------------------------------------
    // Non-unique FK column → attrName(pk), no natural key resolution
    // -------------------------------------------------------------------------

    /**
     * `thumbnail` is a FK (ComposedType) but NOT in the WHERE clause, so it is NOT unique.
     * The header must use `thumbnail(pk)` and the raw PK value from HAC passes through unchanged.
     * No follow-up lookup query is issued for non-unique FK columns.
     */
    @Test
    fun buildImpEx_nonUniqueFkColumn_renderedWithPkNestedPath() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "thumbnail", attributeName = "thumbnail", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            FxSImpExParam(
                attributeName = "thumbnail",
                nestedPath = "pk",
                attributeType = "Media",
                metaType = FxSAttributeMetaType.ITEM,
            ),
        )
        val rows = listOf(listOf("pk1", "637227", "8796218130462"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]; thumbnail(pk)\n; \"637227\"; 8796218130462\n",
            result
        )
    }

    // -------------------------------------------------------------------------
    // Collection attribute → attrName(pk)
    // -------------------------------------------------------------------------

    /**
     * A `Collection`-typed attribute must render as `attrName(pk)` in the header.
     * HAC returns a comma-separated list of PKs; ImpEx resolves each element by PK.
     */
    @Test
    fun buildImpEx_collectionColumn_renderedWithPkNestedPath() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "supercategories", attributeName = "supercategories", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            FxSImpExParam(
                attributeName = "supercategories",
                nestedPath = "pk",
                attributeType = "CategoryCollection",
                metaType = FxSAttributeMetaType.COLLECTION,
            ),
        )
        val rows = listOf(listOf("pk1", "myProduct", "8796163833886,8796245262366"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]; supercategories(pk)\n" +
                "; \"myProduct\"; 8796163833886,8796245262366\n",
            result
        )
    }

    // -------------------------------------------------------------------------
    // JOIN-unique columns (main fix: absent-from-SELECT WHERE condition columns)
    // -------------------------------------------------------------------------

    /**
     * Query: SELECT {pk},{name} FROM {SolrIndexedProperty AS t JOIN SolrIndexedType AS t0 ON {t0.pk}={t.solrIndexedType}}
     *        WHERE {t.name} = ?name AND {t0.identifier} = 'mcProductType'
     *
     * The `solrIndexedType` FK column is not in SELECT but is uniquely constrained via the JOIN.
     * It must appear as a synthetic column at the end with the constant value from the WHERE clause.
     *
     * Expected header:
     *   INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]
     * Expected data row:
     *   ; "propertyName"; mcProductType
     */
    @Test
    fun buildImpEx_joinUniqueColumn_appendedAtEndWithConstantValue() {
        val joinUniqueCol = FxSJoinUniqueColumn(
            fkAttributeName = "solrIndexedType",
            naturalKeyAttr = "identifier",
            constantValue = "mcProductType",
        )
        val queryInfo = FxSQueryInfo(
            primaryType = "SolrIndexedProperty",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = setOf("name", "solrIndexedType"),
            joinUniqueColumns = listOf(joinUniqueCol),
        )
        val params = listOf(atomicParam("name", unique = true))
        val joinUniqueParams = listOf(
            FxSImpExParam(
                attributeName = "solrIndexedType",
                nestedPath = "identifier",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            )
        )
        val rows = listOf(listOf("pk1", "propertyName"))

        val result = FxSImpExConverter.buildImpEx(
            "SolrIndexedProperty", params, joinUniqueParams, queryInfo, rows
        )

        assertEquals(
            "INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]\n" +
                "; \"propertyName\"; mcProductType\n",
            result
        )
    }

    @Test
    fun buildImpEx_joinUniqueColumn_nullConstantValue_renderedAsIgnore() {
        val joinUniqueCol = FxSJoinUniqueColumn(
            fkAttributeName = "solrIndexedType",
            naturalKeyAttr = "identifier",
            constantValue = null,  // bind-parameter query — value not known statically
        )
        val queryInfo = FxSQueryInfo(
            primaryType = "SolrIndexedProperty",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = setOf("name", "solrIndexedType"),
            joinUniqueColumns = listOf(joinUniqueCol),
        )
        val params = listOf(atomicParam("name", unique = true))
        val joinUniqueParams = listOf(
            FxSImpExParam(
                attributeName = "solrIndexedType",
                nestedPath = "identifier",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            )
        )
        val rows = listOf(listOf("pk1", "propertyName"))

        val result = FxSImpExConverter.buildImpEx(
            "SolrIndexedProperty", params, joinUniqueParams, queryInfo, rows
        )

        assertEquals(
            "INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]\n" +
                "; \"propertyName\"; <ignore>\n",
            result
        )
    }

    @Test
    fun buildImpEx_joinUniqueColumn_multipleRows_sameConstantInEveryRow() {
        val joinUniqueCol = FxSJoinUniqueColumn(
            fkAttributeName = "solrIndexedType",
            naturalKeyAttr = "identifier",
            constantValue = "mcProductType",
        )
        val queryInfo = FxSQueryInfo(
            primaryType = "SolrIndexedProperty",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = setOf("name", "solrIndexedType"),
            joinUniqueColumns = listOf(joinUniqueCol),
        )
        val params = listOf(atomicParam("name", unique = true))
        val joinUniqueParams = listOf(
            FxSImpExParam(
                attributeName = "solrIndexedType",
                nestedPath = "identifier",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            )
        )
        val rows = listOf(
            listOf("pk1", "property-A"),
            listOf("pk2", "property-B"),
        )

        val result = FxSImpExConverter.buildImpEx(
            "SolrIndexedProperty", params, joinUniqueParams, queryInfo, rows
        )

        assertEquals(
            "INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]\n" +
                "; \"property-A\"; mcProductType\n" +
                "; \"property-B\"; mcProductType\n",
            result
        )
    }

    // -------------------------------------------------------------------------
    // Enum column (after PK → code resolution)
    // -------------------------------------------------------------------------

    /**
     * After [FxSImpExHeaderBuilder.resolveEnumPks] the enum PK is replaced by its code.
     * The converter receives already-resolved rows — here we verify it passes enum values through.
     */
    @Test
    fun buildImpEx_enumColumn_valuePassedThrough() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Order",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "status", attributeName = "status", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            FxSImpExParam(
                attributeName = "status",
                nestedPath = "code",
                attributeType = "OrderStatus",
                metaType = FxSAttributeMetaType.ENUM,
            ),
        )
        // Row after enum PK resolution — enum cell already contains the code string
        val rows = listOf(listOf("pk1", "ORD-001", "CREATED"))

        val result = FxSImpExConverter.buildImpEx("Order", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Order; code[unique=true]; status(code)\n; \"ORD-001\"; CREATED\n",
            result
        )
    }

    // -------------------------------------------------------------------------
    // Missing cell in short row
    // -------------------------------------------------------------------------

    @Test
    fun buildImpEx_rowShorterThanColumns_missingCellsRenderedAsIgnore() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            atomicParam("name"),
        )
        // Row is missing the "name" cell entirely
        val rows = listOf(listOf("pk1", "myCode"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals("INSERT_UPDATE Product; code[unique=true]; name\n; \"myCode\"; <ignore>\n", result)
    }

    // -------------------------------------------------------------------------
    // Multi-level JOIN: FK in SELECT, no spurious synthetic columns
    // -------------------------------------------------------------------------

    /**
     * Simulates:
     * ```sql
     * SELECT {t.pk},{t.code},{t.name},{t.catalogVersion}
     * FROM {Product AS t
     *        JOIN CatalogVersion AS t0 ON {t0.pk} = {t.catalogversion}
     *        JOIN Catalog        AS t1 ON {t1.pk} = {t0.catalog}}
     * WHERE {t.code} = '637227' AND {t1.id} = 'mcProductCatalog' AND {t0.version} = 'Staged'
     * ```
     *
     * Both `t0` and `t1` resolve to the same root FK `catalogversion` after multi-level chain
     * traversal. `catalogVersion` IS in SELECT so no synthetic join-unique column is created.
     * The analyzer produces `uniqueAttributeNames = {"code", "catalogversion"}` (lowercase).
     * `resolveParam` matches `"catalogVersion".lowercase() == "catalogversion"` → `[unique=true]`.
     *
     * Expected header (unique first):
     *   `INSERT_UPDATE Product; code[unique=true]; catalogVersion(catalog(id),version)[unique=true]; name`
     */
    @Test
    fun buildImpEx_catalogVersionFkInSelect_multilevelJoin_noSyntheticColumn() {
        // uniqueAttributeNames as produced by the fixed analyzer: lowercase keys
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
            ),
            // Lowercase, as collectUniqueAttributes now emits. Both t0 and t1 resolve to
            // "catalogversion" so it appears only once in the set.
            uniqueAttributeNames = setOf("code", "catalogversion"),
            // No synthetic columns — catalogVersion IS in SELECT (excluded by fixed analyzer)
            joinUniqueColumns = emptyList(),
        )
        // params order must match non-pk columns order: code(1), name(2), catalogVersion(3)
        val params = listOf(
            // resolveParam sets unique=true: "code" matches "code" in uniqueAttributeNames
            atomicParam("code", unique = true),
            // name is not unique
            atomicParam("name"),
            // resolveParam sets unique=true: "catalogVersion".lowercase() matches "catalogversion"
            FxSImpExParam(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            ),
        )
        // row order: pk(0), code(1), name(2), catalogVersion(3)
        val rows = listOf(listOf("pk1", "637227", "My Product", "8796125987417"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]; catalogVersion(catalog(id),version)[unique=true]; name\n" +
                "; \"637227\"; 8796125987417; \"My Product\"\n",
            result
        )
    }

    // -------------------------------------------------------------------------
    // Unique columns reordered to the front
    // -------------------------------------------------------------------------

    /**
     * When unique columns appear after non-unique ones in the SELECT list, they must still be
     * emitted first in the ImpEx header and every value row.
     *
     * SELECT {pk},{name},{description},{code} FROM {Product} WHERE {code}=?code
     *   → header: code[unique=true]; name; description
     *   → row:    "myCode"; "My Product"; "Some description"
     */
    @Test
    fun buildImpEx_uniqueColumnsMovedToFront() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
                FxSColumn(resultHeaderName = "description", attributeName = "description", isPk = false),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            atomicParam("name"),
            atomicParam("description"),
            atomicParam("code", unique = true),
        )
        val rows = listOf(listOf("pk1", "My Product", "Some description", "myCode"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]; name; description\n" +
                "; \"myCode\"; \"My Product\"; \"Some description\"\n",
            result
        )
    }

    /**
     * Mixed: first SELECT column is unique, middle is non-unique, last two are unique.
     * All three unique columns must be grouped at the front.
     */
    @Test
    fun buildImpEx_multipleUniqueColumnsInterleavedWithNonUnique() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
                FxSColumn(resultHeaderName = "name", attributeName = "name", isPk = false),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
                FxSColumn(resultHeaderName = "description", attributeName = "description", isPk = false),
            ),
            uniqueAttributeNames = setOf("code", "catalogVersion"),
        )
        val params = listOf(
            atomicParam("code", unique = true),
            atomicParam("name"),
            FxSImpExParam(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            ),
            atomicParam("description"),
        )
        val rows = listOf(listOf("pk1", "myCode", "My Product", "Default:Staged", "Desc"))

        val result = FxSImpExConverter.buildImpEx("Product", params, emptyList(), queryInfo, rows)

        assertEquals(
            "INSERT_UPDATE Product; code[unique=true]; catalogVersion(catalog(id),version)[unique=true]; name; description\n" +
                "; \"myCode\"; Default:Staged; \"My Product\"; \"Desc\"\n",
            result
        )
    }
}
