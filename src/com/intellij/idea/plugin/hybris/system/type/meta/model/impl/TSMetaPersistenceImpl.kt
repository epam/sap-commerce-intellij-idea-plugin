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
package com.intellij.idea.plugin.hybris.system.type.meta.model.impl

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaPersistence
import com.intellij.idea.plugin.hybris.system.type.model.Persistence
import com.intellij.idea.plugin.hybris.system.type.model.PersistenceType
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaPersistenceImpl(
    dom: Persistence,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean
) : TSMetaPersistence {

    override val domAnchor: DomAnchor<Persistence> = DomService.getInstance().createAnchor(dom)
    override val type: PersistenceType? = dom.type.value
    override val qualifier: String? = dom.qualifier.stringValue
    override val attributeHandler: String? = dom.attributeHandler.stringValue

    override fun toString() = "Persistence(module=$module, name=$name, isCustom=$isCustom)"
}
