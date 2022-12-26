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
    override val isCustom: Boolean
) : TSMetaModifiers {

    override val domAnchor: DomAnchor<Modifiers> = DomService.getInstance().createAnchor(dom)
    override val isRead = (dom.read.value ?: true) == java.lang.Boolean.TRUE
    override val isWrite = (dom.write.value ?: true) == java.lang.Boolean.TRUE
    override val isSearch = (dom.search.value ?: true) == java.lang.Boolean.TRUE
    override val isOptional = (dom.optional.value ?: true) == java.lang.Boolean.TRUE
    override val isPrivate = java.lang.Boolean.TRUE == dom.partOf.value
    override val isInitial = java.lang.Boolean.TRUE == dom.partOf.value
    override val isRemovable = (dom.removable.value ?: true) == java.lang.Boolean.TRUE
    override val isPartOf = java.lang.Boolean.TRUE == dom.partOf.value
    override val isUnique = java.lang.Boolean.TRUE == dom.unique.value
    override val isDoNotOptimize = java.lang.Boolean.TRUE == dom.doNotOptimize.value
    override val isEncrypted = java.lang.Boolean.TRUE == dom.encrypted.value

    override fun toString() = "Modifiers(module=$module, isCustom=$isCustom)"
}
