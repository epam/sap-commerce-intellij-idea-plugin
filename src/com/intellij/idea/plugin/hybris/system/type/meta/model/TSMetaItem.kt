/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.system.type.meta.model

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.lang.documentation.renderer.hybrisDoc
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.CreationMode
import com.intellij.idea.plugin.hybris.system.type.model.Index
import com.intellij.idea.plugin.hybris.system.type.model.ItemType


interface TSMetaItem : TSMetaClassifier<ItemType> {
    val extendedMetaItemName: String?
    val attributes: Map<String, TSMetaItemAttribute>
    val customProperties: Map<String, TSMetaCustomProperty>
    val indexes: Map<String, TSMetaItemIndex>
    val deployment: TSMetaDeployment?
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
        val includes: Set<String>
        val creationMode: CreationMode?
        val isRemove: Boolean
        val isReplace: Boolean
        val isUnique: Boolean
    }

    interface TSMetaItemAttribute : TSMetaClassifier<Attribute> {
        override val name: String
        val customProperties: Map<String, TSMetaCustomProperty>
        val persistence: TSMetaPersistence
        val modifiers: TSMetaModifiers
        val description: String?
        val defaultValue: String?
        val type: String?
        val isSelectionOf: String?
        val metaType: String
            get() = HybrisConstants.TS_TYPE_ATTRIBUTE_DESCRIPTOR
        val isDeprecated: Boolean
        val isAutoCreate: Boolean
        val isRedeclare: Boolean
        val isGenerate: Boolean
        val isLocalized: Boolean
        val isDynamic: Boolean
    }
}

interface TSGlobalMetaItem : TSMetaItem, TSGlobalMetaClassifier<ItemType>, TSTypedClassifier {
    override val declarations: MutableSet<TSMetaItem>
    override val attributes: Map<String, TSGlobalMetaItemAttribute>
    override val indexes: Map<String, TSGlobalMetaItemIndex>
    val relationEnds: List<TSMetaRelation.TSMetaRelationElement>
    val allAttributes: Map<String, TSGlobalMetaItemAttribute>
    val allOrderingAttributes: Map<String, TSMetaRelation.TSMetaOrderingAttribute>
    val allIndexes: List<TSGlobalMetaItemIndex>
    val allCustomProperties: List<TSMetaCustomProperty>
    val allRelationEnds: List<TSMetaRelation.TSMetaRelationElement>
    val allExtends: Set<TSGlobalMetaItem>

    override fun documentation() = hybrisDoc {
        title("Item type", name ?: "?")
        subHeader(
            modifiersDocumentation(),
            "extends :: ${extendedMetaItemName ?: HybrisConstants.TS_TYPE_GENERIC_ITEM}",
            description ?: "",
        )
        deployment
            ?.documentation()
            ?.let { contentsWithSeparator(it) }
    }.build()

    private fun modifiersDocumentation() = listOfNotNull(
        if (isAbstract) "abstract" else null,
        if (isAutoCreate) "autocreate" else null,
        if (isGenerate) "generate" else null,
        if (isSingleton) "singleton" else null,
        if (isJaloOnly) "jaloonly" else null,
        if (isCatalogAware) "catalogAware" else null
    ).joinToString(",&nbsp;")

    interface TSGlobalMetaItemIndex : TSMetaItem.TSMetaItemIndex, TSGlobalMetaClassifier<Index> {
        override val declarations: MutableSet<TSMetaItem.TSMetaItemIndex>
    }

    interface TSGlobalMetaItemAttribute : TSMetaItem.TSMetaItemAttribute, TSGlobalMetaClassifier<Attribute>, TSTypedClassifier {
        val owner: TSGlobalMetaItem
        override val declarations: MutableSet<TSMetaItem.TSMetaItemAttribute>
    }
}
