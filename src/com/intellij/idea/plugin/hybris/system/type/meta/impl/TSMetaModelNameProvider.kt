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
package com.intellij.idea.plugin.hybris.system.type.meta.impl

import com.intellij.idea.plugin.hybris.system.type.model.*

object TSMetaModelNameProvider {

    fun extract(dom: ItemType): String? = dom.code.stringValue
    fun extract(dom: EnumType): String? = dom.code.stringValue
    fun extract(dom: CollectionType): String? = dom.code.stringValue
    fun extract(dom: Relation): String? = dom.code.stringValue
    fun extract(dom: AtomicType): String? = dom.clazz.stringValue
    fun extract(dom: MapType): String? = dom.code.stringValue
    fun extract(dom: CustomProperty): String? = dom.name.stringValue
    fun extract(dom: Deployment): String? = dom.table.stringValue
    fun extract(dom: EnumValue): String? = dom.code.stringValue
    fun extract(dom: Index): String? = dom.name.stringValue
    fun extract(dom: Attribute): String? = dom.qualifier.stringValue
    fun extract(dom: Persistence): String? = dom.type.stringValue

}