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

import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaMap
import com.intellij.idea.plugin.hybris.type.system.model.MapType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

class TSMetaMapImpl(override val module: Module, override val project: Project, override val name: String?, dom: MapType, override val isCustom: Boolean)
    : TSMetaEntityImpl<MapType>(dom, module, project, isCustom, name), TSMetaMap {

    override val argumentType = dom.argumentType.stringValue
    override val returnType = dom.returnType.stringValue
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val isRedeclare = java.lang.Boolean.TRUE == dom.redeclare.value
    private val myAllDoms: MutableSet<DomAnchor<MapType>> = LinkedHashSet()

    init {
        myAllDoms.add(DomService.getInstance().createAnchor(dom))
    }

    private fun addDomRepresentation(anotherDom: MapType) {
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom))
    }

    override fun retrieveAllDoms() = myAllDoms.mapNotNull { it.retrieveDomElement() }

    override fun merge(another: TSMetaMap) {
        another.retrieveDom()?.let { addDomRepresentation(it) }
    }

    override fun toString(): String {
        return "TSMetaAtomicImpl(module=$module, name=$name, isCustom=$isCustom)"
    }
}