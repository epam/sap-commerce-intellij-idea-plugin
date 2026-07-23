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

import com.intellij.openapi.project.Project
import sap.commerce.toolset.flexibleSearch.transform.FxSNaturalKeyResolver
import sap.commerce.toolset.flexibleSearch.transform.context.FkResolutionInfo
import sap.commerce.toolset.flexibleSearch.transform.context.FxSAttributeMetaType
import sap.commerce.toolset.flexibleSearch.transform.context.FxSColumn
import sap.commerce.toolset.flexibleSearch.transform.context.FxSQueryInfo
import sap.commerce.toolset.flexibleSearch.transform.context.FxSTransformationRequest
import sap.commerce.toolset.flexibleSearch.transform.impex.ImpExHeaderBuilder.resolveEnumPks
import sap.commerce.toolset.flexibleSearch.transform.impex.ImpExHeaderBuilder.resolveFkPks
import sap.commerce.toolset.flexibleSearch.transform.impex.context.ImpExHeaderParameter
import sap.commerce.toolset.typeSystem.TSConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaCollection
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaEnum
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.model.Cardinality
import sap.commerce.toolset.typeSystem.model.PersistenceType

/**
 * Builds type-system-aware ImpEx header parameters for a [FxSQueryInfo].
 *
 * Resolution strategy per attribute meta-type:
 * - Primitive / java.lang.* / String / Boolean → plain parameter
 * - Enum  → `attrName(code)` so ImpEx resolves the enum value by its code attribute
 * - Collection → `attrName(pk)` (HAC values cleaned from serialization artifacts; each element resolved by pk)
 * - ComposedType (item FK), unique → `attrName(naturalKeyPath)` resolved by [FxSNaturalKeyResolver]; PK resolved via follow-up query
 * - ComposedType (item FK), non-unique → `attrName(pk)` (raw HAC PK value; no follow-up needed)
 * - Localized → adds `lang=xx` modifier
 * - Dynamic attribute → skipped (cannot be imported via ImpEx)
 */
object ImpExHeaderBuilder {

