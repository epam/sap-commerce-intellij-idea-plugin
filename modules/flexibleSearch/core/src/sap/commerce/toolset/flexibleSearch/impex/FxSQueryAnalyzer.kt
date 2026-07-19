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
import com.intellij.psi.util.childrenOfType
import com.intellij.util.asSafely
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchColumnRefExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchColumnRefYExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchDefinedTableName
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchEquivalenceExpression
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchFromClause
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchFromClauseSelect
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
 * Analyzed result of a FlexibleSearch query for ImpEx conversion.
 *
 * @param primaryType            The primary item type from the FROM clause.
 * @param columns                Ordered list of columns matching the HAC result headers.
 * @param uniqueAttributeNames   Attribute names that appear in equality conditions in the WHERE clause.
 */
data class FxSQueryInfo(
    val primaryType: String,
    val columns: List<FxSColumn>,
    val uniqueAttributeNames: Set<String>,
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

        return FxSQueryInfo(
            primaryType = primaryType,
            columns = columns,
            uniqueAttributeNames = uniqueAttributeNames,
        )
    }

    /**
     * Correlates HAC result headers with PSI result columns.
     *
     * HAC returns headers in the same order as the SELECT list. When the counts match, we zip them.
     * If they differ (e.g. `SELECT *`), we fall back to using header names directly.
     */
    private fun correlateColumns(headers: List<String>, psiColumns: List<FlexibleSearchResultColumn>): List<FxSColumn> {
        if (psiColumns.isEmpty() || psiColumns.size != headers.size) {
            // Fallback: no PSI info, create bare columns from header names
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
     * Builds a map of JOIN table alias → root-type FK attribute name by parsing ON conditions.
     *
     * For `JOIN SolrIndexedType AS t0 ON {t0.pk} = {t.solrIndexedType}` produces `"t0" → "solrIndexedType"`.
     * Used by [collectUniqueAttributes] to resolve `{t0.identifier}` → unique attr `solrIndexedType`.
     */
    private fun buildJoinAliasMap(fromClause: FlexibleSearchFromClause?): Map<String, String> {
        if (fromClause == null) return emptyMap()
        val result = mutableMapOf<String, String>()

        fromClause.fromClauseExprList
            .filterIsInstance<FlexibleSearchFromClauseSelect>()
            .forEach { joinExpr ->
                val joinAlias = joinExpr.tableAliasName?.text ?: return@forEach
                val onExpr = joinExpr.joinConstraint?.expression
                    ?.asSafely<FlexibleSearchEquivalenceExpression>() ?: return@forEach
                val exprs = onExpr.expressionList
                if (exprs.size != 2) return@forEach

                val col0 = exprs[0].asSafely<FlexibleSearchColumnRefExpression>() ?: return@forEach
                val col1 = exprs[1].asSafely<FlexibleSearchColumnRefExpression>() ?: return@forEach

                // Identify which side is {joinAlias.pk} and which is {rootAlias.fkAttr}
                val fkCol = when {
                    col0.selectedTableName?.text == joinAlias
                        && col0.columnName?.name.equals("pk", ignoreCase = true) -> col1
                    col1.selectedTableName?.text == joinAlias
                        && col1.columnName?.name.equals("pk", ignoreCase = true) -> col0
                    else -> return@forEach
                }
                val fkAttr = fkCol.columnName?.name ?: return@forEach
                result[joinAlias] = fkAttr
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
     */
    private fun collectUniqueAttributes(
        whereClause: com.intellij.psi.PsiElement,
        joinAliasMap: Map<String, String> = emptyMap(),
    ): Set<String> {
        val uniqueNames = mutableSetOf<String>()

        PsiTreeUtil.findChildrenOfType(whereClause, FlexibleSearchEquivalenceExpression::class.java).forEach { eq ->
            // Skip conditions inside OR expressions — cannot guarantee uniqueness
            if (PsiTreeUtil.getParentOfType(eq, FlexibleSearchOrExpression::class.java) != null) return@forEach

            eq.expressionList.forEach { expr ->
                val colRef = expr.asSafely<FlexibleSearchColumnRefExpression>()
                if (colRef != null) {
                    val alias = colRef.selectedTableName?.text
                    val attrName = colRef.columnName?.name
                    if (attrName != null) {
                        // Resolve JOIN alias to the root-type FK attribute
                        val resolved = if (alias != null) joinAliasMap[alias] ?: attrName else attrName
                        uniqueNames += resolved
                    }
                    return@forEach
                }
                val yColRef = expr.asSafely<FlexibleSearchColumnRefYExpression>()
                if (yColRef != null) {
                    yColRef.yColumnName?.text?.let { uniqueNames += it }
                }
            }
        }

        return uniqueNames
    }
}
