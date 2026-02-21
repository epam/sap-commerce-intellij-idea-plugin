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

package sap.commerce.toolset.text

import com.intellij.openapi.util.TextRange

fun String.removeRanges(rangesDesc: List<TextRange>): String {
    if (rangesDesc.isEmpty()) return this

    val result = StringBuilder(length)
    var cursor = 0

    for (i in rangesDesc.indices.reversed()) {
        val range = rangesDesc[i]

        if (cursor < range.startOffset) {
            result.append(this, cursor, range.startOffset)
        }
        cursor = range.endOffset
    }

    if (cursor < length) {
        result.append(this, cursor, length)
    }

    return result.toString()
}
