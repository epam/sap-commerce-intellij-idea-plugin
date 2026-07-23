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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImpExHeaderBuilderTest  {

    private fun makeContext(queryInfo: FxSQueryInfo, params: List<ImpExHeaderParameter>) =
        FxSTransformationRequest(
            project = DummyProject.getInstance(),
            queryInfo = queryInfo,
            params = params,
            joinUniqueParams = emptyList(),
            rows = emptyList(),
            execSettings = FlexibleSearchExecContext.defaultSettings(),
        )

    // -------------------------------------------------------------------------
    // resolveEnumPks()
    // -------------------------------------------------------------------------

    @Test
    fun resolveEnumPks_replacesKnownEnumPks() {
        val rows = listOf(listOf("8796093054978", "8796093055000", "someValue"))
        val enumColIndices = setOf(1)
        val pkToCode = mapOf("8796093055000" to "APPROVED")

        val result = ImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

        assertEquals(listOf(listOf("8796093054978", "APPROVED", "someValue")), result)
    }

    @Test
    fun resolveEnumPks_keepsOriginalValueForUnknownPk() {
        val rows = listOf(listOf("8796093054978", "9999999999999"))
        val enumColIndices = setOf(1)
        val pkToCode = mapOf("8796093055000" to "APPROVED")

        val result = ImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

        assertEquals(listOf(listOf("8796093054978", "9999999999999")), result)
    }

    @Test
    fun resolveEnumPks_emptyEnumIndices_returnsRowsUnchanged() {
        val rows = listOf(listOf("8796093054978", "8796093055000"))
        val pkToCode = mapOf("8796093055000" to "APPROVED")

        val result = ImpExHeaderBuilder.resolveEnumPks(rows, emptySet(), pkToCode)

        assertEquals(rows, result)
    }

    @Test
    fun resolveEnumPks_multipleEnumColumns_replacesAll() {
        val rows = listOf(listOf("pk1", "enum1pk", "otherValue", "enum2pk"))
        val enumColIndices = setOf(1, 3)
        val pkToCode = mapOf("enum1pk" to "STATUS_A", "enum2pk" to "TYPE_B")

        val result = ImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

        assertEquals(listOf(listOf("pk1", "STATUS_A", "otherValue", "TYPE_B")), result)
    }

    @Test
    fun resolveEnumPks_multipleRows_replacesInAllRows() {
        val rows = listOf(
            listOf("pk1", "enumPk1"),
            listOf("pk2", "enumPk2"),
        )
        val enumColIndices = setOf(1)
        val pkToCode = mapOf("enumPk1" to "CODE_A", "enumPk2" to "CODE_B")

        val result = ImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

        assertEquals(listOf(listOf("pk1", "CODE_A"), listOf("pk2", "CODE_B")), result)
    }

    // -------------------------------------------------------------------------
    // enumSourceIndicesByType()
    // -------------------------------------------------------------------------

    @Test
    fun enumSourceIndicesByType_returnsIndexForEnumColumn() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Order",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "status", attributeName = "status", isPk = false),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            ImpExHeaderParameter(attributeName = "status", attributeType = "OrderStatus", metaType = FxSAttributeMetaType.ENUM),
            ImpExHeaderParameter(attributeName = "code", attributeType = "java.lang.String", metaType = FxSAttributeMetaType.ATOMIC),
        )

        val result = ImpExHeaderBuilder.enumSourceIndicesByType(makeContext(queryInfo, params))

        // Column index 1 (status) → "OrderStatus"
        assertEquals(mapOf(1 to "OrderStatus"), result)
    }

    @Test
    fun enumSourceIndicesByType_skipsNonEnumColumns() {
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
            ImpExHeaderParameter(attributeName = "code", attributeType = "java.lang.String", metaType = FxSAttributeMetaType.ATOMIC),
            ImpExHeaderParameter(attributeName = "name", attributeType = "localizableString", metaType = FxSAttributeMetaType.ATOMIC),
        )

        val result = ImpExHeaderBuilder.enumSourceIndicesByType(makeContext(queryInfo, params))

        assertEquals(emptyMap(), result)
    }

    @Test
    fun enumSourceIndicesByType_skipsEnumParamWithNullType() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "status", attributeName = "status", isPk = false),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(
            // metaType=ENUM but attributeType=null — should be skipped
            ImpExHeaderParameter(attributeName = "status", attributeType = null, metaType = FxSAttributeMetaType.ENUM),
        )

        val result = ImpExHeaderBuilder.enumSourceIndicesByType(makeContext(queryInfo, params))

        assertEquals(emptyMap(), result)
    }

    @Test
    fun enumSourceIndicesByType_multipleEnumColumns_returnsAllIndices() {
        val queryInfo = FxSQueryInfo(
            primaryType = "OrderEntry",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "deliveryMode", attributeName = "deliveryMode", isPk = false),
                FxSColumn(resultHeaderName = "paymentMode", attributeName = "paymentMode", isPk = false),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(
            ImpExHeaderParameter(attributeName = "deliveryMode", attributeType = "DeliveryMode", metaType = FxSAttributeMetaType.ENUM),
            ImpExHeaderParameter(attributeName = "paymentMode", attributeType = "PaymentMode", metaType = FxSAttributeMetaType.ENUM),
        )

        val result = ImpExHeaderBuilder.enumSourceIndicesByType(makeContext(queryInfo, params))

        assertEquals(mapOf(1 to "DeliveryMode", 2 to "PaymentMode"), result)
    }

    // -------------------------------------------------------------------------
    // splitTopLevel()
    // -------------------------------------------------------------------------

    @Test
    fun splitTopLevel_singleToken_returnsSingleElement() {
        assertEquals(listOf("isocode"), ImpExHeaderBuilder.splitTopLevel("isocode"))
    }

    @Test
    fun splitTopLevel_twoScalars_splitsByComma() {
        assertEquals(listOf("catalog", "version"), ImpExHeaderBuilder.splitTopLevel("catalog,version"))
    }

    @Test
    fun splitTopLevel_fkTokenWithInnerComma_notSplitInside() {
        // "catalog(id),version" — the comma inside "catalog(id)" is depth 1, not split
        assertEquals(listOf("catalog(id)", "version"), ImpExHeaderBuilder.splitTopLevel("catalog(id),version"))
    }

    @Test
    fun splitTopLevel_deeplyNested_splitOnlyAtTopLevel() {
        // "a(b(c,d),e),f"
        assertEquals(listOf("a(b(c,d),e)", "f"), ImpExHeaderBuilder.splitTopLevel("a(b(c,d),e),f"))
    }

    // -------------------------------------------------------------------------
    // buildFkLookupQuery()
    // -------------------------------------------------------------------------

    @Test
    fun buildFkLookupQuery_pkPath_returnsNull() {
        assertNull(ImpExHeaderBuilder.buildFkLookupQuery("Language", "pk", emptyMap()))
    }

    @Test
    fun buildFkLookupQuery_simpleScalar_noJoin() {
        val query = ImpExHeaderBuilder.buildFkLookupQuery("Language", "isocode", emptyMap())
        assertEquals("SELECT {pk}, {isocode} FROM {Language}", query)
    }

    @Test
    fun buildFkLookupQuery_multipleScalars_noJoin() {
        val query = ImpExHeaderBuilder.buildFkLookupQuery("Catalog", "id", emptyMap())
        assertEquals("SELECT {pk}, {id} FROM {Catalog}", query)
    }

    @Test
    fun buildFkLookupQuery_catalogVersion_withCatalogJoin() {
        val attrTypes = mapOf("catalog" to "Catalog", "version" to "java.lang.String")
        val query = ImpExHeaderBuilder.buildFkLookupQuery("CatalogVersion", "catalog(id),version", attrTypes)
        assertEquals(
            "SELECT {root.pk}, {j0.id}, {root.version} FROM {CatalogVersion AS root JOIN Catalog AS j0 ON {j0.pk} = {root.catalog}}",
            query,
        )
    }

    @Test
    fun buildFkLookupQuery_unknownFkType_fallsBackToScalar() {
        // "catalog" has parens but its type is absent from attrTypes → emitted as scalar
        val query = ImpExHeaderBuilder.buildFkLookupQuery("CatalogVersion", "catalog(id),version", emptyMap())
        assertEquals(
            "SELECT {root.pk}, {root.catalog}, {root.version} FROM {CatalogVersion AS root}",
            query,
        )
    }

    @Test
    fun buildFkLookupQuery_multipleScalarPaths_noJoin() {
        val query = ImpExHeaderBuilder.buildFkLookupQuery("Product", "catalogVersion,code", emptyMap())
        assertEquals("SELECT {pk}, {catalogVersion}, {code} FROM {Product}", query)
    }

    /**
     * When a JOIN-constrained FK has a composite type-system key (e.g. `identifier,indexname`)
     * but the WHERE clause only uses one attribute (e.g. `identifier`), the lookup query must
     * only SELECT that one attribute — otherwise the resolved natural key string would contain
     * more parts than the ImpEx header declares, causing an import error.
     */
    @Test
    fun buildFkLookupQuery_singleAttrOverride_producesSimpleSelectWithoutJoin() {
        // joinNaturalKey override = "identifier"; full type-system key would be "identifier,indexname"
        val query = ImpExHeaderBuilder.buildFkLookupQuery("SolrIndexedType", "identifier", emptyMap())
        assertEquals("SELECT {pk}, {identifier} FROM {SolrIndexedType}", query)
    }

    /**
     * Multi-level nested path — `baseProduct(code,catalogVersion(catalog(id),version))` style.
     * The nested `catalog(id)` token lives on `CatalogVersion`, so its type must come from
     * the `attrTypesLookup` callback and produce a second-level JOIN off the first join alias.
     *
     * SELECT column order is depth-first — matching the `:`-joined natural key value order
     * ImpEx expects for the nested header path.
     */
    @Test
    fun buildFkLookupQuery_multiLevelNestedPath_producesChainedJoins() {
        val rootAttrTypes = mapOf("catalogversion" to "CatalogVersion")
        val query = ImpExHeaderBuilder.buildFkLookupQuery(
            "Product",
            "code,catalogVersion(catalog(id),version)",
            rootAttrTypes,
        ) { typeName -> if (typeName == "CatalogVersion") mapOf("catalog" to "Catalog") else emptyMap() }

        assertEquals(
            "SELECT {root.pk}, {root.code}, {j1.id}, {j0.version} " +
                "FROM {Product AS root " +
                "JOIN CatalogVersion AS j0 ON {j0.pk} = {root.catalogVersion} " +
                "JOIN Catalog AS j1 ON {j1.pk} = {j0.catalog}}",
            query,
        )
    }

    /**
     * Without an `attrTypesLookup`, a second-level FK token (`catalog(id)`) degrades to a scalar
     * on the first join alias — best-effort fallback, same as the depth-1 unknown-type case.
     */
    @Test
    fun buildFkLookupQuery_multiLevelNestedPath_withoutLookup_innerFkFallsBackToScalar() {
        val rootAttrTypes = mapOf("catalogversion" to "CatalogVersion")
        val query = ImpExHeaderBuilder.buildFkLookupQuery(
            "Product",
            "code,catalogVersion(catalog(id),version)",
            rootAttrTypes,
        )

        assertEquals(
            "SELECT {root.pk}, {root.code}, {j0.catalog}, {j0.version} " +
                "FROM {Product AS root " +
                "JOIN CatalogVersion AS j0 ON {j0.pk} = {root.catalogVersion}}",
            query,
        )
    }

    // -------------------------------------------------------------------------
    // fkSourceIndicesByResolutionInfo()
    // -------------------------------------------------------------------------

    @Test
    fun fkSourceIndicesByResolutionInfo_returnsIndexForItemColumnWithFkInfo() {
        val fkInfo = FkResolutionInfo("CatalogVersion", "SELECT {pk}, {isocode} FROM {Language}")
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
            ),
            uniqueAttributeNames = setOf("code"),
        )
        val params = listOf(
            ImpExHeaderParameter(attributeName = "catalogVersion", attributeType = "CatalogVersion", metaType = FxSAttributeMetaType.ITEM, fkResolutionInfo = fkInfo),
            ImpExHeaderParameter(attributeName = "code", attributeType = "java.lang.String", metaType = FxSAttributeMetaType.ATOMIC),
        )

        val result = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(makeContext(queryInfo, params))

        assertEquals(mapOf(1 to fkInfo), result)
    }

    @Test
    fun fkSourceIndicesByResolutionInfo_skipsItemColumnWithNullFkInfo() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Product",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "catalogVersion", attributeName = "catalogVersion", isPk = false),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(
            // ITEM but no fkResolutionInfo (e.g. natural key is just "pk")
            ImpExHeaderParameter(attributeName = "catalogVersion", attributeType = "CatalogVersion", metaType = FxSAttributeMetaType.ITEM, fkResolutionInfo = null),
        )

        val result = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(makeContext(queryInfo, params))

        assertEquals(emptyMap(), result)
    }

    @Test
    fun fkSourceIndicesByResolutionInfo_skipsNonItemColumns() {
        val queryInfo = FxSQueryInfo(
            primaryType = "Order",
            columns = listOf(
                FxSColumn(resultHeaderName = "pk", attributeName = "pk", isPk = true),
                FxSColumn(resultHeaderName = "status", attributeName = "status", isPk = false),
                FxSColumn(resultHeaderName = "code", attributeName = "code", isPk = false),
            ),
            uniqueAttributeNames = emptySet(),
        )
        val params = listOf(
            ImpExHeaderParameter(attributeName = "status", attributeType = "OrderStatus", metaType = FxSAttributeMetaType.ENUM),
            ImpExHeaderParameter(attributeName = "code", attributeType = "java.lang.String", metaType = FxSAttributeMetaType.ATOMIC),
        )

        val result = ImpExHeaderBuilder.fkSourceIndicesByResolutionInfo(makeContext(queryInfo, params))

        assertEquals(emptyMap(), result)
    }

    // -------------------------------------------------------------------------
    // resolveFkPks()
    // -------------------------------------------------------------------------

    @Test
    fun resolveFkPks_replacesKnownFkPks() {
        val rows = listOf(listOf("rowPk", "8796125987417", "637227"))
        val fkColIndices = setOf(1)
        val pkToNaturalKey = mapOf("8796125987417" to "mcProductCatalog:Staged")

        val result = ImpExHeaderBuilder.resolveFkPks(rows, fkColIndices, pkToNaturalKey)

        assertEquals(listOf(listOf("rowPk", "mcProductCatalog:Staged", "637227")), result)
    }

    @Test
    fun resolveFkPks_keepsOriginalValueForUnknownPk() {
        val rows = listOf(listOf("rowPk", "9999999999999"))
        val fkColIndices = setOf(1)
        val pkToNaturalKey = mapOf("8796125987417" to "mcProductCatalog:Staged")

        val result = ImpExHeaderBuilder.resolveFkPks(rows, fkColIndices, pkToNaturalKey)

        assertEquals(listOf(listOf("rowPk", "9999999999999")), result)
    }

    @Test
    fun resolveFkPks_emptyIndices_returnsRowsUnchanged() {
        val rows = listOf(listOf("rowPk", "8796125987417"))
        val pkToNaturalKey = mapOf("8796125987417" to "mcProductCatalog:Staged")

        val result = ImpExHeaderBuilder.resolveFkPks(rows, emptySet(), pkToNaturalKey)

        assertEquals(rows, result)
    }

    @Test
    fun resolveFkPks_multipleFkColumns_replacesAll() {
        val rows = listOf(listOf("rowPk", "fk1Pk", "scalar", "fk2Pk"))
        val fkColIndices = setOf(1, 3)
        val pkToNaturalKey = mapOf("fk1Pk" to "mcProductCatalog:Staged", "fk2Pk" to "en")

        val result = ImpExHeaderBuilder.resolveFkPks(rows, fkColIndices, pkToNaturalKey)

        assertEquals(listOf(listOf("rowPk", "mcProductCatalog:Staged", "scalar", "en")), result)
    }

    @Test
    fun resolveFkPks_multipleRows_replacesInAllRows() {
        val rows = listOf(
            listOf("pk1", "fkPk1"),
            listOf("pk2", "fkPk2"),
        )
        val fkColIndices = setOf(1)
        val pkToNaturalKey = mapOf("fkPk1" to "catalogA:Online", "fkPk2" to "catalogB:Staged")

        val result = ImpExHeaderBuilder.resolveFkPks(rows, fkColIndices, pkToNaturalKey)

        assertEquals(listOf(listOf("pk1", "catalogA:Online"), listOf("pk2", "catalogB:Staged")), result)
    }
}
