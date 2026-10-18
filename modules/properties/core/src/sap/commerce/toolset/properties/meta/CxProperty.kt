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
 * A single SAP Commerce property together with every place it is declared in.
 *
 * [declarations] are ordered from the lowest to the highest precedence, hence the last one wins and every preceding
 * one is shadowed by it.
 */
data class CxProperty(
    val key: String,
    val declarations: List<CxPropertyDeclaration>,
) {

    /** Declaration the platform would actually apply. */
    val declaration
        get() = declarations.lastOrNull()

    /** Winning value, with `${...}` placeholders left as they are written. Use [CxPropertyModel.resolve] to expand them. */
    val rawValue
        get() = declaration?.value

    /** Declarations shadowed by the [declaration], ordered from the lowest to the highest precedence. */
    val shadowedDeclarations
        get() = declarations.dropLast(1)

    val isShadowed
        get() = declarations.size > 1

    val sources
        get() = declarations.map { it.source }

    fun valueIn(source: CxPropertySource) = declarations
        .find { it.source == source }
        ?.value

    fun declaredIn(scope: CxPropertyScope) = declarations
        .filter { it.source.scope == scope }

    companion object {
        fun of(key: String, declarations: Collection<CxPropertyDeclaration>) = CxProperty(key, declarations.sortedBy { it.source })
    }
}
