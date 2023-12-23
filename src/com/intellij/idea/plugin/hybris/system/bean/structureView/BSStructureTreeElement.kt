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
package com.intellij.idea.plugin.hybris.system.bean.structureView

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.model.*
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue
import com.intellij.util.Function
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomElementNavigationProvider
import com.intellij.util.xml.DomService
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.structure.DomStructureTreeElement

class BSStructureTreeElement(
    stableCopy: DomElement,
    private val myDescriptor: Function<DomElement, DomService.StructureViewMode>,
    private val myNavigationProvider: DomElementNavigationProvider
) : DomStructureTreeElement(stableCopy, myDescriptor, myNavigationProvider) {

    override fun createChildElement(element: DomElement) = BSStructureTreeElement(element, myDescriptor, myNavigationProvider)

    override fun getPresentableText(): String? {
        val resolvedValue = when (val dom = element) {
            is Bean -> resolveText(dom.clazz)?.substringAfterLast(".")
            is Enum -> resolveText(dom.clazz)?.substringAfterLast(".")
            is EnumValue -> dom.stringValue
            is Hint -> resolveText(dom.name)
            is Property -> resolveText(dom.name)

            else -> null
        }

        return resolvedValue ?: super.getPresentableText()
    }

    override fun getLocationString() = when (val dom = element) {
        is Bean -> resolveLocationString(dom.deprecated, dom.deprecatedSince)
        is Enum -> resolveLocationString(dom.deprecated, dom.deprecatedSince)
        is Property -> resolveText(dom.type)
        is Import -> resolveText(dom.type)
        is Description -> dom.stringValue
        is Hint -> dom.stringValue

        else -> null
    }

    override fun getIcon(open: Boolean) = when (element) {
        is Enum -> HybrisIcons.BS_ENUM
        is Bean -> HybrisIcons.BS_BEAN
        is Annotations -> HybrisIcons.TS_ANNOTATION
        is Import -> HybrisIcons.TS_IMPORT
        is EnumValue -> HybrisIcons.BS_ENUM_VALUE
        is Property -> HybrisIcons.BS_PROPERTY
        else -> null
    }

    private fun resolveLocationString(deprecated: FalseAttributeValue, deprecatedSince: GenericAttributeValue<String>) =
        if (deprecated.value) "(deprecated${deprecatedSince.stringValue?.let { " since $it" } ?: ""})"
        else null

    private fun resolveText(attributeValue: GenericAttributeValue<String>?) = attributeValue?.stringValue
}