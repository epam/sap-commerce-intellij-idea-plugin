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

package sap.commerce.toolset.impex.editor.rowMode

import com.intellij.psi.SmartPsiElementPointer
import sap.commerce.toolset.impex.psi.ImpExValueLine

internal sealed interface ImpExRowModeState {

    data object NoValueLine : ImpExRowModeState
    data object NoHeader : ImpExRowModeState
    data object MultipleCarets : ImpExRowModeState

    /**
     * @property modificationStamp the stamp of the document the row was resolved for
     */
    data class Row(
        val row: ImpExRow,
        val pointer: SmartPsiElementPointer<ImpExValueLine>,
        val editable: Boolean,
        val columnTypes: List<ImpExRow.ColumnType>,
        val modificationStamp: Long,
    ) : ImpExRowModeState

    fun hasSameLayoutAs(other: ImpExRowModeState) = if (this is Row && other is Row) {
        editable == other.editable
            && columnTypes.map { it.name } == other.columnTypes.map { it.name }
            && row.hasSameLayoutAs(other.row)
    } else this == other
}
