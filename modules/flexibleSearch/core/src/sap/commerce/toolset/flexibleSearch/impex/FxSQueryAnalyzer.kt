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

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchColumnRefExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchColumnRefYExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchDefinedTableName
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchEquivalenceExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchFromClause
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchFromClauseSimple
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchOrExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchResultColumn
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchSelectCoreSelect

/**
 * Analyzed representation of a FlexibleSearch SELECT column for ImpEx conversion.
 *
 * @param resultHeaderName  The name as returned in the HAC result headers (alias if present, otherwise column name).
 * @param attributeName     The ImpEx attribute name (the actual attribute, not the alias).
 * @param isPk              True if this column is the `pk` field — should be skipped in ImpEx output.
 * @param isLocalized       True when using localized syntax `{col[en]}`.
 * @param langCode          The language code when `isLocalized` is true (e.g., `en`).
 * @param tableAlias        Table alias when using Y-column syntax `{alias:col}`.
 */
data class FxSColumn(
    val resultHeaderName: String,
    val attributeName: String,
    val isPk: Boolean,
    val isLocalized: Boolean = false,
    val langCode: String? = null,
    val tableAlias: String? = null,
)

/**
 * A JOIN-resolved unique attribute that is NOT present in the SELECT list.
 *
 * Produced when a WHERE clause has `{joinAlias.naturalKeyAttr} = 'value'` and the
 * JOIN's ON condition maps `joinAlias` → `fkAttributeName` on the root type.
 *
 * @param fkAttributeName   The FK attribute on the root type (e.g. `solrIndexedType`).
 * @param naturalKeyAttr    Nested natural key path merged from all WHERE equalities on the FK's
 *                          JOIN chain (e.g. `identifier` or `catalog(id),version`).
 * @param constantValue     `:`-joined literal values from the WHERE clause in the path's depth-first
 *                          leaf order, or null when any value is not statically known (bind parameter).
 */
data class FxSJoinUniqueColumn(
    val fkAttributeName: String,
    val naturalKeyAttr: String,
    val constantValue: String?,
)

/**
 * Analyzed result of a FlexibleSearch query for ImpEx conversion.
 *
 * @param primaryType            The primary item type from the FROM clause.
 * @param columns                Ordered list of columns matching the HAC result headers.
 * @param uniqueAttributeNames   Attribute names that appear in equality conditions in the WHERE clause.
 * @param joinUniqueColumns      JOIN-resolved unique attributes absent from the SELECT list — must be
 *                               appended as synthetic columns in the ImpEx output.
 * @param joinNaturalKeyByAttr   Maps root-type FK attribute name (lowercase) → the nested natural
 *                               key path merged from all WHERE JOIN equality conditions on that FK's
 *                               chain (e.g. `catalog(id),version` for a two-level JOIN).
 *                               Used to override the type-system-derived composite key when the query
 *                               only constrains a subset of the FK type's unique attributes.
 */
data class FxSQueryInfo(
    val primaryType: String,
    val columns: List<FxSColumn>,
    val uniqueAttributeNames: Set<String>,
    val joinUniqueColumns: List<FxSJoinUniqueColumn> = emptyList(),
    val joinNaturalKeyByAttr: Map<String, String> = emptyMap(),
)

/**
 * Analyzes a [FlexibleSearchPsiFile] and produces a [FxSQueryInfo] for ImpEx conversion.
 *
 * Only the outermost (first) SELECT is analyzed; subqueries and UNION members are ignored.
 * Result headers from HAC are correlated with PSI columns by their presentation text.
 */
object FxSQueryAnalyzer {

