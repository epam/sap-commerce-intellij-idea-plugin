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

package com.intellij.idea.plugin.hybris.system.bean.util.xml

import com.intellij.idea.plugin.hybris.system.bean.codeInsight.lookup.BSLookupElementFactory
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.ResolvingConverter

class BSEnumClassResolvingConverter : ResolvingConverter<Enum>() {

    override fun toString(element: Enum?, context: ConvertContext?) = element?.clazz?.stringValue

    override fun fromString(name: String?, context: ConvertContext?) = context
        ?.project
        ?.let { BSMetaModelAccess.getInstance(it).findMetaEnumByName(name) }
        ?.retrieveDom()

    override fun getVariants(context: ConvertContext?) = context
        ?.project
        ?.let { BSMetaModelAccess.getInstance(it).getAllEnums() }
        ?.mapNotNull { it.retrieveDom() }
        ?.toMutableList()
        ?: mutableListOf()

    override fun createLookupElement(element: Enum?) = element
        ?.let { BSLookupElementFactory.build(it) }
}