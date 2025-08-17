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
package sap.commerce.toolset.typeSystem.meta.impl

import sap.commerce.toolset.typeSystem.model.*

object TSMetaModelNameProvider {

    fun extract(dom: ItemType): String? = dom.code.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: EnumType): String? = dom.code.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: CollectionType): String? = dom.code.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: Relation): String? = dom.code.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: AtomicType): String? = dom.clazz.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: MapType): String? = dom.code.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: CustomProperty): String? = dom.name.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: Deployment): String? = dom.table.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: EnumValue): String? = dom.code.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: Index): String? = dom.name.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: Attribute): String? = dom.qualifier.stringValue?.takeIf { it.isNotBlank() }
    fun extract(dom: Persistence): String? = dom.type.stringValue?.takeIf { it.isNotBlank() }

}