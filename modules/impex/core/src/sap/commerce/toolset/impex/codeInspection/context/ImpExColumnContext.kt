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

package sap.commerce.toolset.impex.codeInspection.context

import javax.swing.Icon

data class ImpExColumnContext(
    val name: String,
    val number: Int,
    val icon: Icon,
    val unique: Boolean,
    val include: Boolean = unique
) {
    fun mutable() = Mutable(
        name = name,
        number = number,
        icon = icon,
        unique = unique,
        include = include,
    )

    data class Mutable(
        val name: String,
        val number: Int,
        val icon: Icon,
        val unique: Boolean,
        var include: Boolean,
    ) {

        fun immutable() = ImpExColumnContext(
            name = name,
            number = number,
            icon = icon,
            unique = unique,
            include = include,
        )
    }
}