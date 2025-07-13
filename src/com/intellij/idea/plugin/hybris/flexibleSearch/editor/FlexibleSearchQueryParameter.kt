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
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.asSafely
import java.text.SimpleDateFormat
import java.util.*

data class FlexibleSearchQueryParameter(
    val name: String,
    val type: String? = null,
    val operand: IElementType? = null,
    val displayName: String = StringUtil.shortenPathWithEllipsis(name, 20)
) {

    private val lazySqlValue = ClearableLazyValue.create<String> { this.evaluateSqlValue() }
    private val lazyPresentationValue = ClearableLazyValue.create<String> { this.evaluatePresentationValue() }

    var rawValue: Any? = null
        set(value) {
            field = value
            lazySqlValue.drop()
            lazyPresentationValue.drop()
        }

    val sqlValue: String get() = lazySqlValue.get()
    val presentationValue: String get() = lazyPresentationValue.get()

    private fun evaluateSqlValue(): String = when (type) {
        "boolean", "java.lang.Boolean" -> rawValue?.asSafely<Boolean>()?.takeIf { it }
            ?.let { "1" }
            ?: "0"

        "java.util.Date" -> rawValue?.asSafely<Date>()?.time?.toString()
            ?: ""

        "String", "java.lang.String", "localized:java.lang.String" -> rawValue?.asSafely<String>()
            ?.let { stringValue ->
                if (operand == FlexibleSearchTypes.IN_EXPRESSION) stringValue
                    .split("\n")
                    .filter { it.isNotBlank() }
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(",") { "'$it'" }
                else "'$stringValue'"
            }
            ?: "''"

        "java.lang.Float", "java.lang.Double", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Integer",
        "float", "double", "byte", "short", "long", "int" -> (rawValue?.asSafely<String>() ?: "")
            .let { numberValue ->
                if (operand == FlexibleSearchTypes.IN_EXPRESSION) numberValue
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString(",")
                else numberValue
            }

        else -> rawValue?.asSafely<String>() ?: ""
    }

    private fun evaluatePresentationValue(): String = when (type) {
        "boolean", "java.lang.Boolean" -> if (sqlValue == "1") "true" else "false"

        "String", "java.lang.String", "localized:java.lang.String" -> sqlValue

        "java.lang.Float", "java.lang.Double", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Integer",
        "float", "double", "byte", "short", "long", "int" -> sqlValue

        "java.util.Date" -> rawValue
            ?.asSafely<Date>()
            ?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(it) }
            ?: ""

        else -> sqlValue
    }

    companion object {

        fun of(bindParameter: FlexibleSearchBindParameter, currentParameters: Map<String, FlexibleSearchQueryParameter>) = FlexibleSearchQueryParameter(
            name = bindParameter.value,
            type = bindParameter.itemType,
            operand = bindParameter.expression?.elementType,
        ).apply {
            rawValue = currentParameters[name]?.rawValue
        }

    }
}