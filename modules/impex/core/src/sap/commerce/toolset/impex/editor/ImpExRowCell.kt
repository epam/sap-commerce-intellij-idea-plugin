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
 * A cell is created for each column of the header, the value line may have fewer groups than the header has columns.
 *
 * @property range text range of the value, an empty range right after the field separator for an empty value
 * or at the end of the value line when the group of the column is missing
 * @property missingSeparators number of the field separators missing in the value line to get to the group of the column, `0` if the group exists
 */
data class ImpExRowCell(
    val label: String,
    val range: TextRange,
    val unique: Boolean = false,
    val documentIdDeclaration: Boolean = false,
    val missingSeparators: Int = 0,
)
