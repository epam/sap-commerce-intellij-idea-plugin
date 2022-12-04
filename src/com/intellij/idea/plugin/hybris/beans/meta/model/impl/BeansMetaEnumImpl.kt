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
package com.intellij.idea.plugin.hybris.beans.meta.model.impl

import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaHelper
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaEnum
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaEnum
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaSelfMerge
import com.intellij.idea.plugin.hybris.beans.model.Enum
import com.intellij.idea.plugin.hybris.beans.model.EnumValue
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class BeansMetaEnumImpl(
    dom: Enum,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean,
    override val values: Map<String, BeansMetaEnum.BeansMetaEnumValue>
) : BeansMetaEnum {

    override val domAnchor: DomAnchor<Enum> = DomService.getInstance().createAnchor(dom)
    override val description = dom.description.stringValue
    override val template = dom.template.stringValue
    override val deprecatedSince = dom.deprecatedSince.stringValue
    override val shortName = BeansMetaHelper.getShortName(name)
    override val isDeprecated = java.lang.Boolean.TRUE == dom.deprecated.value

    override fun toString() = "Enum(module=$module, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"

    internal class BeansMetaEnumValueImpl(
        dom: EnumValue,
        override val module: Module,
        override val isCustom: Boolean,
        override val name: String?
    ) : BeansMetaEnum.BeansMetaEnumValue {

        override val domAnchor: DomAnchor<EnumValue> = DomService.getInstance().createAnchor(dom)

        override fun toString() = "EnumValue(module=$module, name=$name, isCustom=$isCustom)"
    }
}

internal class BeansGlobalMetaEnumImpl(localMeta: BeansMetaEnum)
    : BeansMetaSelfMerge<Enum, BeansMetaEnum>(localMeta), BeansGlobalMetaEnum {

    override val values = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, BeansMetaEnum.BeansMetaEnumValue>()
    override val domAnchor = localMeta.domAnchor
    override val shortName = localMeta.shortName
    override val module = localMeta.module
    override val template = localMeta.template
    override var description = localMeta.description
    override var deprecatedSince = localMeta.deprecatedSince
    override var isDeprecated = localMeta.isDeprecated

    override fun mergeInternally(localMeta: BeansMetaEnum) {
        description?:let { description = localMeta.description }

        if (localMeta.isDeprecated) isDeprecated = localMeta.isDeprecated

        localMeta.values.values
            .filterNot { values.contains(it.name) }
            .forEach { values[it.name] = it }
    }

    override fun toString() = "Enum(module=$module, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"
}