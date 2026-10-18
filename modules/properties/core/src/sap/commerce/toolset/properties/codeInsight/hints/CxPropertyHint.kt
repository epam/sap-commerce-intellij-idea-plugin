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

package sap.commerce.toolset.properties.codeInsight.hints

import sap.commerce.toolset.properties.meta.CxResolvedProperty

/** What a declaration's inlay hint says about it, if anything. */
object CxPropertyHint {

    fun of(resolved: CxResolvedProperty, declaringPath: String, declaredValue: String?) = of(
        winningPath = resolved.source.path,
        winningName = resolved.source.presentableName,
        resolvedValue = resolved.value,
        declaringPath = declaringPath,
        declaredValue = declaredValue,
    )

    /**
     * A declaration another file overrides names the file which wins; one carrying placeholders shows what it expands
     * to; any other declaration already says what it means and gets no hint.
     */
    fun of(winningPath: String?, winningName: String, resolvedValue: String, declaringPath: String, declaredValue: String?) = when {
        winningPath != null && winningPath != declaringPath -> "overridden by $winningName"
        resolvedValue != declaredValue -> "= $resolvedValue"
        else -> null
    }
}
