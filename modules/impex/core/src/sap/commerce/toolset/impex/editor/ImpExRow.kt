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

package sap.commerce.toolset.impex.editor

import com.intellij.openapi.util.TextRange

/**
 * @property range text range of the whole value line
 * @property contextPrefix macros and the header, which make the value line parse and resolve like in the source file when it is on its own
 */
data class ImpExRow(
    val range: TextRange,
    val headerType: String,
    val subType: String?,
    val cells: List<ImpExRowCell>,
    val contextPrefix: String = "",
) {

    /**
     * The text of the line and the ranges of the cells are intentionally ignored: they follow the edits,
     * while the layout is what requires the fields to be re-created.
     */
    fun hasSameLayoutAs(other: ImpExRow) = range.startOffset == other.range.startOffset
        && headerType == other.headerType
        && subType == other.subType
        && contextPrefix == other.contextPrefix
        && cells.map { it.label } == other.cells.map { it.label }
}
