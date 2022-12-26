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

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaMap
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaMap
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaSelfMerge
import com.intellij.idea.plugin.hybris.system.type.model.MapType
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaMapImpl(
    dom: MapType,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean
) : TSMetaMap {

    override val domAnchor: DomAnchor<MapType> = DomService.getInstance().createAnchor(dom)
    override val argumentType = dom.argumentType.stringValue
    override val returnType = dom.returnType.stringValue
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val isRedeclare = java.lang.Boolean.TRUE == dom.redeclare.value

    override fun toString() = "Map(module=$module, name=$name, isCustom=$isCustom)"
}

internal class TSGlobalMetaMapImpl(localMeta: TSMetaMap)
    : TSMetaSelfMerge<MapType, TSMetaMap>(localMeta), TSGlobalMetaMap {

    override val domAnchor = localMeta.domAnchor
    override val module = localMeta.module
    override var argumentType = localMeta.argumentType
    override var returnType = localMeta.returnType
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var isRedeclare = localMeta.isRedeclare

    override fun mergeInternally(localMeta: TSMetaMap) {
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate

        if (localMeta.isRedeclare) {
            isRedeclare = localMeta.isRedeclare
            argumentType = localMeta.argumentType
            returnType = localMeta.returnType
        }
    }

    override fun toString() = "Map(module=$module, name=$name, isCustom=$isCustom)"
}