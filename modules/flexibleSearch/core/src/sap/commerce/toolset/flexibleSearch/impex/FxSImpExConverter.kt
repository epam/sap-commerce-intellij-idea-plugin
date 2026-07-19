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

/**
 * Converts a [FxSQueryInfo] + resolved [FxSImpExParam] lists + raw result rows into
 * an ImpEx `INSERT_UPDATE` block.
 *
 * Kept in `core` (no IntelliJ services) so it can be unit-tested without a full IDE environment.
 */
object FxSImpExConverter {

    /** ImpEx special value that instructs the import engine to leave the attribute unchanged. */
    const val IMPEX_IGNORE = "<ignore>"


    /**
     * Builds the complete ImpEx text for a single type.
     *
     * Unique columns (`[unique=true]`) — including JOIN-unique synthetic columns — are placed
     * first in both the header and every value row so that ImpEx can locate existing items
     * before applying the remaining attribute values.
     *
     * Layout:
     * ```
     * INSERT_UPDATE TypeName; uniqueCol1[unique=true]; joinUniqueCol(key)[unique=true]; regularCol
     * ; uniqueVal1; joinUniqueConst; regularVal
     * ```
     *
     * @param typeName          ImpEx type name (e.g. `Product`).
     * @param params            Ordered params for the non-PK SELECT columns.
     * @param joinUniqueParams  Synthetic params for JOIN-unique attributes absent from SELECT.
     * @param queryInfo         Analyzed query — provides column list and join-unique column metadata.
     * @param rows              Raw result rows from HAC (each row is a list of cell strings).
     */
    fun buildImpEx(
        typeName: String,
        params: List<FxSImpExParam>,
        joinUniqueParams: List<FxSImpExParam>,
        queryInfo: FxSQueryInfo,
        rows: List<List<String>>,
    ): String {
        // Pair each non-PK column index in the result row with its resolved param
        val paramWithSourceIdx = queryInfo.columns
            .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }
            .zip(params)

        // Unique columns first, non-unique after — preserving relative order within each group
        val (uniqueWithIdx, nonUniqueWithIdx) = paramWithSourceIdx
            .partition { (_, param) -> "unique=true" in param.modifiers }

        return buildString {
            // Header: unique SELECT cols → JOIN-unique synthetic cols → non-unique SELECT cols
            append("INSERT_UPDATE $typeName")
            uniqueWithIdx.forEach { (_, param) -> append("; ${param.render()}") }
            joinUniqueParams.forEach { param -> append("; ${param.render()}") }
            nonUniqueWithIdx.forEach { (_, param) -> append("; ${param.render()}") }
            appendLine()

            // Rows: same order as header
            rows.forEach { row ->
                uniqueWithIdx.forEach { (srcIdx, param) ->
                    val cell = row.getOrNull(srcIdx) ?: ""
                    val value = if (cell == "null" || cell == IMPEX_IGNORE) "" else cell
                    append("; ${param.formatValue(value).ifEmpty { IMPEX_IGNORE }}")
                }
                queryInfo.joinUniqueColumns.forEach { joinCol ->
                    val v = joinCol.constantValue?.takeIf { it.isNotEmpty() } ?: IMPEX_IGNORE
                    append("; $v")
                }
                nonUniqueWithIdx.forEach { (srcIdx, param) ->
                    val cell = row.getOrNull(srcIdx) ?: ""
                    val value = if (cell == "null" || cell == IMPEX_IGNORE) "" else cell
                    append("; ${param.formatValue(value).ifEmpty { IMPEX_IGNORE }}")
                }
                appendLine()
            }
        }
    }
}
