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
import sap.commerce.toolset.typeSystem.model.Cardinality
import sap.commerce.toolset.typeSystem.model.PersistenceType

/** Classifies the resolved SAP Commerce meta-type of an ImpEx parameter column. */
enum class FxSAttributeMetaType {
    /** FK to another ComposedType (Item). */
    ITEM,

    /** Enum type — HAC returns the PK of the HybrisEnumerationValue; must be resolved to its code. */
    ENUM,

    /** Collection type. */
    COLLECTION,

    /** Atomic / primitive (String, Integer, Boolean, …). */
    ATOMIC,

    /** Type could not be determined (unknown attribute or dynamic attribute). */
    UNKNOWN,
}

/**
 * Describes a follow-up FlexibleSearch query needed to resolve FK PK values to their natural key strings.
 *
 * The [fxsLookupQuery] returns rows where column 0 is the PK and columns 1..N are the natural key
 * component values. These components are joined with `:` (the ImpEx path delimiter) to form the
 * final importable string, e.g. `mcProductCatalog:Staged` for a CatalogVersion reference.
 *
 * @param typeName       SAP Commerce type name of the FK attribute (e.g. `CatalogVersion`).
 * @param fxsLookupQuery Pre-built FxS SELECT query (e.g.
 *                       `SELECT {root.pk}, {j0.id}, {root.version} FROM {CatalogVersion AS root JOIN Catalog AS j0 ON {j0.pk} = {root.catalog}}`).
 */
