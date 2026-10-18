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
 * A property of the chain as the platform would apply it: the fully expanded [value] together with the winning
 * [declaration] it came from, so the declaring file stays reachable, and the whole [property] for the shadowed ones.
 */
data class CxResolvedProperty(
    val property: CxProperty,
    val declaration: CxPropertyDeclaration,
    val value: String,
) {

    val key
        get() = property.key

    val source
        get() = declaration.source

    /** Value as written in the declaring file, differing from [value] only when it carries placeholders. */
    val rawValue
        get() = declaration.value

    companion object {
        fun of(property: CxProperty, value: String) = property.declaration
            ?.let { CxResolvedProperty(property, it, value) }
    }
}
