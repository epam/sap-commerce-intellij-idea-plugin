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

package com.intellij.idea.plugin.hybris.system.type.meta.model.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaHelper
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaAtomic
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaAtomic
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaSelfMerge
import com.intellij.idea.plugin.hybris.system.type.model.AtomicType
import com.intellij.idea.plugin.hybris.util.xml.toBoolean
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaAtomicImpl(
    dom: AtomicType,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String,
    override var isCustom: Boolean
) : TSMetaAtomic {

    override val domAnchor: DomAnchor<AtomicType> = DomService.getInstance().createAnchor(dom)
    override val isAutoCreate = dom.autoCreate.toBoolean()
    override val isGenerate = dom.generate.toBoolean()
    override val extends = dom.extends.stringValue ?: HybrisConstants.TS_TYPE_OBJECT

    override fun toString() = "Atomic(module=$extensionName, name=$name, isCustom=$isCustom)"
}

internal class TSGlobalMetaAtomicImpl(localMeta: TSMetaAtomic)
    : TSMetaSelfMerge<AtomicType, TSMetaAtomic>(localMeta), TSGlobalMetaAtomic {

    override val domAnchor = localMeta.domAnchor
    override val name = localMeta.name
    override val moduleName = localMeta.moduleName
    override val extensionName = localMeta.extensionName
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var extends = localMeta.extends
    override var flattenType: String? = TSMetaHelper.flattenType(this)

    override fun mergeInternally(localMeta: TSMetaAtomic) {
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate
        if (extends != localMeta.extends) extends = localMeta.extends
    }

    override fun toString() = "Atomic(module=$extensionName, name=$name, isCustom=$isCustom)"

}