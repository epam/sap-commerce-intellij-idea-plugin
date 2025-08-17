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
package sap.commerce.toolset.typeSystem.meta.model.impl

import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService
import sap.commerce.toolset.typeSystem.meta.TSMetaHelper
import sap.commerce.toolset.typeSystem.meta.model.TSMetaPersistence
import sap.commerce.toolset.typeSystem.model.Attribute
import sap.commerce.toolset.typeSystem.model.ItemType
import sap.commerce.toolset.typeSystem.model.Persistence
import sap.commerce.toolset.typeSystem.model.PersistenceType

internal class TSMetaPersistenceImpl(
    itemTypeDom: ItemType,
    attributeDom: Attribute,
    dom: Persistence,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String?,
    override var isCustom: Boolean
) : TSMetaPersistence {

    override val domAnchor: DomAnchor<Persistence> = DomService.getInstance().createAnchor(dom)
    override val type: PersistenceType? = dom.type.value
    override val qualifier: String? = dom.qualifier.stringValue
    override val attributeHandler: String? = TSMetaHelper.getAttributeHandler(itemTypeDom, attributeDom, dom)

    override fun toString() = "Persistence(module=$extensionName, name=$name, isCustom=$isCustom)"
}
