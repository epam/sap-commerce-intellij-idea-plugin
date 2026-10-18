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

package sap.commerce.toolset.properties.meta

/**
 * A `${...}` reference inside a property value.
 *
 * @param key the property being referred to, which may well not exist.
 * @param range where the whole `${key}` sits in the value it was read from, so the editor can point at it.
 */
data class CxPropertyPlaceholder(
    val key: String,
    val range: IntRange,
) {

    /** Where the key itself sits, without the `${` and `}` around it. */
    val keyRange
        get() = (range.first + PREFIX.length)..(range.last - SUFFIX.length)

    companion object {
        const val PREFIX = "\${"
        const val SUFFIX = "}"

        /**
         * Every placeholder of [value], in the order they are written.
         *
         * An unterminated `${` is not a placeholder — the platform leaves it alone and so does this.
         */
        fun of(value: String): List<CxPropertyPlaceholder> {
            if (!value.contains(PREFIX)) return emptyList()

            val placeholders = mutableListOf<CxPropertyPlaceholder>()
            var index = 0

            while (index < value.length) {
                val start = value.indexOf(PREFIX, index)
                if (start < 0) break

                val end = value.indexOf(SUFFIX, start + PREFIX.length)
                if (end < 0) break

                placeholders.add(CxPropertyPlaceholder(value.substring(start + PREFIX.length, end), start..end))
                index = end + SUFFIX.length
            }

            return placeholders
        }
    }
}
