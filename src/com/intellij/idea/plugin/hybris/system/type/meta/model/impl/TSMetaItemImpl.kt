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

import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaHelper
import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive.CaseInsensitiveConcurrentHashMap
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaItem.TSMetaItemAttribute
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaItem.TSMetaItemIndex
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService
import java.util.*

internal class TSMetaItemImpl(
    dom: ItemType,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean,
    override val attributes: Map<String, TSMetaItemAttribute>,
    override val indexes: Map<String, TSMetaItemIndex>,
    override val customProperties: Map<String, TSMetaCustomProperty>,
    override val deployment: TSMetaDeployment?
) : TSMetaItem {

    override val domAnchor: DomAnchor<ItemType> = DomService.getInstance().createAnchor(dom)
    override val isAbstract = java.lang.Boolean.TRUE == dom.abstract.value
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val isSingleton = java.lang.Boolean.TRUE == dom.singleton.value
    override val isJaloOnly = java.lang.Boolean.TRUE == dom.jaloOnly.value
    override val isCatalogAware = TSMetaHelper.isCatalogAware(dom.customProperties)
    override val jaloClass = dom.jaloClass.stringValue
    override val description = dom.description.xmlTag?.value?.text
    override var extendedMetaItemName = dom.extends.stringValue

    override fun toString() = "Item(module=$module, name=$name, isCustom=$isCustom)"

    internal class TSMetaItemIndexImpl(
        dom: Index,
        override val module: Module,
        override val name: String,
        override val isCustom: Boolean
    ) : TSMetaItemIndex {

        override val domAnchor: DomAnchor<Index> = DomService.getInstance().createAnchor(dom)
        override val isRemove = java.lang.Boolean.TRUE == dom.remove.value
        override val isReplace = java.lang.Boolean.TRUE == dom.replace.value
        override val isUnique = java.lang.Boolean.TRUE == dom.unique.value
        override val creationMode = dom.creationMode.value ?: CreationMode.ALL
        override val keys = dom.keys
            .mapNotNull { it.attribute.stringValue }
            .toSet()
        override val includes = dom.includes
            .mapNotNull { it.attribute.stringValue }
            .toSet()

        override fun toString() = "Index(module=$module, name=$name, isCustom=$isCustom)"
    }

    internal class TSMetaItemAttributeImpl(
        dom: Attribute,
        override val module: Module,
        override val name: String,
        override val isCustom: Boolean,
        override val persistence: TSMetaPersistence,
        override val modifiers: TSMetaModifiers,
        override val customProperties: Map<String, TSMetaCustomProperty>
    ) : TSMetaItemAttribute {

        override val domAnchor: DomAnchor<Attribute> = DomService.getInstance().createAnchor(dom)

        override val description = dom.description.xmlTag?.value?.text
        override val defaultValue = dom.defaultValue.stringValue
        override val type = dom.type.stringValue
        override val isDeprecated = TSMetaHelper.isDeprecated(dom.model, name)
        override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
        override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
        override val isRedeclare = java.lang.Boolean.TRUE == dom.redeclare.value
        override val isSelectionOf = dom.isSelectionOf.stringValue
        override val isLocalized = TSMetaHelper.isLocalized(type)
        override val isDynamic = TSMetaHelper.isDynamic(persistence)

        override fun toString() = "Attribute(module=$module, name=$name, isCustom=$isCustom)"
    }
}

