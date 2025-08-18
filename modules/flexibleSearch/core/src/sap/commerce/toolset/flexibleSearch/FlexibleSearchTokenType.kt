/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.flexibleSearch

import com.intellij.psi.tree.IElementType
import java.util.regex.Pattern

class FlexibleSearchTokenType(debugName: String) : IElementType(debugName, FlexibleSearchLanguage) {

    override fun toString() = super.toString()
        .takeIf { it.isNotBlank() }
        ?.lowercase()
        ?.let { PATTERN.matcher(it).replaceAll(" ") }
        ?.let { "<$it>" }
        ?: super.toString()

    companion object {
        private val PATTERN = Pattern.compile("[_]")
    }
}