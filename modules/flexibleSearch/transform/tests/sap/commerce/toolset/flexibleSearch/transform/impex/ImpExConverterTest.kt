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

package sap.commerce.toolset.flexibleSearch.transform.impex

import com.intellij.openapi.command.impl.DummyProject
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.transform.context.*
import sap.commerce.toolset.flexibleSearch.transform.impex.context.ImpExHeaderParameter
import sap.commerce.toolset.typeSystem.TSConstants
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ImpExConverter.buildImpEx] covering the full ImpEx text generation pipeline.
 *
 * Each test constructs plain [FxSQueryInfo] / [ImpExHeaderParameter] data-class instances — no PSI or
 * IntelliJ services required.
 */
class ImpExConverterTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun makeContext(
        queryInfo: FxSQueryInfo,
        params: List<ImpExHeaderParameter>,
        rows: List<List<String>> = emptyList(),
        joinUniqueParams: List<ImpExHeaderParameter> = emptyList(),
    ) = ImpExTransformationRequest(
        project = DummyProject.getInstance(),
        queryInfo = queryInfo,
        params = params,
        joinUniqueParams = joinUniqueParams,
        rows = rows,
        execSettings = FlexibleSearchExecContext.defaultSettings(),
    )

    private fun atomicParam(name: String, unique: Boolean = false, type: String = "java.lang.String") =
        ImpExHeaderParameter(
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            atomicParam(TSConstants.Attribute.NAME),
        )
        val rows = listOf(listOf("8796093054978", "testProduct", "Test Product"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; name
; "testProduct"; "Test Product"
""",
            actual = result
        )
    }

    @Test
    fun buildImpEx_multipleRows() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(atomicParam(TSConstants.Attribute.CODE, unique = true))
        val rows = listOf(
            listOf("pk1", "product-A"),
            listOf("pk2", "product-B"),
        )

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]
; "product-A"
; "product-B"
""",
            actual = result
        )
    }

    @Test
    fun buildImpEx_pkOnlySelect_emptyValueRows() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true)),
            uniqueAttributeNames = emptySet(),
        )

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, emptyList(), listOf(listOf("pk1"))))

        // Header only, data row has no fields
        assertEquals(
            expected = "INSERT_UPDATE Product\n\n",
            actual = result
        )
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
                FxSColumn(resultHeaderName = "", attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = "", attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = "", attributeName = TSConstants.Attribute.NAME, isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            atomicParam(TSConstants.Attribute.NAME),
        )

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params))

        assertEquals(
            expected = "INSERT_UPDATE Product; code[unique=true]; name\n",
            actual = result
        )
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
                FxSColumn(resultHeaderName = "", attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = "", attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = "", attributeName = TSConstants.Attribute.NAME, isPk = false),
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
            atomicParam(TSConstants.Attribute.CODE),
            atomicParam(TSConstants.Attribute.NAME),
        )
        val joinUniqueParams = listOf(
            ImpExHeaderParameter(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            )
        )

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, joinUniqueParams = joinUniqueParams))

        assertEquals(
            expected = "INSERT_UPDATE Product; catalogVersion(catalog(id),version)[unique=true]; code; name\n",
            actual = result
        )
    }

    // -------------------------------------------------------------------------
    // NULL cell handling
    // -------------------------------------------------------------------------

    /**
     * HAC returns `"null"` for SQL NULLs and `"<ignore>"` for platform-level null values.
     * Both must produce the unquoted ImpEx `<ignore>` sentinel — not a quoted string literal.
     */
    @Test
    fun buildImpEx_nullAndHacIgnoreSentinels_renderedAsIgnore() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(atomicParam(TSConstants.Attribute.CODE), atomicParam(TSConstants.Attribute.NAME))
        val rows = listOf(listOf("pk1", "null", "<ignore>"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code; name
; <ignore>; <ignore>
""",
            actual = result
        )
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false, isLocalized = true, langCode = "en"),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(
            ImpExHeaderParameter(
                attributeName = TSConstants.Attribute.NAME,
                attributeType = "localizableString",
                metaType = FxSAttributeMetaType.ATOMIC,
                modifiers = listOf("lang=en"),
            )
        )
        val rows = listOf(listOf("pk1", "English Name"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; name[lang=en]
; "English Name"
""",
            actual = result
        )
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            ImpExHeaderParameter(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                attributeType = "CatalogVersion",
                metaType = FxSAttributeMetaType.ITEM,
                modifiers = listOf("unique=true"),
            ),
        )
        val rows = listOf(listOf("pk1", "myProduct", "Default:Staged"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; catalogVersion(catalog(id),version)[unique=true]
; "myProduct"; Default:Staged
""",
            actual = result
        )
    }

    /**
     * `SolrIndexedType` has a composite type-system key `identifier,indexname`. When the query
     * constrains only `{t0.identifier}` in the WHERE clause, `joinNaturalKeyByAttr` maps
     * `"solrindexedtype" → "identifier"`, overriding the full composite key so the header emits
     * `solrIndexedType(identifier)[unique=true]` — not `(identifier,indexname)`.
     *
     * Query: `… FROM {SolrIndexedProperty AS t JOIN SolrIndexedType AS t0 ON {t0.pk}={t.solrIndexedType}}`
     *        `WHERE {t.name} = 'feature-powersupply' AND {t0.identifier} = 'mcProductType'`
     */
    @Test
    fun buildImpEx_joinNaturalKeyOverridesFullCompositeKey() {
        val queryInfo = FxSQueryInfo(
            primaryType = "SolrIndexedProperty",
            columns = listOf(
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
                FxSColumn(resultHeaderName = "solrIndexedType", attributeName = "solrIndexedType", isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.NAME, "solrindexedtype"),
            joinNaturalKeyByAttr = mapOf("solrindexedtype" to "identifier"),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.NAME, unique = true),
            ImpExHeaderParameter(
                attributeName = "solrIndexedType",
                nestedPath = "identifier",
                attributeType = "SolrIndexedType",
                metaType = FxSAttributeMetaType.ITEM,
                modifiers = listOf("unique=true"),
            ),
        )
        val rows = listOf(listOf("pk1", "feature-powersupply", "mcProductType"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]
; "feature-powersupply"; mcProductType
""",
            actual = result
        )
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = "thumbnail", attributeName = "thumbnail", isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            ImpExHeaderParameter(
                attributeName = "thumbnail",
                nestedPath = TSConstants.Attribute.PK,
                attributeType = "Media",
                metaType = FxSAttributeMetaType.ITEM,
            ),
        )
        val rows = listOf(listOf("pk1", "637227", "8796218130462"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; thumbnail(pk)
; "637227"; 8796218130462
""",
            actual = result
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = "supercategories", attributeName = "supercategories", isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            ImpExHeaderParameter(
                attributeName = "supercategories",
                nestedPath = TSConstants.Attribute.PK,
                attributeType = "CategoryCollection",
                metaType = FxSAttributeMetaType.COLLECTION,
            ),
        )
        val rows = listOf(listOf("pk1", "myProduct", "8796163833886,8796245262366"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; supercategories(pk)
; "myProduct"; 8796163833886,8796245262366
""",
            actual = result
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.NAME, "solrIndexedType"),
            joinUniqueColumns = listOf(joinUniqueCol),
        )
        val params = listOf(atomicParam(TSConstants.Attribute.NAME, unique = true))
        val joinUniqueParams = listOf(
            ImpExHeaderParameter(
                attributeName = "solrIndexedType",
                nestedPath = "identifier",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            )
        )
        val rows = listOf(listOf("pk1", "propertyName"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows, joinUniqueParams))

        assertEquals(
            expected = """INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]
; "propertyName"; mcProductType
""",
            actual = result
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.NAME, "solrIndexedType"),
            joinUniqueColumns = listOf(joinUniqueCol),
        )
        val params = listOf(atomicParam(TSConstants.Attribute.NAME, unique = true))
        val joinUniqueParams = listOf(
            ImpExHeaderParameter(
                attributeName = "solrIndexedType",
                nestedPath = "identifier",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            )
        )
        val rows = listOf(listOf("pk1", "propertyName"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows, joinUniqueParams))

        assertEquals(
            expected = """INSERT_UPDATE SolrIndexedProperty; name[unique=true]; solrIndexedType(identifier)[unique=true]
; "propertyName"; <ignore>
""",
            actual = result
        )
    }

    // -------------------------------------------------------------------------
    // Enum column (after PK → code resolution)
    // -------------------------------------------------------------------------

    /**
     * After [ImpExHeaderBuilder.resolveEnumPks] the enum PK is replaced by its code.
     * The converter receives already-resolved rows — here we verify it passes enum values through.
     */
    @Test
    fun buildImpEx_enumColumn_valuePassedThrough() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Order",
            columns = listOf(
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = "status", attributeName = "status", isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            ImpExHeaderParameter(
                attributeName = "status",
                nestedPath = TSConstants.Attribute.CODE,
                attributeType = "OrderStatus",
                metaType = FxSAttributeMetaType.ENUM,
            ),
        )
        // Row after enum PK resolution — enum cell already contains the code string
        val rows = listOf(listOf("pk1", "ORD-001", "CREATED"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Order; code[unique=true]; status(code)
; "ORD-001"; CREATED
""",
            actual = result
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            atomicParam(TSConstants.Attribute.NAME),
        )
        // Row is missing the TSConstants.Attribute.NAME cell entirely
        val rows = listOf(listOf("pk1", "myCode"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; name
; "myCode"; <ignore>
""",
            actual = result
        )
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
     * The analyzer produces `uniqueAttributeNames = {TSConstants.Attribute.CODE, "catalogversion"}` (lowercase).
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
            ),
            // Lowercase, as collectUniqueAttributes now emits. Both t0 and t1 resolve to
            // "catalogversion" so it appears only once in the set.
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE, "catalogversion"),
            // No synthetic columns — catalogVersion IS in SELECT (excluded by fixed analyzer)
            joinUniqueColumns = emptyList(),
        )
        // params order must match non-pk columns order: code(1), name(2), catalogVersion(3)
        val params = listOf(
            // resolveParam sets unique=true: TSConstants.Attribute.CODE matches TSConstants.Attribute.CODE in uniqueAttributeNames
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            // name is not unique
            atomicParam(TSConstants.Attribute.NAME),
            // resolveParam sets unique=true: "catalogVersion".lowercase() matches "catalogversion"
            ImpExHeaderParameter(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            ),
        )
        // row order: pk(0), code(1), name(2), catalogVersion(3)
        val rows = listOf(listOf("pk1", "637227", "My Product", "8796125987417"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; catalogVersion(catalog(id),version)[unique=true]; name
; "637227"; 8796125987417; "My Product"
""",
            actual = result
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
                FxSColumn(resultHeaderName = "description", attributeName = "description", isPk = false),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.NAME),
            atomicParam("description"),
            atomicParam(TSConstants.Attribute.CODE, unique = true),
        )
        val rows = listOf(listOf("pk1", "My Product", "Some description", "myCode"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; name; description
; "myCode"; "My Product"; "Some description"
""",
            actual = result
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
                FxSColumn(resultHeaderName = TSConstants.Attribute.PK, attributeName = TSConstants.Attribute.PK, isPk = true),
                FxSColumn(resultHeaderName = TSConstants.Attribute.CODE, attributeName = TSConstants.Attribute.CODE, isPk = false),
                FxSColumn(resultHeaderName = TSConstants.Attribute.NAME, attributeName = TSConstants.Attribute.NAME, isPk = false),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
                FxSColumn(resultHeaderName = "description", attributeName = "description", isPk = false),
            ),
            uniqueAttributeNames = setOf(TSConstants.Attribute.CODE, "catalogVersion"),
        )
        val params = listOf(
            atomicParam(TSConstants.Attribute.CODE, unique = true),
            atomicParam(TSConstants.Attribute.NAME),
            ImpExHeaderParameter(
                attributeName = "catalogVersion",
                nestedPath = "catalog(id),version",
                modifiers = listOf("unique=true"),
                metaType = FxSAttributeMetaType.ITEM,
            ),
            atomicParam("description"),
        )
        val rows = listOf(listOf("pk1", "myCode", "My Product", "Default:Staged", "Desc"))

        val result = ImpExConverter.buildImpEx(makeContext(queryInfo, params, rows))

        assertEquals(
            expected = """INSERT_UPDATE Product; code[unique=true]; catalogVersion(catalog(id),version)[unique=true]; name; description
; "myCode"; Default:Staged; "My Product"; "Desc"
""",
            actual = result
        )
    }
}
