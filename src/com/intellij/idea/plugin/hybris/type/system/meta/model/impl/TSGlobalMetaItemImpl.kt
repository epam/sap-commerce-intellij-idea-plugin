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
package com.intellij.idea.plugin.hybris.type.system.meta.model.impl

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.CaseInsensitiveConcurrentHashMap
import com.intellij.idea.plugin.hybris.type.system.meta.impl.TSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.type.system.meta.model.*
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem.TSMetaItemAttribute
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem.TSMetaItemIndex
import com.intellij.idea.plugin.hybris.type.system.model.Attribute
import com.intellij.idea.plugin.hybris.type.system.model.CreationMode
import com.intellij.idea.plugin.hybris.type.system.model.Index
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.openapi.module.Module
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

class TSMetaItemImpl(
    dom: ItemType,
    override val module: Module,
    override val name: String?,
    override val isCustom: Boolean
) : TSMetaItem {

    override val domAnchor: DomAnchor<ItemType> = DomService.getInstance().createAnchor(dom)
    override val attributes = CaseInsensitiveConcurrentHashMap<String, TSMetaItemAttribute>()
    override val customProperties = CaseInsensitiveConcurrentHashMap<String, TSMetaCustomProperty>()
    override val indexes = CaseInsensitiveConcurrentHashMap<String, TSMetaItemIndex>()
    override val isAbstract = java.lang.Boolean.TRUE == dom.abstract.value
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val isSingleton = java.lang.Boolean.TRUE == dom.singleton.value
    override val isJaloOnly = java.lang.Boolean.TRUE == dom.jaloOnly.value
    override val jaloClass = dom.jaloClass.stringValue
    override val description = dom.description.xmlTag?.value?.text
    override var extendedMetaItemName = dom.extends.stringValue
    override val deployment: TSMetaDeployment<TSMetaItem> = TSMetaDeploymentImpl(dom.deployment, this, module, TSMetaModelNameProvider.extract(dom.deployment), isCustom)

    init {
        dom.attributes.attributes
            .filter { TSMetaModelNameProvider.extract(it) != null }
            .map { attr -> TSMetaItemAttributeImpl(attr, module, TSMetaModelNameProvider.extract(attr)!!, isCustom) }
            .forEach { attr -> addAttribute(attr.name.trim { it <= ' ' }, attr) }
        dom.customProperties.properties
            .filter { TSMetaModelNameProvider.extract(it) != null }
            .map { TSMetaCustomPropertyImpl(it, module, isCustom, TSMetaModelNameProvider.extract(it)!!) }
            .forEach { prop -> addCustomProperty(prop.name.trim { it <= ' ' }, prop) }
        dom.indexes.indexes
            .filter { TSMetaModelNameProvider.extract(it) != null }
            .map { TSMetaItemIndexImpl(it, module, TSMetaModelNameProvider.extract(it)!!, isCustom) }
            .forEach { index -> addIndex(index.name.trim { it <= ' ' }, index) }
    }

    private fun addAttribute(key: String, attribute: TSMetaItemAttribute) = attributes.put(key, attribute)
    private fun addCustomProperty(key: String, customProperty: TSMetaCustomProperty) = customProperties.put(key, customProperty)
    private fun addIndex(key: String, index: TSMetaItemIndex) = indexes.put(key, index)

    override fun toString() = "TSMetaItemImpl(module=$module, name=$name, isCustom=$isCustom)"

    class TSMetaItemIndexImpl(
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

        override fun toString() = "TSMetaItemIndexImpl(module=$module, name=$name, isCustom=$isCustom)"
    }

    class TSMetaItemAttributeImpl(
        dom: Attribute,
        override val module: Module,
        override val name: String,
        override val isCustom: Boolean
    ) : TSMetaItemAttribute {

        override val domAnchor: DomAnchor<Attribute> = DomService.getInstance().createAnchor(dom)

        override val customProperties = createCustomProperties(dom)
        override val modifiers: TSMetaModifiers<TSMetaItemAttribute> = TSMetaModifiersImpl(dom.modifiers, module, isCustom)
        override val description = dom.description.xmlTag?.value?.text
        override val defaultValue = dom.defaultValue.stringValue
        override val type = dom.type.stringValue
        override val isDeprecated = extractDeprecated(dom)
        override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
        override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
        override val isRedeclare = java.lang.Boolean.TRUE == dom.redeclare.value
        override val isSelectionOf = dom.isSelectionOf.stringValue

        override fun toString() = "TSMetaItemAttributeImpl(module=$module, name=$name, isCustom=$isCustom)"

        private fun createCustomProperties(dom: Attribute): CaseInsensitiveConcurrentHashMap<String, TSMetaCustomProperty> {
            val map = CaseInsensitiveConcurrentHashMap<String, TSMetaCustomProperty>()
            dom.customProperties.properties
                .filter { TSMetaModelNameProvider.extract(it) != null }
                .map { TSMetaCustomPropertyImpl(it, module, isCustom, TSMetaModelNameProvider.extract(it)!!) }
                .forEach { prop -> map[prop.name.trim { it <= ' ' }] = prop }
            return map
        }

        private fun extractDeprecated(dom: Attribute): Boolean {
            return dom.model.setters
                .any { name == it.name.stringValue && java.lang.Boolean.TRUE == it.deprecated.value }
        }
    }
}

