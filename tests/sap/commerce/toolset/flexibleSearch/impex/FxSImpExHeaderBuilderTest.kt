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

class FxSImpExHeaderBuilderTest {

    // -------------------------------------------------------------------------
    // resolveEnumPks()
    // -------------------------------------------------------------------------

    @Test
    fun resolveEnumPks_replacesKnownEnumPks() {
        val rows = listOf(listOf("8796093054978", "8796093055000", "someValue"))
        val enumColIndices = setOf(1)
        val pkToCode = mapOf("8796093055000" to "APPROVED")

        val result = FxSImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

        assertEquals(listOf(listOf("8796093054978", "APPROVED", "someValue")), result)
    }

    @Test
    fun resolveEnumPks_keepsOriginalValueForUnknownPk() {
        val rows = listOf(listOf("8796093054978", "9999999999999"))
        val enumColIndices = setOf(1)
        val pkToCode = mapOf("8796093055000" to "APPROVED")

        val result = FxSImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

        assertEquals(listOf(listOf("8796093054978", "9999999999999")), result)
    }

    @Test
    fun resolveEnumPks_emptyEnumIndices_returnsRowsUnchanged() {
        val rows = listOf(listOf("8796093054978", "8796093055000"))
        val pkToCode = mapOf("8796093055000" to "APPROVED")

        val result = FxSImpExHeaderBuilder.resolveEnumPks(rows, emptySet(), pkToCode)

        assertEquals(rows, result)
    }

    @Test
    fun resolveEnumPks_multipleEnumColumns_replacesAll() {
        val rows = listOf(listOf("pk1", "enum1pk", "otherValue", "enum2pk"))
        val enumColIndices = setOf(1, 3)
        val pkToCode = mapOf("enum1pk" to "STATUS_A", "enum2pk" to "TYPE_B")

        val result = FxSImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

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

        val result = FxSImpExHeaderBuilder.resolveEnumPks(rows, enumColIndices, pkToCode)

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
            FxSImpExParam(attributeName = "status", attributeType = "OrderStatus", metaType = FxSAttributeMetaType.ENUM),
            FxSImpExParam(attributeName = "code", attributeType = "java.lang.String", metaType = FxSAttributeMetaType.ATOMIC),
        )

        val result = FxSImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)

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
            FxSImpExParam(attributeName = "code", attributeType = "java.lang.String", metaType = FxSAttributeMetaType.ATOMIC),
            FxSImpExParam(attributeName = "name", attributeType = "localizableString", metaType = FxSAttributeMetaType.ATOMIC),
        )

        val result = FxSImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)

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
            FxSImpExParam(attributeName = "status", attributeType = null, metaType = FxSAttributeMetaType.ENUM),
        )

        val result = FxSImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)

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
            FxSImpExParam(attributeName = "deliveryMode", attributeType = "DeliveryMode", metaType = FxSAttributeMetaType.ENUM),
            FxSImpExParam(attributeName = "paymentMode", attributeType = "PaymentMode", metaType = FxSAttributeMetaType.ENUM),
        )

        val result = FxSImpExHeaderBuilder.enumSourceIndicesByType(queryInfo, params)

        assertEquals(mapOf(1 to "DeliveryMode", 2 to "PaymentMode"), result)
    }
}