    fun buildParams(queryInfo: FxSQueryInfo, project: Project): List<ImpExHeaderParameter> {
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
                val joinNaturalKey = queryInfo.joinNaturalKeyByAttr[col.attributeName.lowercase()]
                resolveParam(col, attrType, isDynamic, tsAccess, queryInfo.uniqueAttributeNames, joinNaturalKey)
            }
    }

    /**
     * Builds ImpEx params for JOIN-resolved unique attributes that are absent from the SELECT list.
     *
     * Each [FxSJoinUniqueColumn] becomes a param with `[unique=true]` and the nested path set to
     * the [FxSJoinUniqueColumn.naturalKeyAttr] specified in the WHERE condition.
     */
    fun buildJoinUniqueParams(queryInfo: FxSQueryInfo, project: Project): List<ImpExHeaderParameter> {
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
            ImpExHeaderParameter(
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
    fun enumSourceIndicesByType(context: FxSTransformationRequest): Map<Int, String> =
        context.queryInfo.columns
            .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }
            .zip(context.params)
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
    fun fkSourceIndicesByResolutionInfo(context: FxSTransformationRequest): Map<Int, FkResolutionInfo> =
        context.queryInfo.columns
            .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }
            .zip(context.params)
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
     * (colon = default ImpEx path-delimiter). Components appear in depth-first order of the
     * nested path — matching how ImpEx distributes a `:`-delimited value across nested leaves.
     *
     * Returns `null` when [nestedPath] is `"pk"` (no meaningful natural key to resolve).
     *
     * [attrTypes] maps lowercase FK attribute names of [typeName] to their SAP Commerce type names.
     * Only FK attributes that have parens in [nestedPath] need entries; scalar attributes are
     * emitted directly. An absent entry causes the FK attribute to be treated as a scalar.
     * [attrTypesLookup] provides the same map for nested types, enabling multi-level paths
     * (e.g. `catalogVersion(catalog(id),version)` on `Product`); when it returns an empty map,
     * deeper FK tokens fall back to scalars.
     *
     * ### Examples
     * - `("Language", "isocode", {})` → `"SELECT {pk}, {isocode} FROM {Language}"`
     * - `("CatalogVersion", "catalog(id),version", {"catalog" → "Catalog"})` →
     *   `"SELECT {root.pk}, {j0.id}, {root.version} FROM {CatalogVersion AS root JOIN Catalog AS j0 ON {j0.pk} = {root.catalog}}"`
     * - `("Product", "code,catalogVersion(catalog(id),version)", {"catalogversion" → "CatalogVersion"}, lookup)` →
     *   `"SELECT {root.pk}, {root.code}, {j1.id}, {j0.version} FROM {Product AS root JOIN CatalogVersion AS j0 ON {j0.pk} = {root.catalogVersion} JOIN Catalog AS j1 ON {j1.pk} = {j0.catalog}}"`
     */
    internal fun buildFkLookupQuery(
        typeName: String,
        nestedPath: String,
        attrTypes: Map<String, String>,
        attrTypesLookup: (String) -> Map<String, String> = { emptyMap() },
    ): String? {
        if (nestedPath == TSConstants.Attribute.PK) return null
        val tokens = splitTopLevel(nestedPath)
        val hasJoins = tokens.any { '(' in it }

        if (!hasJoins) {
            val selectCols = tokens.joinToString(", ") { "{$it}" }
            return "SELECT {${TSConstants.Attribute.PK}}, $selectCols FROM {$typeName}"
        }

        // Build query with JOIN clauses for FK tokens, recursing into nested paths
        val selectParts = mutableListOf<String>()
        val joinParts = mutableListOf<String>()
        var joinCount = 0

        fun emit(tokens: List<String>, ownerAlias: String, ownerAttrTypes: Map<String, String>) {
            tokens.forEach { token ->
                val parenIdx = token.indexOf('(')
                if (parenIdx < 0) {
                    // Scalar attribute directly on the owning type
                    selectParts += "{$ownerAlias.$token}"
                } else {
                    val fkAttr = token.substring(0, parenIdx)
                    val innerPath = token.substring(parenIdx + 1, token.length - 1)
                    val joinTypeName = ownerAttrTypes[fkAttr.lowercase()]
                    if (joinTypeName == null) {
                        // Type unknown — emit as scalar (best-effort fallback)
                        selectParts += "{$ownerAlias.$fkAttr}"
                    } else {
                        val alias = "j${joinCount++}"
                        joinParts += "$joinTypeName AS $alias ON {$alias.${TSConstants.Attribute.PK}} = {$ownerAlias.$fkAttr}"
                        emit(splitTopLevel(innerPath), alias, attrTypesLookup(joinTypeName))
                    }
                }
            }
        }
        emit(tokens, "root", attrTypes)

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
    // Type-system unique attribute discovery
    // -------------------------------------------------------------------------

    /**
     * Returns the lowercase attribute names that are declared `unique=true` in the type system
     * for [typeName] — independently of what appears in the FlexibleSearch WHERE clause.
     *
     * Sources (all are checked):
     * - `allAttributes` with `modifiers.isUnique == true` (excludes dynamic attrs)
     * - `allRelationEnds` with `modifiers.isUnique == true` and `cardinality == ONE`
     * - `allIndexes` with `isUnique == true` → all keys in those indexes
     */
    fun typeSystemUniqueAttributeNames(typeName: String, project: Project): Set<String> {
        val meta = TSMetaModelAccess.getInstance(project).findMetaItemByName(typeName) ?: return emptySet()

        val result = mutableSetOf<String>()

        meta.allAttributes.values
            .filter { it.modifiers.isUnique }
            .mapTo(result) { it.name.lowercase() }

        meta.allRelationEnds
            .filter { it.modifiers.isUnique && it.cardinality == Cardinality.ONE }
            .mapNotNullTo(result) { it.qualifier?.lowercase() }

        meta.allIndexes
            .filter { it.isUnique }
            .flatMapTo(result) { idx -> idx.keys.map { it.lowercase() } }

        return result
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
        joinNaturalKey: String? = null,
    ): ImpExHeaderParameter {
        val modifiers = mutableListOf<String>()
        // uniqueAttributeNames is stored lowercase; compare case-insensitively
        if (col.attributeName.lowercase() in uniqueAttributeNames) modifiers += "unique=true"
        if (col.isLocalized && col.langCode != null) modifiers += "lang=${col.langCode}"

        // Dynamic attributes cannot be imported — mark them as virtual
        if (isDynamic) {
            modifiers += "virtual=true"
            return ImpExHeaderParameter(col.attributeName, modifiers = modifiers, metaType = FxSAttributeMetaType.UNKNOWN)
        }

        if (attrType == null) {
            // Unknown attribute — fall back to plain parameter, type not determinable
            return ImpExHeaderParameter(col.attributeName, modifiers = modifiers, metaType = FxSAttributeMetaType.UNKNOWN)
        }

        return when (val meta = tsAccess.findMetaClassifierByName(attrType)) {
            is TSGlobalMetaItem -> {
                if ("unique=true" in modifiers) {
                    // Prefer the natural key attr(s) from the WHERE JOIN condition when available —
                    // the query only constrains those attrs, so they uniquely identify the item in
                    // this context. Fall back to the full type-system composite key otherwise.
                    // Attr types are always needed: a JOIN-derived path may itself contain nested
                    // FK tokens (e.g. `catalog(id),version` from a multi-level JOIN chain).
                    val naturalPath = joinNaturalKey ?: FxSNaturalKeyResolver.resolve(meta, tsAccess)
                    val fkAttrTypes = FxSNaturalKeyResolver.buildAttrTypes(meta)
                    val fkResolutionInfo = buildFkLookupQuery(attrType, naturalPath, fkAttrTypes) { nestedTypeName ->
                        tsAccess.findMetaItemByName(nestedTypeName)
                            ?.let(FxSNaturalKeyResolver::buildAttrTypes)
                            ?: emptyMap()
                    }
                        ?.let { FkResolutionInfo(attrType, it) }
                    ImpExHeaderParameter(
                        attributeName = col.attributeName,
                        nestedPath = naturalPath,
                        modifiers = modifiers,
                        attributeType = attrType,
                        metaType = FxSAttributeMetaType.ITEM,
                        fkResolutionInfo = fkResolutionInfo
                    )
                } else {
                    // Non-unique FK column: the HAC result contains a raw PK.
                    // Use attrName(pk) — ImpEx resolves it directly; no follow-up query needed.
                    ImpExHeaderParameter(
                        attributeName = col.attributeName,
                        nestedPath = TSConstants.Attribute.PK,
                        modifiers = modifiers,
                        attributeType = attrType,
                        metaType = FxSAttributeMetaType.ITEM
                    )
                }
            }

            is TSGlobalMetaCollection -> {
                // Collection — attrName(pk): each element is resolved by PK
                ImpExHeaderParameter(
                    attributeName = col.attributeName,
                    nestedPath = TSConstants.Attribute.PK,
                    modifiers = modifiers,
                    attributeType = attrType,
                    metaType = FxSAttributeMetaType.COLLECTION
                )
            }

            is TSGlobalMetaEnum -> {
                // Enum — reference by code: attrName(code) so ImpEx resolves the enum value by its code.
                // HAC returns the PK of the HybrisEnumerationValue item; callers must resolve it via
                // a follow-up SELECT {pk}, {code} FROM {EnumType} query.
                ImpExHeaderParameter(
                    attributeName = col.attributeName,
                    nestedPath = "code",
                    modifiers = modifiers,
                    attributeType = attrType,
                    metaType = FxSAttributeMetaType.ENUM
                )
            }

            else -> {
                // Primitive, atomic, map, or unknown — plain parameter
                ImpExHeaderParameter(
                    attributeName = col.attributeName,
                    modifiers = modifiers,
                    attributeType = attrType,
                    metaType = FxSAttributeMetaType.ATOMIC
                )
            }
        }
    }

}