data class FkResolutionInfo(
    val typeName: String,
    val fxsLookupQuery: String,
)

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
data class FxSImpExParam(
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

/**
 * Builds type-system-aware ImpEx header parameters for a [FxSQueryInfo].
 *
 * Resolution strategy per attribute meta-type:
 * - Primitive / java.lang.* / String / Boolean → plain parameter
 * - Enum  → `attrName(code)` so ImpEx resolves the enum value by its code attribute
 * - Collection → `attrName(pk)` (HAC values cleaned from serialization artifacts; each element resolved by pk)
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
                val regularAttr = primaryMeta?.allAttributes?.get(col.attributeName)
                val attrType = regularAttr?.type
                    ?: primaryMeta?.allRelationEnds
                        ?.firstOrNull { it.qualifier == col.attributeName && it.cardinality == Cardinality.ONE }
                        ?.type
                val isDynamic = regularAttr?.persistence?.type == PersistenceType.DYNAMIC
                resolveParam(col, attrType, isDynamic, tsAccess, queryInfo.uniqueAttributeNames)
            }
    }

    /**
     * Builds ImpEx params for JOIN-resolved unique attributes that are absent from the SELECT list.
     *
     * Each [FxSJoinUniqueColumn] becomes a param with `[unique=true]` and the nested path set to
     * the [FxSJoinUniqueColumn.naturalKeyAttr] specified in the WHERE condition.
     */
    fun buildJoinUniqueParams(queryInfo: FxSQueryInfo, project: Project): List<FxSImpExParam> {
        if (queryInfo.joinUniqueColumns.isEmpty()) return emptyList()
        val tsAccess = TSMetaModelAccess.getInstance(project)
        val primaryMeta = tsAccess.findMetaItemByName(queryInfo.primaryType)

        return queryInfo.joinUniqueColumns.map { joinCol ->
            val attrType = primaryMeta?.allAttributes?.get(joinCol.fkAttributeName)?.type
                ?: primaryMeta?.allRelationEnds
                    ?.firstOrNull { it.qualifier == joinCol.fkAttributeName && it.cardinality == Cardinality.ONE }
                    ?.type
            val metaType = if (attrType != null && tsAccess.findMetaClassifierByName(attrType) is TSGlobalMetaItem)
                FxSAttributeMetaType.ITEM else FxSAttributeMetaType.UNKNOWN
            FxSImpExParam(
                attributeName = joinCol.fkAttributeName,
                nestedPath = joinCol.naturalKeyAttr,
                modifiers = listOf("unique=true"),
                attributeType = attrType,
                metaType = metaType,
            )
        }
    }

    /**
     * Returns a map of result-row column index → enum type name for all ENUM-typed params.
     *
     * The returned indices correspond to positions in the raw HAC result rows (matching
     * [FxSQueryInfo.columns] indices). Callers use this to build follow-up lookup queries
     * (`SELECT {pk}, {code} FROM {EnumType}`) and to pass to [resolveEnumPks].
     */
    fun enumSourceIndicesByType(queryInfo: FxSQueryInfo, params: List<FxSImpExParam>): Map<Int, String> =
        queryInfo.columns
            .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }
            .zip(params)
            .filter { (_, param) -> param.metaType == FxSAttributeMetaType.ENUM && param.attributeType != null }
            .associate { (srcIdx, param) -> srcIdx to param.attributeType!! }

    /**
     * Replaces enum PK values in [rows] with their corresponding codes from [pkToCode].
     *
     * Only cells at column indices listed in [enumColIndices] are touched; all other cells
     * are passed through unchanged. If a PK has no entry in [pkToCode] the original value
     * is kept so the output is still usable (user can fix it manually).
     */
    fun resolveEnumPks(
        rows: List<List<String>>,
        enumColIndices: Set<Int>,
        pkToCode: Map<String, String>,
    ): List<List<String>> {
        if (enumColIndices.isEmpty()) return rows
        return rows.map { row ->
            row.mapIndexed { idx, cell ->
                if (idx in enumColIndices) pkToCode.getOrDefault(cell, cell) else cell
            }
        }
    }

    /**
     * Returns a map of result-row column index → [FkResolutionInfo] for all ITEM-typed params
     * that have a resolvable natural key path (i.e. not bare `pk`).
     *
     * Callers use this to build follow-up FxS queries that convert FK PK values to natural key
     * strings and then pass the result to [resolveFkPks].
     */
    fun fkSourceIndicesByResolutionInfo(queryInfo: FxSQueryInfo, params: List<FxSImpExParam>): Map<Int, FkResolutionInfo> =
        queryInfo.columns
            .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }
            .zip(params)
            .filter { (_, param) -> param.metaType == FxSAttributeMetaType.ITEM && param.fkResolutionInfo != null }
            .associate { (srcIdx, param) -> srcIdx to param.fkResolutionInfo!! }

    /**
     * Replaces FK PK values in [rows] with their corresponding natural key strings from [pkToNaturalKey].
     *
     * Only cells at column indices listed in [fkColIndices] are touched; all other cells are
     * passed through unchanged. If a PK has no entry in [pkToNaturalKey] the original value is
     * kept so the output remains usable (user can fix it manually).
     */
    fun resolveFkPks(
        rows: List<List<String>>,
        fkColIndices: Set<Int>,
        pkToNaturalKey: Map<String, String>,
    ): List<List<String>> {
        if (fkColIndices.isEmpty()) return rows
        return rows.map { row ->
            row.mapIndexed { idx, cell ->
                if (idx in fkColIndices) pkToNaturalKey.getOrDefault(cell, cell) else cell
            }
        }
    }

    // -------------------------------------------------------------------------
    // FK lookup query builder (testable, no IntelliJ platform types)
    // -------------------------------------------------------------------------

    /**
     * Builds a FlexibleSearch SELECT query that, when executed, returns rows of the form
     * `[pk, key_component_1, key_component_2, ...]` for [typeName].
     *
     * The natural key string for a resolved row is `key_component_1:key_component_2:...`
     * (colon = default ImpEx path-delimiter).
     *
     * Returns `null` when [nestedPath] is `"pk"` (no meaningful natural key to resolve).
     *
     * [attrTypes] maps lowercase FK attribute names of [typeName] to their SAP Commerce type names.
     * Only FK attributes that have parens in [nestedPath] need entries; scalar attributes are
     * emitted directly. An absent entry causes the FK attribute to be treated as a scalar.
     *
     * ### Examples
     * - `("Language", "isocode", {})` → `"SELECT {pk}, {isocode} FROM {Language}"`
     * - `("CatalogVersion", "catalog(id),version", {"catalog" → "Catalog"})` →
     *   `"SELECT {root.pk}, {j0.id}, {root.version} FROM {CatalogVersion AS root JOIN Catalog AS j0 ON {j0.pk} = {root.catalog}}"`
     */
    internal fun buildFkLookupQuery(
        typeName: String,
        nestedPath: String,
        attrTypes: Map<String, String>,
    ): String? {
        if (nestedPath == "pk") return null
        val tokens = splitTopLevel(nestedPath)
        val hasJoins = tokens.any { '(' in it }

        if (!hasJoins) {
            val selectCols = tokens.joinToString(", ") { "{$it}" }
            return "SELECT {pk}, $selectCols FROM {$typeName}"
        }

        // Build query with JOIN clauses for FK tokens
        val selectParts = mutableListOf<String>()
        val joinParts = mutableListOf<String>()
        var joinCount = 0

        tokens.forEach { token ->
            val parenIdx = token.indexOf('(')
            if (parenIdx < 0) {
                // Scalar attribute directly on the root type
                selectParts += "{root.$token}"
            } else {
                val fkAttr = token.substring(0, parenIdx)
                val innerPath = token.substring(parenIdx + 1, token.length - 1)
                val joinTypeName = attrTypes[fkAttr.lowercase()]
                if (joinTypeName == null) {
                    // Type unknown — emit as scalar (best-effort fallback)
                    selectParts += "{root.$fkAttr}"
                } else {
                    val alias = "j${joinCount++}"
                    joinParts += "$joinTypeName AS $alias ON {$alias.pk} = {root.$fkAttr}"
                    // Depth-1 only: inner path tokens are all scalars on the join alias
                    splitTopLevel(innerPath).forEach { inner -> selectParts += "{$alias.$inner}" }
                }
            }
        }

        val fromClause = buildString {
            append("$typeName AS root")
            joinParts.forEach { append(" JOIN $it") }
        }
        val allSelectCols = (listOf("{root.pk}") + selectParts).joinToString(", ")
        return "SELECT $allSelectCols FROM {$fromClause}"
    }

    /**
     * Splits [s] at top-level commas (commas not nested inside parentheses).
     *
     * e.g. `"catalog(id),version"` → `["catalog(id)", "version"]`
     */
    internal fun splitTopLevel(s: String): List<String> {
        val tokens = mutableListOf<String>()
        var depth = 0
        var start = 0
        for (i in s.indices) {
            when (s[i]) {
                '(' -> depth++
                ')' -> depth--
                ',' -> if (depth == 0) {
                    tokens += s.substring(start, i).trim()
                    start = i + 1
                }
            }
        }
        tokens += s.substring(start).trim()
        return tokens
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun resolveParam(
        col: FxSColumn,
        attrType: String?,
        isDynamic: Boolean,
        tsAccess: TSMetaModelAccess,
        uniqueAttributeNames: Set<String>,
    ): FxSImpExParam {
        val modifiers = mutableListOf<String>()
        // uniqueAttributeNames is stored lowercase; compare case-insensitively
        if (col.attributeName.lowercase() in uniqueAttributeNames) modifiers += "unique=true"
        if (col.isLocalized && col.langCode != null) modifiers += "lang=${col.langCode}"

        // Dynamic attributes cannot be imported — mark them as virtual
        if (isDynamic) {
            modifiers += "virtual=true"
            return FxSImpExParam(col.attributeName, modifiers = modifiers, metaType = FxSAttributeMetaType.UNKNOWN)
        }

        if (attrType == null) {
            // Unknown attribute — fall back to plain parameter, type not determinable
            return FxSImpExParam(col.attributeName, modifiers = modifiers, metaType = FxSAttributeMetaType.UNKNOWN)
        }

        return when (val meta = tsAccess.findMetaClassifierByName(attrType)) {
            is TSGlobalMetaItem -> {
                // FK to another ComposedType — resolve natural key path and pre-build lookup query
                val naturalPath = FxSNaturalKeyResolver.resolve(meta, tsAccess)
                val fkAttrTypes = buildFkAttrTypes(meta)
                val fkResolutionInfo = buildFkLookupQuery(attrType, naturalPath, fkAttrTypes)
                    ?.let { FkResolutionInfo(attrType, it) }
                FxSImpExParam(col.attributeName, nestedPath = naturalPath, modifiers = modifiers, attributeType = attrType, metaType = FxSAttributeMetaType.ITEM, fkResolutionInfo = fkResolutionInfo)
            }

            is TSGlobalMetaCollection -> {
                // Collection — attrName(pk): each element is resolved by PK
                FxSImpExParam(col.attributeName, nestedPath = "pk", modifiers = modifiers, attributeType = attrType, metaType = FxSAttributeMetaType.COLLECTION)
            }

            is TSGlobalMetaEnum -> {
                // Enum — reference by code: attrName(code) so ImpEx resolves the enum value by its code.
                // HAC returns the PK of the HybrisEnumerationValue item; callers must resolve it via
                // a follow-up SELECT {pk}, {code} FROM {EnumType} query.
                FxSImpExParam(col.attributeName, nestedPath = "code", modifiers = modifiers, attributeType = attrType, metaType = FxSAttributeMetaType.ENUM)
            }

            else -> {
                // Primitive, atomic, map, or unknown — plain parameter
                FxSImpExParam(col.attributeName, modifiers = modifiers, attributeType = attrType, metaType = FxSAttributeMetaType.ATOMIC)
            }
        }
    }

    /**
     * Builds a lowercase attribute-name → type-name map for FK-type resolution in [buildFkLookupQuery].
     *
     * Mirrors the same logic in [FxSNaturalKeyResolver] to keep the two resolvers consistent.
     */
    private fun buildFkAttrTypes(meta: TSGlobalMetaItem): Map<String, String> {
        val result = mutableMapOf<String, String>()
        meta.allAttributes.forEach { (name, attr) ->
            val type = attr.type ?: return@forEach
            result[name.lowercase()] = type
        }
        meta.allRelationEnds
            .filter { it.cardinality == Cardinality.ONE }
            .forEach { end ->
                val qualifier = end.qualifier?.lowercase() ?: return@forEach
                result.putIfAbsent(qualifier, end.type)
            }
        return result
    }
}
