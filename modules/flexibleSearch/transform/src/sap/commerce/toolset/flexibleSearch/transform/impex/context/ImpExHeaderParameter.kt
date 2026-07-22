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

import sap.commerce.toolset.flexibleSearch.transform.context.FkResolutionInfo
import sap.commerce.toolset.flexibleSearch.transform.context.FxSAttributeMetaType

/**
 * Describes a resolved ImpEx header parameter with its modifiers and optional nested path.
 *
 * @param attributeName    The base attribute name as it appears in the ImpEx header.
 * @param nestedPath       Optional nested resolution path (e.g., `catalog(id),version` for CatalogVersion).
 * @param modifiers        Ordered list of `key=value` modifier strings.
 * @param attributeType    Resolved SAP Commerce type name (e.g. `java.lang.String`, `java.lang.Integer`,
 *                         `CatalogVersion`). Null when the type could not be determined.
 * @param metaType         Classified meta-type of the attribute.
 * @param fkResolutionInfo Follow-up query info for ITEM-typed params; null for all other meta-types.
 */
data class ImpExHeaderParameter(
    val attributeName: String,
    val nestedPath: String? = null,
    val modifiers: List<String> = emptyList(),
    val attributeType: String? = null,
    val metaType: FxSAttributeMetaType = FxSAttributeMetaType.UNKNOWN,
    val fkResolutionInfo: FkResolutionInfo? = null,
) {
    /** Renders the full ImpEx column definition, e.g. `catalogVersion(catalog(id),version)[unique=true]`. */
    fun render(): String = buildString {
        append(attributeName)
        if (!nestedPath.isNullOrBlank()) append("($nestedPath)")
        if (modifiers.isNotEmpty()) append("[${modifiers.joinToString(",")}]")
    }

    /**
     * Formats [value] for use in an ImpEx data row.
     *
     * - String-typed attributes: wraps the value in double-quotes and escapes any embedded `"` as `""`.
     * - Collection attributes: strips HAC's internal serialization artifacts (leading/trailing
     *   delimiter tokens, `#N` internal markers) leaving only the comma-separated PK values.
     * - All other / unknown types: returns the value unchanged.
     * - Empty values are always returned unchanged.
     */
    fun formatValue(value: String): String = when {
        value.isEmpty() -> value
        metaType == FxSAttributeMetaType.COLLECTION -> cleanCollectionValue(value)
        attributeType in STRING_ATTRIBUTE_TYPES -> "\"${value.replace("\"", "\"\"")}\""
        else -> value
    }

    /**
     * Strips HAC collection serialization artifacts from [value].
     *
     * HAC returns collection attributes in a format like `,#1,8796163833886,8796245262366,` where:
     * - Leading/trailing delimiters produce empty tokens
     * - `#N` tokens are internal SAP Commerce markers (not item references)
     *
     * Returns only the meaningful tokens joined by `,`.
     */
    private fun cleanCollectionValue(value: String): String =
        value.split(",")
            .filter { token -> token.isNotEmpty() && !token.matches(HAC_INTERNAL_MARKER) }
            .joinToString(",")

    companion object {
        /** SAP Commerce atomic type names whose values require double-quote wrapping in ImpEx. */
        val STRING_ATTRIBUTE_TYPES = setOf("java.lang.String", "localizableString")

        /** Matches HAC internal collection markers like `#1`, `#42`. */
        private val HAC_INTERNAL_MARKER = Regex("#\\d+")
    }
}