    fun analyze(psiFile: FlexibleSearchPsiFile, resultHeaders: List<String>): FxSQueryInfo {
        val selectCore = PsiTreeUtil.findChildOfType(psiFile, FlexibleSearchSelectCoreSelect::class.java)

        val primaryType = selectCore?.fromClause
            ?.let { PsiTreeUtil.findChildOfType(it, FlexibleSearchDefinedTableName::class.java) }
            ?.text
            ?: "UnknownType"

        val psiColumns = selectCore?.resultColumns?.resultColumnList ?: emptyList()

        val columns = correlateColumns(resultHeaders, psiColumns)

        val joinAliasMap = buildJoinAliasMap(selectCore?.fromClause)

        val uniqueAttributeNames = selectCore?.whereClause
            ?.let { collectUniqueAttributes(it, joinAliasMap) }
            ?: emptySet()

        // Collect JOIN-unique columns that are NOT already in the SELECT list.
        // Use lowercase keys so that ON-condition casing (e.g. `catalogversion`) matches SELECT
        // casing (e.g. `catalogVersion`) — FlexibleSearch column names are case-insensitive.
        val selectedAttrNames = columns.filterNot { it.isPk }.map { it.attributeName.lowercase() }.toSet()
        val joinUniqueColumns = if (joinAliasMap.isEmpty()) emptyList()
            else selectCore?.whereClause
                ?.let { collectJoinUniqueColumns(it, joinAliasMap, selectedAttrNames) }
                ?: emptyList()

        // Collect the natural key attribute(s) used per JOIN FK attr across ALL WHERE conditions,
        // including those whose FK is already in SELECT. This allows buildParams to use just the
        // queried attribute(s) instead of the full type-system composite key.
        val joinNaturalKeyByAttr = if (joinAliasMap.isEmpty()) emptyMap()
            else selectCore?.whereClause
                ?.let { collectJoinNaturalKeys(it, joinAliasMap) }
                ?: emptyMap()

        return FxSQueryInfo(
            primaryType = primaryType,
            columns = columns,
            uniqueAttributeNames = uniqueAttributeNames,
            joinUniqueColumns = joinUniqueColumns,
            joinNaturalKeyByAttr = joinNaturalKeyByAttr,
        )
    }

    /**
     * Correlates HAC result headers with PSI result columns.
     *
     * HAC returns headers in the same order as the SELECT list. When the counts match, we zip them.
     * If headers are absent (query not yet executed), columns are derived from PSI alone.
     * If counts differ (e.g. `SELECT *`), we fall back to using header names directly.
     */
    private fun correlateColumns(headers: List<String>, psiColumns: List<FlexibleSearchResultColumn>): List<FxSColumn> {
        if (headers.isEmpty() && psiColumns.isNotEmpty()) {
            // No execution result yet — derive columns from PSI without header correlation
            return psiColumns.map { analyzeColumn("", it) }
        }

        if (psiColumns.isEmpty() || psiColumns.size != headers.size) {
            // Size mismatch (e.g. SELECT *): fall back to bare header names
            return headers.map { header ->
                FxSColumn(
                    resultHeaderName = header,
                    attributeName = header,
                    isPk = header.equals("pk", ignoreCase = true),
                )
            }
        }

        return headers.zip(psiColumns).map { (headerName, psiCol) ->
            analyzeColumn(headerName, psiCol)
        }
    }

    private fun analyzeColumn(headerName: String, psiColumn: FlexibleSearchResultColumn): FxSColumn {
        val expression = psiColumn.expression

        // Plain column ref: {col} or {alias.col}
        val columnRef = expression?.asSafely<FlexibleSearchColumnRefExpression>()
        if (columnRef != null) {
            val attrName = columnRef.columnName?.name ?: headerName
            val alias = columnRef.selectedTableName?.text
            return FxSColumn(
                resultHeaderName = headerName,
                attributeName = attrName,
                isPk = attrName.equals("pk", ignoreCase = true),
                tableAlias = alias,
            )
        }

        // Y-column: {alias:col} or {alias:col[en]}
        val yColumnRef = expression?.asSafely<FlexibleSearchColumnRefYExpression>()
        if (yColumnRef != null) {
            val attrName = yColumnRef.yColumnName?.text ?: headerName
            val alias = yColumnRef.selectedTableName?.text
            val localizedName = yColumnRef.columnLocalizedName
            val langCode = localizedName?.text?.removeSurrounding("[", "]")
            return FxSColumn(
                resultHeaderName = headerName,
                attributeName = attrName,
                isPk = attrName.equals("pk", ignoreCase = true),
                isLocalized = localizedName != null,
                langCode = langCode,
                tableAlias = alias,
            )
        }

        // Function, subquery, literal expression — use header name as-is
        return FxSColumn(
            resultHeaderName = headerName,
            attributeName = headerName,
            isPk = headerName.equals("pk", ignoreCase = true),
        )
    }

