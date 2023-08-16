/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.bean.meta.model.*
import com.intellij.util.xml.DomElement
import java.util.*
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.function.Function

object BSMetaHelper {

    fun flattenType(meta: BSMetaProperty) = meta.type
        ?.replace("&lt;", "<")
        ?.replace("&gt;", ">")
        ?.reversed()
        ?.chars()
        ?.mapToObj { codePoint: Int -> Char(codePoint) }
        ?.collect((ClassFlattenNameCollector()))

    fun getShortName(name: String?) = name?.split(".")?.lastOrNull()
    fun getNameWithGeneric(name: String?, generic: String?) = (name ?: "") + (generic?.let { "<$it>" } ?: "")

    fun isDeprecated(it: BSGlobalMetaClassifier<DomElement>) = when (it) {
        is BSMetaEnum -> it.isDeprecated
        is BSMetaBean -> it.isDeprecated
        else -> false
    }

    fun getGenericName(name: String?): String? {
        if (name == null) return null

        val unescapedExtends = getUnescapedName(name)
        val from = unescapedExtends.indexOf('<')
        val to = unescapedExtends.lastIndexOf('>')

        return if (from != -1 && to != -1) unescapedExtends.substring(from + 1, to)
        else null
    }

    fun getBeanName(name: String) = getUnescapedName(name)
        .substringBefore("<")

    fun getAllExtends(metaModel: BSGlobalMetaModel, meta: BSGlobalMetaBean): Set<BSGlobalMetaBean> {
        val tempParents = LinkedHashSet<BSGlobalMetaBean>()
        var metaItem = getExtendsMetaItem(metaModel, meta)

        while (metaItem != null) {
            tempParents.add(metaItem)
            metaItem = getExtendsMetaItem(metaModel, metaItem)
        }
        return Collections.unmodifiableSet(tempParents)
    }

    private fun getExtendsMetaItem(metaModel: BSGlobalMetaModel, meta: BSGlobalMetaBean): BSGlobalMetaBean? {
        val extendsName = meta.extends
            // prevent deadlock when type extends itself
            ?.takeIf { it != meta.name }
            ?: HybrisConstants.BS_TYPE_OBJECT

        return metaModel.getMetaType<BSGlobalMetaBean>(BSMetaType.META_BEAN)[extendsName]
            ?: metaModel.getMetaType<BSGlobalMetaBean>(BSMetaType.META_EVENT)[extendsName]
            ?: metaModel.getMetaType<BSGlobalMetaBean>(BSMetaType.META_WS_BEAN)[extendsName]
    }

    private fun getUnescapedName(name: String) = name
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}

internal class ClassFlattenNameCollector : Collector<Char, MutableList<StringBuilder>, String> {

    var ignoreMode: Boolean = false
    override fun supplier(): Supplier<MutableList<StringBuilder>> {
        return Supplier { ArrayList() }
    }

    override fun accumulator(): BiConsumer<MutableList<StringBuilder>, Char> {
        return BiConsumer { stringBuilders: MutableList<StringBuilder>, c: Char ->
            if (c == ' ' || c == '\n' || c == '.') {
                stringBuilders.add(StringBuilder())
                ignoreMode = true;
            } else if (c == ',' || c == '<') {
                ignoreMode = false;
                val flattenClassName = stringBuilders.last()
                flattenClassName.insert(0, c)
            } else {
                if (!ignoreMode) {
                    if (stringBuilders.isEmpty()) {
                        stringBuilders.add(StringBuilder())
                    }
                    val flattenClassName = stringBuilders.last()
                    flattenClassName.insert(0, c)
                }
            }
        }
    }

    override fun combiner(): BinaryOperator<MutableList<StringBuilder>> {
        return BinaryOperator { left, _ -> left }
    }

    override fun finisher(): Function<MutableList<StringBuilder>, String> {
        return Function { classNames ->
            classNames.map { it.toString() }.filter { it.isNotEmpty() }.reversed().joinToString("")
        }
    }

    override fun characteristics(): Set<Collector.Characteristics> {
        return setOf(Collector.Characteristics.UNORDERED)
    }
}