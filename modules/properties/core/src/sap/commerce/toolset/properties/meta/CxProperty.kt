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
 * [declarations] are ordered from the lowest to the highest precedence, hence the last [active][CxPropertySource.active]
 * one wins and every preceding active one is shadowed by it. Declarations of a source the running system does not read
 * are kept apart as [ignoredDeclarations] and never win, however high they would otherwise rank.
 */
data class CxProperty(
    val key: String,
    val declarations: List<CxPropertyDeclaration>,
) {

    /** Declarations the running system actually reads, ordered from the lowest to the highest precedence. */
    val activeDeclarations
        get() = declarations.filter { it.source.active }

    /** Declarations of a source outside of the active system configuration, which the platform never loads. */
    val ignoredDeclarations
        get() = declarations.filterNot { it.source.active }

    /** Declaration the platform would actually apply. */
    val declaration
        get() = activeDeclarations.lastOrNull()

    /** Winning value, with `${...}` placeholders left as they are written. Use [CxPropertyModel.resolve] to expand them. */
    val rawValue
        get() = declaration?.value

    /** Active declarations shadowed by the [declaration], ordered from the lowest to the highest precedence. */
    val shadowedDeclarations
        get() = activeDeclarations.dropLast(1)

    val isShadowed
        get() = activeDeclarations.size > 1

    /** The project declares this property, but only in sources the running system does not read. */
    val isIgnored
        get() = declarations.isNotEmpty() && activeDeclarations.isEmpty()

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
