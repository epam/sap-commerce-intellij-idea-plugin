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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaAtomic
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaAtomic
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaSelfMerge
import com.intellij.idea.plugin.hybris.type.system.model.AtomicType
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaAtomicImpl(
    dom: AtomicType,
    override val module: Module,
    override val name: String,
    override val isCustom: Boolean
) : TSMetaAtomic {

    override val domAnchor: DomAnchor<AtomicType> = DomService.getInstance().createAnchor(dom)
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val extends = dom.extends.stringValue ?: HybrisConstants.TS_TYPE_OBJECT

    override fun toString() = "Atomic(module=$module, name=$name, isCustom=$isCustom)"
}

internal class TSGlobalMetaAtomicImpl(localMeta: TSMetaAtomic)
    : TSMetaSelfMerge<AtomicType, TSMetaAtomic>(localMeta), TSGlobalMetaAtomic {

    override val domAnchor = localMeta.domAnchor
    override val name = localMeta.name
    override val module = localMeta.module
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var extends = localMeta.extends

    override fun mergeInternally(localMeta: TSMetaAtomic) {
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate
        if (extends != localMeta.extends) extends = localMeta.extends
    }

    override fun toString() = "Atomic(module=$module, name=$name, isCustom=$isCustom)"

}