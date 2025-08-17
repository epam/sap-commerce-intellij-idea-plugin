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
import sap.commerce.toolset.CaseInsensitiveMap
import sap.commerce.toolset.typeSystem.meta.TSMetaHelper
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaEnum
import sap.commerce.toolset.typeSystem.meta.model.TSMetaEnum
import sap.commerce.toolset.typeSystem.meta.model.TSMetaSelfMerge
import sap.commerce.toolset.typeSystem.model.EnumType
import sap.commerce.toolset.typeSystem.model.EnumValue
import sap.commerce.toolset.xml.toBoolean

internal class TSMetaEnumImpl(
    dom: EnumType,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String?,
    override var isCustom: Boolean,
    override val values: Map<String, TSMetaEnum.TSMetaEnumValue>
) : TSMetaEnum {

    override val domAnchor: DomAnchor<EnumType> = DomService.getInstance().createAnchor(dom)
    override val isAutoCreate = dom.autoCreate.toBoolean()
    override val isGenerate = dom.generate.toBoolean()
    override val isDynamic = dom.dynamic.toBoolean()
    override val description = dom.description.stringValue
    override val jaloClass = dom.jaloClass.stringValue

    override fun toString() = "Enum(module=$extensionName, name=$name, isDynamic=$isDynamic, isCustom=$isCustom)"

    internal class TSMetaEnumValueImpl(
        dom: EnumValue,
        override val moduleName: String,
        override val extensionName: String,
        override var isCustom: Boolean,
        override val name: String
    ) : TSMetaEnum.TSMetaEnumValue {

        override val domAnchor: DomAnchor<EnumValue> = DomService.getInstance().createAnchor(dom)
        override val description = dom.description.stringValue

        override fun toString() = "EnumValue(module=$extensionName, name=$name, isCustom=$isCustom)"
    }

}

internal class TSGlobalMetaEnumImpl(localMeta: TSMetaEnum)
    : TSMetaSelfMerge<EnumType, TSMetaEnum>(localMeta), TSGlobalMetaEnum {

    override val values = CaseInsensitiveMap.CaseInsensitiveConcurrentHashMap<String, TSMetaEnum.TSMetaEnumValue>()
    override val domAnchor = localMeta.domAnchor
    override val moduleName = localMeta.moduleName
    override val extensionName = localMeta.extensionName
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var isDynamic = localMeta.isDynamic
    override var description = localMeta.description
    override var jaloClass = localMeta.jaloClass
    override var flattenType: String? = TSMetaHelper.flattenType(this)

    override fun mergeInternally(localMeta: TSMetaEnum) {
        jaloClass?:let { jaloClass = localMeta.jaloClass }
        description?:let { description = localMeta.description }

        if (localMeta.isDynamic) isDynamic = localMeta.isDynamic
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate

        localMeta.values.values
            .filterNot { values.contains(it.name) }
            .forEach { values[it.name] = it }
    }

    override fun toString() = "Enum(module=$extensionName, name=$name, isDynamic=$isDynamic, isCustom=$isCustom)"
}