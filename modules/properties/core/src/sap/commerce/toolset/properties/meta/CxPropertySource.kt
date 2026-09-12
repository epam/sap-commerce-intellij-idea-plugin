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

import com.intellij.openapi.vfs.VirtualFile

/**
 * A single origin of property declarations — in most cases a `.properties` file of the platform property chain.
 *
 * Sources are totally ordered from the lowest to the highest precedence, so the winning declaration of a property is
 * always the one bound to the greatest source.
 *
 * @param name file name of the source, or a synthetic name for [CxPropertyScope.ENVIRONMENT].
 * @param rank position within the [scope]; the extension load order for [CxPropertyScope.PROJECT] and the alphabetical
 * position for [CxPropertyScope.OPTIONAL_CONFIG].
 * @param extension owning SAP Commerce extension, when the source belongs to one.
 * @param path system dependent path of the source, for presentation purposes only.
 * @param file navigation target; never taken into account while resolving a property value.
 */
data class CxPropertySource(
    val name: String,
    val scope: CxPropertyScope,
    val rank: Int = 0,
    val extension: String? = null,
    val path: String? = null,
    val file: VirtualFile? = null,
) : Comparable<CxPropertySource> {

    val presentableName
        get() = extension
            ?.let { "$it/$name" }
            ?: name

    override fun compareTo(other: CxPropertySource) = compareValuesBy(this, other, { it.scope.ordinal }, { it.rank }, { it.name })
}
