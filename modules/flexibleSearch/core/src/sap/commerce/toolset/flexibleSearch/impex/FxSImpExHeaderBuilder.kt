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

import com.intellij.openapi.project.Project
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaCollection
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaEnum
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem.TSGlobalMetaItemAttribute
import sap.commerce.toolset.typeSystem.model.PersistenceType

/**
 * Describes a resolved ImpEx header parameter with its modifiers and optional nested path.
 *
 * @param attributeName  The base attribute name as it appears in the ImpEx header.
 * @param nestedPath     Optional nested resolution path (e.g., `catalog(id),version` for CatalogVersion).
 * @param modifiers      Ordered list of `key=value` modifier strings.
 * @param attributeType  Resolved SAP Commerce type name (e.g. `java.lang.String`, `java.lang.Integer`,
 *                       `CatalogVersion`). Null when the type could not be determined.
 */
data class FxSImpExParam(
    val attributeName: String,
    val nestedPath: String? = null,
    val modifiers: List<String> = emptyList(),
    val attributeType: String? = null,
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
     * - All other / unknown types: returns the value unchanged.
     * - Empty values are always returned unchanged.
     */
    fun formatValue(value: String): String = when {
        value.isEmpty() -> value
        attributeType in STRING_ATTRIBUTE_TYPES -> "\"${value.replace("\"", "\"\"")}\""
        else -> value
    }

    companion object {
        /** SAP Commerce atomic type names whose values require double-quote wrapping in ImpEx. */
        val STRING_ATTRIBUTE_TYPES = setOf("java.lang.String", "localizableString")
    }
}

/**
 * Builds type-system-aware ImpEx header parameters for a [FxSQueryInfo].
 *
 * Resolution strategy per attribute meta-type:
 * - Primitive / java.lang.* / String / Boolean → plain parameter
 * - Enum  → plain parameter (enum codes are strings)
 * - Collection → plain parameter with `[collection-delimiter=,]`
 * - ComposedType (item FK) → `attrName(naturalKeyPath)` resolved by [FxSNaturalKeyResolver]
 * - Localized → adds `lang=xx` modifier
 * - Dynamic attribute → skipped (cannot be imported via ImpEx)
 */
object FxSImpExHeaderBuilder {

    fun buildParams(queryInfo: FxSQueryInfo, project: Project): List<FxSImpExParam> {
        val tsAccess = TSMetaModelAccess.getInstance(project)
        val primaryMeta = tsAccess.findMetaItemByName(queryInfo.primaryType)

        return queryInfo.columns
            .filterNot { it.isPk }
            .map { col ->
                val attr = primaryMeta?.allAttributes?.get(col.attributeName)
                resolveParam(col, attr, tsAccess, queryInfo.uniqueAttributeNames)
            }
    }

    private fun resolveParam(
        col: FxSColumn,
        attr: TSGlobalMetaItemAttribute?,
        tsAccess: TSMetaModelAccess,
        uniqueAttributeNames: Set<String>,
    ): FxSImpExParam {
        val modifiers = mutableListOf<String>()
        if (col.attributeName in uniqueAttributeNames) modifiers += "unique=true"
        if (col.isLocalized && col.langCode != null) modifiers += "lang=${col.langCode}"

        // Dynamic attributes cannot be imported — mark them as virtual
        if (attr?.persistence?.type == PersistenceType.DYNAMIC) {
            modifiers += "virtual=true"
            return FxSImpExParam(col.attributeName, modifiers = modifiers)
        }

        val attrType = attr?.type
        if (attrType == null) {
            // Unknown attribute — fall back to plain parameter, type not determinable
            return FxSImpExParam(col.attributeName, modifiers = modifiers)
        }

        return when (val meta = tsAccess.findMetaClassifierByName(attrType)) {
            is TSGlobalMetaItem -> {
                // FK to another ComposedType — resolve natural key path
                val naturalPath = FxSNaturalKeyResolver.resolve(meta, tsAccess)
                FxSImpExParam(col.attributeName, nestedPath = naturalPath, modifiers = modifiers, attributeType = attrType)
            }

            is TSGlobalMetaCollection -> {
                // Collection type — add collection-delimiter modifier
                modifiers += "collection-delimiter=,"
                FxSImpExParam(col.attributeName, modifiers = modifiers, attributeType = attrType)
            }

            is TSGlobalMetaEnum -> {
                // Enum — plain parameter, values are string codes
                FxSImpExParam(col.attributeName, modifiers = modifiers, attributeType = attrType)
            }

            else -> {
                // Primitive, atomic, map, or unknown — plain parameter
                FxSImpExParam(col.attributeName, modifiers = modifiers, attributeType = attrType)
            }
        }
    }
}
