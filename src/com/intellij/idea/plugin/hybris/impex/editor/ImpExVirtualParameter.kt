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

package com.intellij.idea.plugin.hybris.impex.editor

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.openapi.util.text.StringUtil

data class ImpExVirtualParameter(
    val name: String,
    val displayName: String = StringUtil.shortenPathWithEllipsis(name.removePrefix("$"), 20),
) {

    private val lazyPresentationValue = ClearableLazyValue.create<String> { this.evaluatePresentationValue() }

    val finalText: String
        get() = "$name = $rawValue"

    val presentationValue: String get() = lazyPresentationValue.get()

    var rawValue: String? = null
        set(value) {
            field = value
            lazyPresentationValue.drop()
        }

    private fun evaluatePresentationValue(): String = rawValue ?: ""

    companion object {
        fun of(macroDeclaration: ImpexMacroDeclaration) = ImpExVirtualParameter(
            name = macroDeclaration.macroNameDec.text,
        ).apply {
            rawValue = getInitialValue(macroDeclaration)
        }

        fun getInitialValue(macroDeclaration: ImpexMacroDeclaration) = macroDeclaration.macroNameDec.resolveValue(mutableSetOf())
    }
}