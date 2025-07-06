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

package com.intellij.idea.plugin.hybris.flexibleSearch.editor

import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchBindParameter
import com.intellij.psi.tree.IElementType

data class FlexibleSearchQueryParameter(
    val name: String,
    var value: String = "",
    var presentationValue: String = value,
    val type: String? = null,
    val operand: IElementType? = null
) {
    companion object {
        fun of(bindParameter: FlexibleSearchBindParameter, currentParameters: Collection<FlexibleSearchQueryParameter>): FlexibleSearchQueryParameter {
            val parameter = bindParameter.value
            val currentParameter = currentParameters.find { it.name == parameter }
            val itemType = bindParameter.itemType
            val value = currentParameter?.value ?: resolveInitialValue(itemType)
            val presentationValue = currentParameter?.presentationValue ?: resolveInitialPresentationValue(itemType)

            return FlexibleSearchQueryParameter(parameter, value, presentationValue, type = itemType)
        }

        private fun resolveInitialValue(itemType: String?): String = when (itemType) {
            "boolean", "java.lang.Boolean" -> "0"
            "String", "java.lang.String", "localized:java.lang.String" -> "''"
            else -> ""
        }

        private fun resolveInitialPresentationValue(itemType: String?): String = when (itemType) {
            "boolean", "java.lang.Boolean" -> "false"
            "String", "java.lang.String", "localized:java.lang.String" -> "''"
            else -> ""
        }
    }
}