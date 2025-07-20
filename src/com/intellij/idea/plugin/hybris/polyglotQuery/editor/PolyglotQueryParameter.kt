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

package com.intellij.idea.plugin.hybris.polyglotQuery.editor

import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryBindParameter
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.asSafely
import org.apache.commons.lang3.BooleanUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

data class PolyglotQueryParameter(
    val name: String,
    val operand: IElementType? = null,
    private val project: WeakReference<Project>,
    private val rawType: String? = null,
    val displayName: String = StringUtil.shortenPathWithEllipsis(name, 20),
) {

    private val lazySqlValue = ClearableLazyValue.create<String> { this.evaluateSqlValue() }
    private val lazyPresentationValue = ClearableLazyValue.create<String> { this.evaluatePresentationValue() }

    val type: KClass<*> = when (rawType) {
        "boolean", "java.lang.Boolean" -> Boolean::class
        "String", "java.lang.String", "localized:java.lang.String" -> String::class
        "byte", "java.lang.Byte" -> Byte::class
        "short", "java.lang.Short" -> Short::class
        "int", "java.lang.Integer" -> Int::class
        "float", "java.lang.Float" -> Float::class
        "double", "java.lang.Double" -> Double::class
        "long", "java.lang.Long" -> Long::class
        "java.util.Date" -> Date::class

        else -> Any::class
    }

    var rawValue: Any? = null
        set(value) {
            field = value
            lazySqlValue.drop()
            lazyPresentationValue.drop()
        }

    val sqlValue: String get() = lazySqlValue.get()
    val presentationValue: String get() = lazyPresentationValue.get()

    private fun evaluateSqlValue(): String = when (type) {
        Boolean::class -> rawValue?.asSafely<Boolean>()
            ?.let { BooleanUtils.toStringTrueFalse(it) }
            ?: "false"

        Date::class -> rawValue?.asSafely<Date>()?.time
            ?.let { "new java.util.Date($it)" }
            ?: ""

        String::class -> rawValue?.asSafely<String>()
            ?.replace("\"", "\\\"")
            ?.let { "\"$it\"" }
            ?: "\"\""

        else -> rawValue?.asSafely<String>()
            ?.let {
                project.get()?.takeUnless { it.isDisposed }
                    ?.let { TSMetaModelAccess.getInstance(it) }
                    ?.findMetaItemByName(rawType)
                    ?.let { "de.hybris.platform.core.PK.parse(\"$rawValue\")" }
                    ?: it
            }
            ?: ""
    }

    private fun evaluatePresentationValue(): String = when (type) {
        Date::class -> rawValue?.asSafely<Date>()
            ?.let { SimpleDateFormat(DATE_FORMAT).format(it) }
            ?: ""

        else -> sqlValue
    }

    companion object {

        const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"

        fun of(bindParameter: PolyglotQueryBindParameter, currentParameters: Map<String, PolyglotQueryParameter>) = PolyglotQueryParameter(
            name = bindParameter.value,
            operand = bindParameter.operator?.elementType,
            rawType = bindParameter.itemType,
            project = WeakReference(bindParameter.project),
        ).apply {
            rawValue = currentParameters[name]?.rawValue
        }

    }
}