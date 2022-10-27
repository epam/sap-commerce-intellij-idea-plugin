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

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive.NoCaseMultiMap
import com.intellij.idea.plugin.hybris.type.system.meta.impl.TSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaDeployment
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem.TSMetaItemAttribute
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem.TSMetaItemIndex
import com.intellij.idea.plugin.hybris.type.system.model.Attribute
import com.intellij.idea.plugin.hybris.type.system.model.CreationMode
import com.intellij.idea.plugin.hybris.type.system.model.Index
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService

class TSMetaItemImpl(
    override val module: Module,
    override val project: Project,
    override val name: String?,
    dom: ItemType,
    override val isCustom: Boolean
) : TSMetaEntityImpl<ItemType>(dom, module, project, isCustom, name), TSMetaItem {
    private val myAllDoms: MutableSet<DomAnchor<ItemType>> = LinkedHashSet()
    override val attributes = NoCaseMultiMap<TSMetaItemAttribute>()
    override val customProperties = NoCaseMultiMap<TSMetaCustomProperty>()
    override val indexes = NoCaseMultiMap<TSMetaItemIndex>()
    override var extendedMetaItemName: String? = null
    override val isAbstract = java.lang.Boolean.TRUE == dom.abstract.value
    override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
    override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
    override val isSingleton = java.lang.Boolean.TRUE == dom.singleton.value
    override val isJaloOnly = java.lang.Boolean.TRUE == dom.jaloOnly.value
    override val jaloClass = dom.jaloClass.stringValue
    override val description = dom.description.xmlTag?.value?.text
    override val deployment: TSMetaDeployment<TSMetaItem> = TSMetaDeploymentImpl(module, project, dom.deployment, isCustom, this, TSMetaModelNameProvider.extract(dom.deployment))

    init {
        myAllDoms.add(DomService.getInstance().createAnchor(dom))
        registerExtends(dom)
    }

    override fun retrieveAllDoms(): List<ItemType> = myAllDoms.mapNotNull { it.retrieveDomElement() }
    override fun addAttribute(key: String, attribute: TSMetaItemAttribute) = attributes.putValue(key, attribute)
    override fun addCustomProperty(key: String, customProperty: TSMetaCustomProperty) = customProperties.putValue(key, customProperty)

    override fun addIndex(key: String, index: TSMetaItemIndex) = indexes.putValue(key, index)

    override fun merge(another: TSMetaItem) {
        another.retrieveDom()?.let { addDomRepresentation(it) }

        attributes.putAllValues(another.attributes)
        customProperties.putAllValues(another.customProperties)
        indexes.putAllValues(another.indexes)
    }

    private fun addDomRepresentation(anotherDom: ItemType) {
        myAllDoms.add(DomService.getInstance().createAnchor(anotherDom))
        registerExtends(anotherDom)
    }

    private fun registerExtends(dom: ItemType) {
        //only one extends is allowed
        if (extendedMetaItemName == null) {
            extendedMetaItemName = dom.extends.rawText
        }
    }

    override fun toString(): String {
        return "TSMetaItemImpl(module=$module, name=$name, isCustom=$isCustom)"
    }

    class TSMetaItemIndexImpl(
        override val module: Module,
        override val project: Project,
        dom: Index,
        override val isCustom: Boolean,
        override val owner: TSMetaItem,
        override val name: String
    ) : TSMetaEntityImpl<Index>(dom, module, project, isCustom, name), TSMetaItemIndex {

        override val isRemove = java.lang.Boolean.TRUE == dom.remove.value
        override val isReplace = java.lang.Boolean.TRUE == dom.replace.value
        override val isUnique = java.lang.Boolean.TRUE == dom.unique.value
        override val creationMode = dom.creationMode.value ?: CreationMode.ALL
        override val keys = dom.keys
            .mapNotNull { it.attribute.stringValue }
            .toSet()

        override fun toString(): String {
            return "TSMetaEntityImpl(module=$module, name=$name, isCustom=$isCustom)"
        }
    }

    class TSMetaItemAttributeImpl(
        override val module: Module,
        override val project: Project,
        dom: Attribute,
        override val isCustom: Boolean,
        override val owner: TSMetaItem,
        override val name: String
    ) : TSMetaEntityImpl<Attribute>(dom, module, project, isCustom, name),
        TSMetaItemAttribute {

        override val customProperties = NoCaseMultiMap<TSMetaCustomProperty>()
        override val description = dom.description.xmlTag?.value?.text
        override val defaultValue = dom.defaultValue.stringValue
        override val type = dom.type.stringValue
        override val isDeprecated = extractDeprecated(dom)
        override val isAutoCreate = java.lang.Boolean.TRUE == dom.autoCreate.value
        override val isGenerate = java.lang.Boolean.TRUE == dom.generate.value
        override val isRedeclare = java.lang.Boolean.TRUE == dom.redeclare.value

        override fun addCustomProperty(key: String, customProperty: TSMetaCustomProperty) = customProperties.putValue(key, customProperty)

        private fun extractDeprecated(dom: Attribute): Boolean {
            return dom.model.setters
                .any { name == it.name.stringValue && java.lang.Boolean.TRUE == it.deprecated.value }
        }

        override fun toString(): String {
            return "TSMetaItemAttributeImpl(module=$module, name=$name, isCustom=$isCustom)"
        }

    }
}