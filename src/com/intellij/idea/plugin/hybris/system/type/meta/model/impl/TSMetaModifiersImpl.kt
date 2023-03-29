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

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaModifiers
import com.intellij.idea.plugin.hybris.system.type.model.Modifiers
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaModifiersImpl(
    dom: Modifiers,
    override val module: Module,
    override var isCustom: Boolean
) : TSMetaModifiers {

    override val domAnchor: DomAnchor<Modifiers> = DomService.getInstance().createAnchor(dom)
    override val isRead = dom.read.value
    override val isWrite = dom.write.value
    override val isSearch = dom.search.value
    override val isOptional = dom.optional.value
    override val isPrivate = dom.partOf.value
    override val isInitial = dom.partOf.value
    override val isRemovable = dom.removable.value
    override val isPartOf = dom.partOf.value
    override val isUnique = dom.unique.value
    override val isDoNotOptimize = dom.doNotOptimize.value
    override val isEncrypted = dom.encrypted.value

    override fun toString() = "Modifiers(module=$module, isCustom=$isCustom)"
}