internal class TSGlobalMetaItemImpl(localMeta: TSMetaItem)
    : TSGlobalMetaItemSelfMerge<ItemType, TSMetaItem>(localMeta), TSGlobalMetaItem {

    override val attributes = CaseInsensitiveConcurrentHashMap<String, TSGlobalMetaItem.TSGlobalMetaItemAttribute>()
    override val customProperties = CaseInsensitiveConcurrentHashMap<String, TSMetaCustomProperty>()
    override val indexes = CaseInsensitiveConcurrentHashMap<String, TSGlobalMetaItem.TSGlobalMetaItemIndex>()

    override val allAttributes = LinkedList<TSGlobalMetaItem.TSGlobalMetaItemAttribute>()
    override val allIndexes = LinkedList<TSGlobalMetaItem.TSGlobalMetaItemIndex>()
    override val allCustomProperties = LinkedList<TSMetaCustomProperty>()
    override val allRelationEnds = LinkedList<TSMetaRelation.TSMetaRelationElement>()
    override val allExtends = LinkedHashSet<TSGlobalMetaItem>()

    override var domAnchor = localMeta.domAnchor
    override var module = localMeta.module
    override var extendedMetaItemName = localMeta.extendedMetaItemName
    override var isAbstract = localMeta.isAbstract
    override var isAutoCreate = localMeta.isAutoCreate
    override var isGenerate = localMeta.isGenerate
    override var isSingleton = localMeta.isSingleton
    override var isJaloOnly = localMeta.isJaloOnly
    override var jaloClass = localMeta.jaloClass
    override var description = localMeta.description
    override var deployment = localMeta.deployment
    override var isCatalogAware = localMeta.isCatalogAware
    override var flattenType: String? = TSMetaHelper.flattenType(this)

    init {
        mergeAttributes(localMeta)
        mergeIndexes(localMeta)
        mergeCustomProperties(localMeta)
    }

    override fun toString() = "Item(module=$module, name=$name, isCustom=$isCustom)"

    @Suppress("UNCHECKED_CAST")
    private fun mergeAttributes(localMeta: TSMetaItem) = localMeta.attributes.values.forEach {
        val globalAttribute = this.attributes.computeIfAbsent(it.name) { _ -> TSGlobalMetaItemAttributeImpl(it)}
        if (globalAttribute is TSMetaSelfMerge<*, *>) {
            (globalAttribute as TSMetaSelfMerge<Attribute, TSMetaItemAttribute>).merge(it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mergeIndexes(localMeta: TSMetaItem) = localMeta.indexes.values.forEach {
        val globalIndex = this.indexes.computeIfAbsent(it.name) { _ -> TSGlobalMetaItemIndexImpl(it) }
        if (globalIndex is TSMetaSelfMerge<*, *>) {
            (globalIndex as TSMetaSelfMerge<Index, TSMetaItemIndex>).merge(it)
        }
    }

    private fun mergeCustomProperties(localMeta: TSMetaItem) = customProperties.putAll(localMeta.customProperties)

    override fun postMerge(globalMetaModel: TSGlobalMetaModel) {
        val extends = TSMetaHelper.getAllExtends(globalMetaModel, this)
        val relationEnds = TSMetaHelper.getAllRelationEnds(globalMetaModel, this, extends)

        allExtends.addAll(extends)
        allAttributes.addAll(attributes.values + extends.flatMap { it.attributes.values })
        allCustomProperties.addAll(customProperties.values + extends.flatMap { it.customProperties.values })
        allIndexes.addAll(indexes.values + extends.flatMap { it.indexes.values })
        allRelationEnds.addAll(relationEnds)

        if (!isCatalogAware) {
            isCatalogAware = extends.any { it.isCatalogAware }
        }
    }

    override fun mergeInternally(localMeta: TSMetaItem) {
        if (localMeta.isAbstract) isAbstract = localMeta.isAbstract
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate
        if (localMeta.isJaloOnly) isJaloOnly = localMeta.isJaloOnly
        if (localMeta.isSingleton) isJaloOnly = localMeta.isSingleton
        if (localMeta.isCatalogAware) isCatalogAware = localMeta.isCatalogAware

        if (localMeta.deployment?.retrieveDom()?.exists() == true) deployment = localMeta.deployment

        localMeta.extendedMetaItemName?.let {
            if (extendedMetaItemName != null) mergeConflicts.add("Extends should be defined only once.")
            extendedMetaItemName = it
        }

        mergeAttributes(localMeta)
        mergeIndexes(localMeta)
        mergeCustomProperties(localMeta)
    }

    internal class TSGlobalMetaItemIndexImpl(localMeta: TSMetaItemIndex)
        : TSMetaSelfMerge<Index, TSMetaItemIndex>(localMeta), TSGlobalMetaItem.TSGlobalMetaItemIndex {

        override val name: String = localMeta.name
        override var domAnchor = localMeta.domAnchor
        override var module = localMeta.module
        override var isRemove = localMeta.isRemove
        override var isReplace = localMeta.isReplace
        override var isUnique = localMeta.isUnique
        override var creationMode = localMeta.creationMode
        override var keys = localMeta.keys
        override var includes = localMeta.includes

        override fun mergeInternally(localMeta: TSMetaItemIndex) {
            if (localMeta.isReplace) {
                isRemove = localMeta.isRemove
                isReplace = localMeta.isReplace
                isUnique = localMeta.isUnique
                creationMode = localMeta.creationMode
                keys = localMeta.keys
                includes = localMeta.includes
            }
        }

        override fun toString() = "Index(module=$module, name=$name, isCustom=$isCustom)"
    }

    internal class TSGlobalMetaItemAttributeImpl(localMeta: TSMetaItemAttribute)
        : TSMetaSelfMerge<Attribute, TSMetaItemAttribute>(localMeta), TSGlobalMetaItem.TSGlobalMetaItemAttribute {

        override val customProperties = CaseInsensitiveConcurrentHashMap<String, TSMetaCustomProperty>()
        override val name: String = localMeta.name
        override var module = localMeta.module
        override var modifiers = localMeta.modifiers
        override var persistence = localMeta.persistence
        override var domAnchor = localMeta.domAnchor
        override var description = localMeta.description
        override var defaultValue = localMeta.defaultValue
        override var type = localMeta.type
        override var isDeprecated = localMeta.isDeprecated
        override var isAutoCreate = localMeta.isAutoCreate
        override var isGenerate = localMeta.isGenerate
        override var isRedeclare = localMeta.isRedeclare
        override var isSelectionOf = localMeta.isSelectionOf
        override var isLocalized = localMeta.isLocalized
        override var isDynamic = localMeta.isDynamic
        override var flattenType = TSMetaHelper.flattenType(localMeta)

        init {
            mergeCustomProperties(localMeta)
        }

        private fun mergeCustomProperties(localMeta: TSMetaItemAttribute) = customProperties.putAll(localMeta.customProperties)

        override fun mergeInternally(localMeta: TSMetaItemAttribute) {
            if (localMeta.isRedeclare) {
                domAnchor = localMeta.domAnchor
                type = localMeta.type
                modifiers = localMeta.modifiers
                isLocalized = localMeta.isLocalized
                isDynamic = localMeta.isDynamic

                mergeCustomProperties(localMeta)
            }
        }

        override fun toString() = "Attribute(module=$module, name=$name, isCustom=$isCustom)"

    }
}