class TSGlobalMetaItemImpl(localMeta: TSMetaItem)
    : TSMetaSelfMerge<ItemType, TSMetaItem>(localMeta), TSGlobalMetaItem {

    override val attributes = CaseInsensitiveConcurrentHashMap<String, TSGlobalMetaItem.TSGlobalMetaItemAttribute>()
    override val customProperties = CaseInsensitiveConcurrentHashMap<String, TSMetaCustomProperty>()
    override val indexes = CaseInsensitiveConcurrentHashMap<String, TSGlobalMetaItem.TSGlobalMetaItemIndex>()

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

    init {
        mergeAttributes(localMeta)
        mergeCustomProperties(localMeta)
        mergeIndexes(localMeta)
    }

    private fun mergeAttributes(localMeta: TSMetaItem) = localMeta.attributes.values.forEach {
        val globalAttribute = this.attributes[it.name!!]
        if (globalAttribute == null) {
            this.attributes[it.name!!] = TSGlobalMetaItemAttributeImpl(it)
        } else if (globalAttribute is TSMetaSelfMerge<*, *>) {
            (globalAttribute as TSMetaSelfMerge<Attribute, TSMetaItemAttribute>).merge(it)
        }
    }

    private fun mergeIndexes(localMeta: TSMetaItem) = localMeta.indexes.values.forEach {
        val globalIndex = this.indexes[it.name!!]
        if (globalIndex == null) {
            this.indexes[it.name!!] = TSGlobalMetaItemIndexImpl(it)
        } else if (globalIndex is TSMetaSelfMerge<*, *>) {
            (globalIndex as TSMetaSelfMerge<Index, TSMetaItemIndex>).merge(it)
        }
    }

    private fun mergeCustomProperties(localMeta: TSMetaItem) = customProperties.putAll(localMeta.customProperties)

    override fun mergeInternally(localMeta: TSMetaItem) {
        if (localMeta.isAbstract) isAbstract = localMeta.isAbstract
        if (localMeta.isAutoCreate) isAutoCreate = localMeta.isAutoCreate
        if (localMeta.isGenerate) isGenerate = localMeta.isGenerate
        if (localMeta.isJaloOnly) isJaloOnly = localMeta.isJaloOnly
        if (localMeta.isSingleton) isJaloOnly = localMeta.isSingleton

        if (localMeta.deployment.retrieveDom()?.exists() == true) deployment = localMeta.deployment

        localMeta.extendedMetaItemName?.let {
            if (extendedMetaItemName != null) mergeConflicts.add("Extends should be defined only once.")
            extendedMetaItemName = it
        }

        mergeAttributes(localMeta)
        mergeCustomProperties(localMeta)
        mergeIndexes(localMeta)
    }

    class TSGlobalMetaItemIndexImpl(localMeta: TSMetaItemIndex)
        : TSMetaSelfMerge<Index, TSMetaItemIndex>(localMeta), TSGlobalMetaItem.TSGlobalMetaItemIndex {

        override var domAnchor = localMeta.domAnchor
        override var module = localMeta.module
        override var isRemove = localMeta.isRemove
        override var isReplace = localMeta.isReplace
        override var isUnique = localMeta.isUnique
        override var creationMode = localMeta.creationMode
        override var keys = localMeta.keys

        override fun mergeInternally(localMeta: TSMetaItemIndex) {
            if (localMeta.isReplace) {
                isRemove = localMeta.isRemove
                isReplace = localMeta.isReplace
                isUnique = localMeta.isUnique
                creationMode = localMeta.creationMode
                keys = localMeta.keys
            }
        }
    }

    class TSGlobalMetaItemAttributeImpl(localMeta: TSMetaItemAttribute)
        : TSMetaSelfMerge<Attribute, TSMetaItemAttribute>(localMeta), TSGlobalMetaItem.TSGlobalMetaItemAttribute {

        override val customProperties = CaseInsensitiveConcurrentHashMap<String, TSMetaCustomProperty>()
        override var module = localMeta.module
        override var modifiers = localMeta.modifiers
        override var domAnchor = localMeta.domAnchor
        override var description = localMeta.description
        override var defaultValue = localMeta.defaultValue
        override var type = localMeta.type
        override var isDeprecated = localMeta.isDeprecated
        override var isAutoCreate = localMeta.isAutoCreate
        override var isGenerate = localMeta.isGenerate
        override var isRedeclare = localMeta.isRedeclare
        override var isSelectionOf = localMeta.isSelectionOf

        init {
            mergeCustomProperties(localMeta)
        }

        private fun mergeCustomProperties(localMeta: TSMetaItemAttribute) = customProperties.putAll(localMeta.customProperties)

        override fun mergeInternally(localMeta: TSMetaItemAttribute) {
            if (localMeta.isRedeclare) {
                domAnchor = localMeta.domAnchor
                type = localMeta.type
                modifiers = localMeta.modifiers

                mergeCustomProperties(localMeta)
            }
        }

    }
}