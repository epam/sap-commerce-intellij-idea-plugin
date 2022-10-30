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

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaEnum
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaEnum.TSMetaEnumValue
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaSelfMerge
import com.intellij.idea.plugin.hybris.type.system.model.EnumType
import com.intellij.idea.plugin.hybris.type.system.model.EnumValue
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaEnumImpl(
    dom: EnumType,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean,
    override val values: Map<String, TSMetaEnumValue>
) : TSMetaEnum {

    override val domAnchor: DomAnchor<EnumType> = DomService.getInstance().createAnchor(dom)
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val isDynamic = java.lang.Boolean.TRUE == dom.dynamic.value
    override val description = dom.description.stringValue
    override val jaloClass = dom.jaloClass.stringValue

    internal class TSMetaEnumValueImpl(
        dom: EnumValue,
        override val module: Module,
        override val isCustom: Boolean,
        override val name: String
    ) : TSMetaEnumValue {

        override val domAnchor: DomAnchor<EnumValue> = DomService.getInstance().createAnchor(dom)
        override val description = dom.description.stringValue
    }

}

internal class TSGlobalMetaEnumImpl(localMeta: TSMetaEnum)
    : TSMetaSelfMerge<EnumType, TSMetaEnum>(localMeta), TSGlobalMetaEnum {

    override val values = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, TSMetaEnumValue>()
    override val domAnchor = localMeta.domAnchor
    override val module = localMeta.module
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var isDynamic = localMeta.isDynamic
    override var description = localMeta.description
    override var jaloClass = localMeta.jaloClass

    override fun mergeInternally(localMeta: TSMetaEnum) {
        jaloClass?.let { jaloClass = localMeta.jaloClass }
        description?.let { description = localMeta.description }

        if (localMeta.isDynamic) isDynamic = localMeta.isDynamic
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate

        localMeta.values.values
            .filterNot { values.contains(it.name) }
            .forEach { values[it.name] = it }
    }
}