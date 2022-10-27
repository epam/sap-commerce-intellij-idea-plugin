/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.type.system.meta.model.impl

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaEnum
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaEnum.TSMetaEnumValue
import com.intellij.idea.plugin.hybris.type.system.model.EnumType
import com.intellij.idea.plugin.hybris.type.system.model.EnumValue
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

class TSMetaEnumImpl(
    override val module: Module,
    override val project: Project,
    override val name: String?,
    dom: EnumType,
    override val isCustom: Boolean
) : TSMetaEntityImpl<EnumType>(dom, module, project, isCustom, name), TSMetaEnum {

    private val myAllDoms: MutableSet<DomAnchor<EnumType>> = LinkedHashSet()
    override val values = NoCaseMultiMap<TSMetaEnumValue>()
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val isDynamic = java.lang.Boolean.TRUE == dom.dynamic.value
    override val description = dom.description.stringValue
    override val jaloClass = dom.jaloClass.stringValue

    init {
        myAllDoms.add(DomService.getInstance().createAnchor(dom))
    }

    override fun findValueByName(name: String) = ArrayList(values[name])
    override fun retrieveAllDomsStream() = myAllDoms.mapNotNull { it.retrieveDomElement() }

    override fun merge(another: TSMetaEnum) {
        another.values.values().stream()
            .filter { anotherValue -> anotherValue.name != null }
            .forEach { anotherValue -> values.putValue(anotherValue.name!!, anotherValue) }
        addDomRepresentation(another.retrieveDom()!!)
    }

    private fun addDomRepresentation(anotherDom: EnumType) =
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom))

    class TSMetaEnumValueImpl(
        override val module: Module,
        override val project: Project,
        dom: EnumValue,
        override val isCustom: Boolean,
        override val owner: TSMetaEnum,
        override val name: String
    ) : TSMetaEntityImpl<EnumValue>(dom, module, project, isCustom, name),
        TSMetaEnumValue {

        override val description = dom.description.stringValue

    }
}