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
package com.intellij.idea.plugin.hybris.system.bean.meta.model.impl

import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaSelfMerge
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.idea.plugin.hybris.system.bean.model.EnumValue
import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive
import com.intellij.idea.plugin.hybris.util.xml.toBoolean
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class BSMetaEnumImpl(
    dom: Enum,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String?,
    override val isCustom: Boolean,
    override val values: Map<String, BSMetaEnum.BSMetaEnumValue>
) : BSMetaEnum {

    override val domAnchor: DomAnchor<Enum> = DomService.getInstance().createAnchor(dom)
    override val description = dom.description.stringValue
    override val template = dom.template.stringValue
    override val deprecatedSince = dom.deprecatedSince.stringValue
    override val shortName = BSMetaHelper.getShortName(name)
    override val isDeprecated = dom.deprecated.toBoolean()

    override fun toString() = "Enum(module=$extensionName, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"

    internal class BSMetaEnumValueImpl(
        dom: EnumValue,
        override val moduleName: String,
        override val extensionName: String,
        override val isCustom: Boolean,
        override val name: String?
    ) : BSMetaEnum.BSMetaEnumValue {

        override val domAnchor: DomAnchor<EnumValue> = DomService.getInstance().createAnchor(dom)

        override fun toString() = "EnumValue(module=$extensionName, name=$name, isCustom=$isCustom)"
    }
}

internal class BSGlobalMetaEnumImpl(localMeta: BSMetaEnum)
    : BSMetaSelfMerge<Enum, BSMetaEnum>(localMeta), BSGlobalMetaEnum {

    override val values = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, BSMetaEnum.BSMetaEnumValue>()
    override val domAnchor = localMeta.domAnchor
    override val shortName = localMeta.shortName
    override val moduleName = localMeta.moduleName
    override val extensionName = localMeta.extensionName
    override val template = localMeta.template
    override var description = localMeta.description
    override var deprecatedSince = localMeta.deprecatedSince
    override var isDeprecated = localMeta.isDeprecated

    override fun mergeInternally(localMeta: BSMetaEnum) {
        description?:let { description = localMeta.description }

        if (localMeta.isDeprecated) isDeprecated = localMeta.isDeprecated

        localMeta.values.values
            .filterNot { values.contains(it.name) }
            .forEach { values[it.name] = it }
    }

    override fun toString() = "Enum(module=$extensionName, name=$name, isDeprecated=$isDeprecated, isCustom=$isCustom)"
}