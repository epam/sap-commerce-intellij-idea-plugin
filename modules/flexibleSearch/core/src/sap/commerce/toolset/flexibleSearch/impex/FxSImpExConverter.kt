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

    /**
     * Builds the complete ImpEx text for a single type.
     *
     * Layout:
     * ```
     * INSERT_UPDATE TypeName; param1; param2; joinUniqueParam1
     * ; value1; value2; joinUniqueConstant1
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

        return buildString {
            // Header line: SELECT columns + synthetic JOIN-unique columns appended at the end
            append("INSERT_UPDATE $typeName")
            params.forEach { param -> append("; ${param.render()}") }
            joinUniqueParams.forEach { param -> append("; ${param.render()}") }
            appendLine()

            // Value rows: regular column values, then JOIN-unique constant values
            rows.forEach { row ->
                paramWithSourceIdx.forEach { (srcIdx, param) ->
                    val cell = row.getOrNull(srcIdx) ?: ""
                    val value = if (cell == "null") "" else cell
                    append("; ${param.formatValue(value)}")
                }
                queryInfo.joinUniqueColumns.forEach { joinCol ->
                    append("; ${joinCol.constantValue ?: ""}")
                }
                appendLine()
            }
        }
    }
}