    /**
     * Resolution of a JOIN table alias against the root type's FK attribute it hangs off.
     *
     * @param rootFkAttr The FK attribute on the root type that starts the JOIN chain.
     * @param pathFromFk Attribute segments traversed *below* the FK type down to this alias,
     *                   in order (empty for a direct join). E.g. for
     *                   `t JOIN CatalogVersion t0 ON {t0.pk}={t.catalogversion} JOIN Catalog t1 ON {t1.pk}={t0.catalog}`,
     *                   alias `t1` has `rootFkAttr=catalogversion`, `pathFromFk=[catalog]`.
     */
    internal data class JoinAliasInfo(
        val rootFkAttr: String,
        val pathFromFk: List<String>,
    )

    /**
     * Builds a map of JOIN table alias → [JoinAliasInfo] by parsing ON conditions.
     *
     * Handles multi-level JOIN chains. For example:
     * ```
     * Product AS t
     *   JOIN CatalogVersion AS t0 ON {t0.pk} = {t.catalogversion}
     *   JOIN Catalog        AS t1 ON {t1.pk} = {t0.catalog}
     * ```
     * produces `"t0" → (catalogversion, [])` **and** `"t1" → (catalogversion, [catalog])` — `t1` is
     * reachable from root via `t0`, so the root-level FK for the chain is `catalogversion` and the
     * path below the FK type is `catalog`.
     *
     * Algorithm:
     * 1. First pass — build a raw chain: `joinAlias → (parentAlias, attrOnParent)`.
     * 2. Second pass — for each join alias, walk up the chain until the parent is NOT itself
     *    a join target (i.e., it is the root or an external alias). The last `attrOnParent`
     *    encountered before reaching root is the FK attribute on the root type; the earlier
     *    ones (reversed) form the path below the FK type.
     */
    private fun buildJoinAliasMap(fromClause: FlexibleSearchFromClause?): Map<String, JoinAliasInfo> {
        if (fromClause == null) return emptyMap()
        val simple = PsiTreeUtil.findChildOfType(fromClause, FlexibleSearchFromClauseSimple::class.java)
            ?: return emptyMap()

        val tables = simple.tableOrSubqueryList
        val constraints = simple.joinConstraintList
        if (tables.size < 2 || constraints.isEmpty()) return emptyMap()

        // --- Pass 1: raw chain joinAlias → (parentAlias, attrOnParent) ---
        val rawChain = mutableMapOf<String, Pair<String, String>>()
        constraints.forEachIndexed { idx, constraint ->
            val joinAlias = tables.getOrNull(idx + 1)?.fromTable?.tableAliasName?.text ?: return@forEachIndexed
            val onExpr = constraint.expression
                ?.asSafely<FlexibleSearchEquivalenceExpression>() ?: return@forEachIndexed
            val exprs = onExpr.expressionList
            if (exprs.size != 2) return@forEachIndexed

            val col0 = exprs[0].asSafely<FlexibleSearchColumnRefYExpression>() ?: return@forEachIndexed
            val col1 = exprs[1].asSafely<FlexibleSearchColumnRefYExpression>() ?: return@forEachIndexed

            // Identify {joinAlias.pk} and {someAlias.fkAttr}
            val fkCol = when {
                col0.selectedTableName?.text == joinAlias
                    && col0.yColumnName?.text.equals("pk", ignoreCase = true) -> col1
                col1.selectedTableName?.text == joinAlias
                    && col1.yColumnName?.text.equals("pk", ignoreCase = true) -> col0
                else -> return@forEachIndexed
            }
            val parentAlias = fkCol.selectedTableName?.text ?: return@forEachIndexed
            val attrOnParent = fkCol.yColumnName?.text ?: return@forEachIndexed
            rawChain[joinAlias] = parentAlias to attrOnParent
        }

        if (rawChain.isEmpty()) return emptyMap()

        // --- Pass 2: resolve each alias to its root-level FK attr + path below the FK ---
        // Walk up the chain until the parent is NOT itself a join target (= it is root).
        val result = mutableMapOf<String, JoinAliasInfo>()
        for (joinAlias in rawChain.keys) {
            var current = joinAlias
            val segmentsBottomUp = mutableListOf<String>()
            val visited = mutableSetOf<String>()
            while (visited.add(current)) {
                val (parentAlias, attrOnParent) = rawChain[current] ?: break
                if (parentAlias !in rawChain) {
                    // parentAlias is the root → attrOnParent is the FK on root; the segments
                    // collected on the way up (reversed) are the path below the FK type
                    result[joinAlias] = JoinAliasInfo(attrOnParent, segmentsBottomUp.reversed())
                    break
                }
                segmentsBottomUp += attrOnParent
                current = parentAlias
            }
        }
        return result
    }

