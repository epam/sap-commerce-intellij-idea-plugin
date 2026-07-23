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

package sap.commerce.toolset.flexibleSearch.transform.impex.context

import sap.commerce.toolset.flexibleSearch.transform.context.FxSAttributeMetaType
import sap.commerce.toolset.typeSystem.TSConstants
import kotlin.test.Test
import kotlin.test.assertEquals

class ImpExHeaderParameterTest {

    // -------------------------------------------------------------------------
    // render()
    // -------------------------------------------------------------------------

    @Test
    fun render_plainAttribute() {
        val param = ImpExHeaderParameter(attributeName = "code")
        assertEquals("code", param.render())
    }

    @Test
    fun render_withNestedPath() {
        val param = ImpExHeaderParameter(attributeName = "catalogVersion", nestedPath = "catalog(id),version")
        assertEquals("catalogVersion(catalog(id),version)", param.render())
    }

    @Test
    fun render_withSingleModifier() {
        val param = ImpExHeaderParameter(attributeName = "code", modifiers = listOf("unique=true"))
        assertEquals("code[unique=true]", param.render())
    }

    @Test
    fun render_withMultipleModifiers() {
        val param = ImpExHeaderParameter(attributeName = "name", modifiers = listOf("lang=en", "unique=true"))
        assertEquals("name[lang=en,unique=true]", param.render())
    }

    @Test
    fun render_withNestedPathAndModifier() {
        val param = ImpExHeaderParameter(
            attributeName = "catalogVersion",
            nestedPath = "catalog(id),version",
            modifiers = listOf("unique=true"),
        )
        assertEquals("catalogVersion(catalog(id),version)[unique=true]", param.render())
    }

    @Test
    fun render_emptyNestedPath_skipsParens() {
        val param = ImpExHeaderParameter(attributeName = "code", nestedPath = "")
        assertEquals("code", param.render())
    }

    @Test
    fun render_blankNestedPath_skipsParens() {
        val param = ImpExHeaderParameter(attributeName = "code", nestedPath = "   ")
        assertEquals("code", param.render())
    }

    @Test
    fun render_enumParam_withCode() {
        val param = ImpExHeaderParameter(
            attributeName = "approvalStatus",
            nestedPath = "code",
            metaType = FxSAttributeMetaType.ENUM,
        )
        assertEquals("approvalStatus(code)", param.render())
    }

    // -------------------------------------------------------------------------
    // formatValue()
    // -------------------------------------------------------------------------

    @Test
    fun formatValue_emptyString_passThrough() {
        val param = ImpExHeaderParameter(attributeName = "code", attributeType = "java.lang.String")
        assertEquals("", param.formatValue(""))
    }

    @Test
    fun formatValue_javaLangString_wrapsInDoubleQuotes() {
        val param = ImpExHeaderParameter(attributeName = "code", attributeType = "java.lang.String")
        assertEquals("\"hello\"", param.formatValue("hello"))
    }

    @Test
    fun formatValue_localizableString_wrapsInDoubleQuotes() {
        val param = ImpExHeaderParameter(attributeName = "name", attributeType = "localized:java.lang.String")
        assertEquals("\"My Product\"", param.formatValue("My Product"))
    }

    @Test
    fun formatValue_localizableString_unknownMetaType_wrapsInDoubleQuotes() {
        // When the type system lookup fails the metaType is UNKNOWN but attributeType is still
        // set to "localized:java.lang.String" as a fallback — the value must still be quoted.
        val param = ImpExHeaderParameter(
            attributeName = "name",
            attributeType = "localized:java.lang.String",
            metaType = FxSAttributeMetaType.UNKNOWN,
        )
        assertEquals("\"FireStorm 18 Volt 6 Tool Combo Kit\"", param.formatValue("FireStorm 18 Volt 6 Tool Combo Kit"))
    }

    @Test
    fun formatValue_stringType_escapesEmbeddedDoubleQuotes() {
        val param = ImpExHeaderParameter(attributeName = "description", attributeType = "java.lang.String")
        // Input:  say "hello"
        // Output: "say ""hello"""
        assertEquals("\"say \"\"hello\"\"\"", param.formatValue("say \"hello\""))
    }

    @Test
    fun formatValue_integerType_passThrough() {
        val param = ImpExHeaderParameter(attributeName = "priority", attributeType = "java.lang.Integer")
        assertEquals("42", param.formatValue("42"))
    }

    @Test
    fun formatValue_unknownType_passThrough() {
        val param = ImpExHeaderParameter(attributeName = TSConstants.Attribute.PK)
        assertEquals("8796093054978", param.formatValue("8796093054978"))
    }

    @Test
    fun formatValue_composedTypeReference_passThrough() {
        val param = ImpExHeaderParameter(
            attributeName = "catalogVersion",
            nestedPath = "catalog(id),version",
            attributeType = "CatalogVersion",
            metaType = FxSAttributeMetaType.ITEM,
        )
        // CatalogVersion is not a String type — passed through as-is
        assertEquals("Default:Staged", param.formatValue("Default:Staged"))
    }

    @Test
    fun formatValue_enumCode_passThrough() {
        val param = ImpExHeaderParameter(
            attributeName = "approvalStatus",
            nestedPath = "code",
            attributeType = "ArticleApprovalStatus",
            metaType = FxSAttributeMetaType.ENUM,
        )
        assertEquals("approved", param.formatValue("approved"))
    }

    @Test
    fun formatValue_collection_stripsHacMarkerAndLeadingTrailingDelimiters() {
        val param = ImpExHeaderParameter(attributeName = "others", metaType = FxSAttributeMetaType.COLLECTION)
        // HAC raw value: ,#1,8796163833886,8796245262366,8796272525342,
        assertEquals(
            "8796163833886,8796245262366,8796272525342",
            param.formatValue(",#1,8796163833886,8796245262366,8796272525342,")
        )
    }

    @Test
    fun formatValue_collection_cleanValuePassThrough() {
        val param = ImpExHeaderParameter(attributeName = "others", metaType = FxSAttributeMetaType.COLLECTION)
        assertEquals("8796163833886,8796245262366", param.formatValue("8796163833886,8796245262366"))
    }

    @Test
    fun formatValue_collection_emptyValue_passThrough() {
        val param = ImpExHeaderParameter(attributeName = "others", metaType = FxSAttributeMetaType.COLLECTION)
        assertEquals("", param.formatValue(""))
    }

    @Test
    fun formatValue_collection_onlyMarkersAndDelimiters_producesEmpty() {
        val param = ImpExHeaderParameter(attributeName = "others", metaType = FxSAttributeMetaType.COLLECTION)
        assertEquals("", param.formatValue(",#1,,"))
    }
}