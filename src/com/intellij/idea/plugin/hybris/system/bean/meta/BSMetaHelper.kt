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

import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaEnum
import com.intellij.util.xml.DomElement

object BSMetaHelper {

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

    private fun getUnescapedName(name: String) = name
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}