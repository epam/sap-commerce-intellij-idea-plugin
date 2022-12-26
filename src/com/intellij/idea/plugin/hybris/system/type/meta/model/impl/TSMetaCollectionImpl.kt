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

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaCollection
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaCollection
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaSelfMerge
import com.intellij.idea.plugin.hybris.system.type.model.CollectionType
import com.intellij.idea.plugin.hybris.system.type.model.Type
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaCollectionImpl(
    dom: CollectionType,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean
) : TSMetaCollection {

    override val domAnchor: DomAnchor<CollectionType> = DomService.getInstance().createAnchor(dom)
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val elementType = dom.elementType.stringValue!!
    override val type = dom.type.value ?: Type.COLLECTION

    override fun toString() = "Collection(module=$module, name=$name, isCustom=$isCustom)"
}

internal class TSGlobalMetaCollectionImpl(localMeta: TSMetaCollection)
    : TSMetaSelfMerge<CollectionType, TSMetaCollection>(localMeta), TSGlobalMetaCollection {

    override val domAnchor = localMeta.domAnchor
    override val module = localMeta.module
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var type = localMeta.type
    override var elementType = localMeta.elementType

    override fun mergeInternally(localMeta: TSMetaCollection) {
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate
        if (type != localMeta.type) type = localMeta.type

        elementType = localMeta.elementType
    }

    override fun toString() = "Collection(module=$module, name=$name, isCustom=$isCustom)"

}