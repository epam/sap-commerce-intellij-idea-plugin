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

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap
import com.intellij.idea.plugin.hybris.type.system.model.Attribute
import com.intellij.idea.plugin.hybris.type.system.model.CreationMode
import com.intellij.idea.plugin.hybris.type.system.model.Index
import com.intellij.idea.plugin.hybris.type.system.model.ItemType

interface TSMetaItem : TSMetaClassifier<ItemType>, TSMetaSelfMerge<TSMetaItem> {
    val extendedMetaItemName: String?
    val attributes: NoCaseMultiMap<TSMetaItemAttribute>
    val customProperties: NoCaseMultiMap<TSMetaCustomProperty>
    val indexes: NoCaseMultiMap<TSMetaItemIndex>
    val deployment: TSMetaDeployment<TSMetaItem>
    val description: String?
    val jaloClass: String?
    val isAbstract: Boolean
    val isAutoCreate: Boolean
    val isGenerate: Boolean
    val isSingleton: Boolean
    val isJaloOnly: Boolean
    fun addAttribute(key: String, attribute: TSMetaItemAttribute)
    fun addCustomProperty(key: String, customProperty: TSMetaCustomProperty)
    fun addIndex(key: String, index: TSMetaItemIndex)
    fun retrieveAllDoms(): List<ItemType>

    interface TSMetaItemIndex : TSMetaClassifier<Index> {
        val keys: Set<String>
        val creationMode: CreationMode?
        val owner: TSMetaItem?
        val isRemove: Boolean
        val isReplace: Boolean
        val isUnique: Boolean
    }

    interface TSMetaItemAttribute : TSMetaClassifier<Attribute> {
        val customProperties: NoCaseMultiMap<TSMetaCustomProperty>
        val description: String?
        val defaultValue: String?
        val type: String?
        val owner: TSMetaItem
        val isDeprecated: Boolean
        val isAutoCreate: Boolean
        val isRedeclare: Boolean
        val isGenerate: Boolean
        fun addCustomProperty(key: String, customProperty: TSMetaCustomProperty)
    }

    companion object {
        const val IMPLICIT_SUPER_CLASS_NAME = "GenericItem"
    }
}