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

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaHelper
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaCollection
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaCollection
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaSelfMerge
import com.intellij.idea.plugin.hybris.system.type.model.CollectionType
import com.intellij.idea.plugin.hybris.util.xml.toBoolean
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

internal class TSMetaCollectionImpl(
    dom: CollectionType,
    override val moduleName: String,
    override val extensionName: String,
    override val name: String?,
    override var isCustom: Boolean
) : TSMetaCollection {

    override val domAnchor: DomAnchor<CollectionType> = DomService.getInstance().createAnchor(dom)
    override val isAutoCreate = dom.autoCreate.toBoolean()
    override val isGenerate = dom.generate.toBoolean()
    override val elementType = dom.elementType.stringValue!!
    override val type = dom.type.value

    override fun toString() = "Collection(module=$extensionName, name=$name, isCustom=$isCustom)"
}

internal class TSGlobalMetaCollectionImpl(localMeta: TSMetaCollection)
    : TSMetaSelfMerge<CollectionType, TSMetaCollection>(localMeta), TSGlobalMetaCollection {

    override val domAnchor = localMeta.domAnchor
    override val moduleName = localMeta.moduleName
    override val extensionName = localMeta.extensionName
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var type = localMeta.type
    override var elementType = localMeta.elementType
    override var flattenType: String? = TSMetaHelper.flattenType(this)

    override fun mergeInternally(localMeta: TSMetaCollection) {
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate
        if (type != localMeta.type) type = localMeta.type

        elementType = localMeta.elementType
    }

    override fun toString() = "Collection(module=$extensionName, name=$name, isCustom=$isCustom)"

}