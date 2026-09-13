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

package sap.commerce.toolset.properties.ui

import sap.commerce.toolset.properties.meta.CxResolvedProperty
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation

/**
 * The other side a listed property is held against, so that a row disagreeing with it can be highlighted and explained.
 *
 * Which side that is depends on the view: the remote instance is held against the project's own files, a project
 * listing against the instance it was fetched from, and the properties of one extension against the value the whole
 * chain ends up resolving to — which is how an overridden declaration shows itself.
 *
 * @param ownLabel names the value the rows themselves carry.
 * @param otherLabel names the value they are compared against.
 * @param declarations provenance of the compared value, when it comes from the project's own files.
 */
internal class CxPropertyCounterpart(
    val ownLabel: String,
    val otherLabel: String,
    private val values: Map<String, String>,
    private val declarations: Map<String, CxResolvedProperty> = emptyMap(),
) {

    fun valueOf(key: String) = values[key]

    fun declarationOf(key: String) = declarations[key]

    /** Value of [property] on the other side, when there is one and it disagrees. */
    fun disagreementWith(property: CxPropertyPresentation) = values[property.key]
        ?.takeIf { it != property.value }

    fun countDisagreements(properties: Collection<CxPropertyPresentation>) = properties
        .count { disagreementWith(it) != null }

    companion object {

        /** Rows carrying a remote value, held against what the project's own files resolve to. */
        fun project(declarations: Map<String, CxResolvedProperty>) = CxPropertyCounterpart(
            ownLabel = "Remote",
            otherLabel = "Project",
            values = declarations.mapValues { (_, resolved) -> resolved.value },
            declarations = declarations,
        )

        /** Rows carrying a project value, held against the remote instance they were fetched from. */
        fun remote(values: Map<String, String>) = CxPropertyCounterpart(
            ownLabel = "Project",
            otherLabel = "Remote",
            values = values,
        )

        /** Rows carrying one instance's value, held against another instance of the same kind. */
        fun instance(ownName: String, otherName: String, values: Map<String, String>) = CxPropertyCounterpart(
            ownLabel = ownName,
            otherLabel = otherName,
            values = values,
        )

        /** Rows carrying the declaration of one extension, held against the value the whole chain resolves to. */
        fun effective(declarations: Map<String, CxResolvedProperty>) = CxPropertyCounterpart(
            ownLabel = "Declared here",
            otherLabel = "Effective",
            values = declarations.mapValues { (_, resolved) -> resolved.value },
            declarations = declarations,
        )
    }
}