    /**
     * Collects attribute names used in top-level equality conditions in the WHERE clause.
     *
     * Skips equality expressions nested inside OR clauses since they cannot guarantee uniqueness.
     * Handles both plain `{col} = ?x` and Y-column `{alias:col} = ?x` patterns.
     *
     * When [joinAliasMap] is non-empty, aliased columns from JOIN tables (e.g. `{t0.identifier}`)
     * are resolved back to the root-type FK attribute (e.g. `solrIndexedType`) via the map.
     *
     * All returned names are **lowercase** — FlexibleSearch attribute names are case-insensitive
     * and ON-condition text may use different casing than the SELECT list.
     */
    private fun collectUniqueAttributes(
        whereClause: com.intellij.psi.PsiElement,
        joinAliasMap: Map<String, JoinAliasInfo> = emptyMap(),
    ): Set<String> {
        val uniqueNames = mutableSetOf<String>()

        PsiTreeUtil.findChildrenOfType(whereClause, FlexibleSearchEquivalenceExpression::class.java).forEach { eq ->
            // Skip conditions inside OR expressions — cannot guarantee uniqueness
            if (PsiTreeUtil.getParentOfType(eq, FlexibleSearchOrExpression::class.java) != null) return@forEach

            eq.expressionList.forEach { expr ->
                // All {alias.col} and {alias:col} refs inside braces are FlexibleSearchColumnRefYExpression
                val yColRef = expr.asSafely<FlexibleSearchColumnRefYExpression>()
                if (yColRef != null) {
                    val alias = yColRef.selectedTableName?.text
                    val attrName = yColRef.yColumnName?.text
                    if (attrName != null) {
                        // Resolve JOIN alias to the root-type FK attribute; normalize to lowercase
                        val resolved = if (alias != null) joinAliasMap[alias]?.rootFkAttr ?: attrName else attrName
                        uniqueNames += resolved.lowercase()
                    }
                    return@forEach
                }
                // Bare col or alias.col without braces (rare in FlexibleSearch)
                val colRef = expr.asSafely<FlexibleSearchColumnRefExpression>()
                if (colRef != null) {
                    val alias = colRef.selectedTableName?.text
                    val attrName = colRef.columnName?.name
                    if (attrName != null) {
                        val resolved = if (alias != null) joinAliasMap[alias]?.rootFkAttr ?: attrName else attrName
                        uniqueNames += resolved.lowercase()
                    }
                }
            }
        }

        return uniqueNames
    }

    /**
     * Collects the natural key path used per JOIN FK attribute across all top-level
     * WHERE equality conditions, regardless of whether the FK appears in the SELECT list.
     *
     * Returns a map of root-type FK attribute name (lowercase) → nested natural key path.
     * Conditions on aliases deeper in the JOIN chain contribute *nested* segments relative to
     * the FK type, and sibling paths are merged.
     *
     * Example: with `t0 → CatalogVersion` (`{t.catalogversion}`) and `t1 → Catalog`
     * (`{t0.catalog}`), the conditions `{t1.id} = 'x'` and `{t0.version} = 'y'` produce
     * `{"catalogversion" → "catalog(id),version"}`.
     */
    internal fun collectJoinNaturalKeys(
        whereClause: com.intellij.psi.PsiElement,
        joinAliasMap: Map<String, JoinAliasInfo>,
    ): Map<String, String> {
        val pathsByFk = mutableMapOf<String, MutableList<List<String>>>()

        PsiTreeUtil.findChildrenOfType(whereClause, FlexibleSearchEquivalenceExpression::class.java).forEach { eq ->
            if (PsiTreeUtil.getParentOfType(eq, FlexibleSearchOrExpression::class.java) != null) return@forEach
            val exprs = eq.expressionList
            if (exprs.size != 2) return@forEach

            for (i in 0..1) {
                val colRef = exprs[i].asSafely<FlexibleSearchColumnRefYExpression>() ?: continue
                val alias = colRef.selectedTableName?.text ?: continue
                val info = joinAliasMap[alias] ?: continue
                val naturalKeyAttr = colRef.yColumnName?.text ?: continue
                pathsByFk.getOrPut(info.rootFkAttr.lowercase()) { mutableListOf() } += info.pathFromFk + naturalKeyAttr
                break
            }
        }

        return pathsByFk.mapValues { (_, paths) -> mergeNaturalKeyPaths(paths) }
    }

