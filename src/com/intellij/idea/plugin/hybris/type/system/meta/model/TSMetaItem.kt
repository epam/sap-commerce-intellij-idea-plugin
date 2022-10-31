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
package com.intellij.idea.plugin.hybris.type.system.meta.model

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.type.system.model.Attribute
import com.intellij.idea.plugin.hybris.type.system.model.CreationMode
import com.intellij.idea.plugin.hybris.type.system.model.Index
import com.intellij.idea.plugin.hybris.type.system.model.ItemType


interface TSMetaItem : TSMetaClassifier<ItemType> {
    val extendedMetaItemName: String?
    val attributes: Map<String, TSMetaItemAttribute>
    val customProperties: Map<String, TSMetaCustomProperty>
    val indexes: Map<String, TSMetaItemIndex>
    val deployment: TSMetaDeployment
    val description: String?
    val jaloClass: String?
    val isAbstract: Boolean
    val isAutoCreate: Boolean
    val isGenerate: Boolean
    val isSingleton: Boolean
    val isJaloOnly: Boolean
    val isCatalogAware: Boolean

    interface TSMetaItemIndex : TSMetaClassifier<Index> {
        override val name: String
        val keys: Set<String>
        val creationMode: CreationMode?
        val isRemove: Boolean
        val isReplace: Boolean
        val isUnique: Boolean
    }

    interface TSMetaItemAttribute : TSMetaClassifier<Attribute> {
        override val name: String
        val customProperties: Map<String, TSMetaCustomProperty>
        val modifiers: TSMetaModifiers
        val description: String?
        val defaultValue: String?
        val type: String?
        val isSelectionOf: String?
        val metaType: String
            get() = HybrisConstants.TS_ATTRIBUTE_DEFAULT_META_TYPE
        val isDeprecated: Boolean
        val isAutoCreate: Boolean
        val isRedeclare: Boolean
        val isGenerate: Boolean
        val isLocalized: Boolean
    }
}

interface TSGlobalMetaItem : TSMetaItem, TSGlobalMetaClassifier<ItemType> {
    override val declarations: MutableSet<TSMetaItem>
    override val attributes: Map<String, TSGlobalMetaItemAttribute>
    override val indexes: Map<String, TSGlobalMetaItemIndex>
    var allAttributes: List<TSGlobalMetaItemAttribute>?
    var allIndexes: List<TSGlobalMetaItemIndex>?
    var allCustomProperties: List<TSMetaCustomProperty>?
    var allRelationEnds: List<TSMetaRelation.TSMetaRelationElement>?
    var allExtends: Set<TSGlobalMetaItem>?

    interface TSGlobalMetaItemIndex : TSMetaItem.TSMetaItemIndex, TSGlobalMetaClassifier<Index> {
        override val declarations: MutableSet<TSMetaItem.TSMetaItemIndex>
    }

    interface TSGlobalMetaItemAttribute : TSMetaItem.TSMetaItemAttribute, TSGlobalMetaClassifier<Attribute> {
        override val declarations: MutableSet<TSMetaItem.TSMetaItemAttribute>
    }
}