    /**
     * Merges attribute [paths] (each a list of segments relative to the FK type, leaf last)
     * into a single nested ImpEx parameter path, preserving first-appearance order.
     *
     * Example: `[[catalog, id], [version]]` → `"catalog(id),version"`.
     */
    internal fun mergeNaturalKeyPaths(paths: List<List<String>>): String {
        val root = PathNode()
        paths.forEach { segments ->
            var node = root
            segments.forEach { node = node.children.getOrPut(it) { PathNode() } }
        }
        return root.render()
    }

    /** Mutable trie of natural key path segments; leaves may carry a WHERE constant value. */
    private class PathNode {
        val children = LinkedHashMap<String, PathNode>()
        var constantValue: String? = null

        fun render(): String = children.entries.joinToString(",") { (name, child) ->
            if (child.children.isEmpty()) name else "$name(${child.render()})"
        }

        /** Depth-first leaf values — same order as the leaves appear in [render]. */
        fun leafValues(): List<String?> = children.values.flatMap { child ->
            if (child.children.isEmpty()) listOf(child.constantValue) else child.leafValues()
        }
    }

    /**
     * Collects JOIN-resolved unique attributes whose values come from WHERE clause literal equality
     * conditions but whose FK column is NOT in the SELECT list.
     *
     * Example: `{t0.identifier} = 'mcProductType'` with `t0 → (solrIndexedType, [])` in [joinAliasMap]
     * produces `FxSJoinUniqueColumn(fkAttributeName="solrIndexedType", naturalKeyAttr="identifier", constantValue="mcProductType")`.
     *
     * All conditions on the same FK's JOIN chain are merged into one nested path; their values are
     * `:`-joined in the path's depth-first leaf order (`catalog(id),version` → `'x:y'`), or null
     * when any of them is not statically known. Duplicate conditions on the same leaf keep the
     * first value. Only top-level AND conditions are considered (OR conditions are skipped).
     * Attributes already present in [excludeAttrNames] (i.e. already in SELECT) are skipped.
     */
    private fun collectJoinUniqueColumns(
        whereClause: com.intellij.psi.PsiElement,
        joinAliasMap: Map<String, JoinAliasInfo>,
        excludeAttrNames: Set<String>,
    ): List<FxSJoinUniqueColumn> {
        // FK attr (lowercase) → (original-case FK attr, path trie with constant values at leaves)
        val treesByFk = LinkedHashMap<String, Pair<String, PathNode>>()

        PsiTreeUtil.findChildrenOfType(whereClause, FlexibleSearchEquivalenceExpression::class.java).forEach { eq ->
            if (PsiTreeUtil.getParentOfType(eq, FlexibleSearchOrExpression::class.java) != null) return@forEach
            val exprs = eq.expressionList
            if (exprs.size != 2) return@forEach

            for (i in 0..1) {
                val colRef = exprs[i].asSafely<FlexibleSearchColumnRefYExpression>() ?: continue
                val alias = colRef.selectedTableName?.text ?: continue
                val info = joinAliasMap[alias] ?: continue
                val naturalKeyAttr = colRef.yColumnName?.text ?: continue

                // excludeAttrNames is lowercase; compare case-insensitively
                if (info.rootFkAttr.lowercase() in excludeAttrNames) break

                val otherText = exprs[1 - i].text.trim()
                val constantValue = when {
                    otherText.startsWith("'") && otherText.endsWith("'") -> otherText.removeSurrounding("'")
                    otherText.startsWith("?") -> null  // bind parameter
                    otherText.startsWith("{") -> null  // column ref — value not known statically
                    else -> otherText                  // numeric or unquoted literal
                }

                var node = treesByFk.getOrPut(info.rootFkAttr.lowercase()) { info.rootFkAttr to PathNode() }.second
                (info.pathFromFk + naturalKeyAttr).forEach { node = node.children.getOrPut(it) { PathNode() } }
                if (node.constantValue == null) node.constantValue = constantValue
                break
            }
        }

        return treesByFk.values.map { (fkAttr, tree) ->
            val leafValues = tree.leafValues()
            FxSJoinUniqueColumn(
                fkAttributeName = fkAttr,
                naturalKeyAttr = tree.render(),
                constantValue = if (leafValues.any { it == null }) null else leafValues.joinToString(":"),
            )
        }
    }
}
