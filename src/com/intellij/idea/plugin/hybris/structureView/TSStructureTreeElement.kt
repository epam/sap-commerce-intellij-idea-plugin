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
package com.intellij.idea.plugin.hybris.structureView

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Function
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomElementNavigationProvider
import com.intellij.util.xml.DomService
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.structure.DomStructureTreeElement

class TSStructureTreeElement(
    stableCopy: DomElement,
    private val myDescriptor: Function<DomElement, DomService.StructureViewMode>,
    private val myNavigationProvider: DomElementNavigationProvider
) : DomStructureTreeElement(stableCopy, myDescriptor, myNavigationProvider) {
    override fun createChildElement(element: DomElement): StructureViewTreeElement {
        return TSStructureTreeElement(element, myDescriptor, myNavigationProvider)
    }

    override fun getPresentableText() = when (val dom = element) {
        is AtomicTypes -> "Atomic types"
        is CollectionTypes -> "Collections types"
        is MapTypes -> "Map types"
        is EnumTypes -> "Enum types"
        is Relations -> "Relations"
        is ItemTypes -> "Item types"
        is TypeGroup -> resolveValue(dom.name)
        is AtomicType -> resolveValue(dom.clazz)
        is Attribute -> resolveValue(dom.qualifier)
        is CollectionType -> resolveValue(dom.code)
        is ColumnType -> resolveValue(dom.database)
        is CustomProperty -> resolveValue(dom.name)
        is EnumType -> resolveValue(dom.code)
        is EnumValue -> resolveValue(dom.code)
        is Index -> resolveValue(dom.name)
        is IndexKey -> resolveValue(dom.attribute)
        is IndexInclude -> resolveValue(dom.attribute)
        is ItemType -> resolveValue(dom.code)
        is MapType -> resolveValue(dom.code)
        is Relation -> resolveValue(dom.code)
        is RelationElement -> resolveValue(dom.qualifier)
        is Value -> resolveValue(dom.code)
        is Modifiers -> "modifiers: "
        is Description -> ""
        else -> null
    }
        ?: super.getPresentableText()

    override fun getLocationString() = when (val dom = element) {
        is Attribute -> resolveLocationString(dom)
        is ItemType -> resolveLocationString(dom)
        is Modifiers -> resolveLocationString(dom)
        is Description -> resolveLocationString(dom)
        is Index -> resolveLocationString(dom)
        is CollectionType -> resolveLocationString(dom)
        is MapType -> resolveLocationString(dom)
        is RelationElement -> resolveLocationString(dom)
        is ModelMethod -> resolveLocationString(dom)
        is Deployment -> resolveLocationString(dom)
        is Persistence -> dom.type.stringValue + (dom.attributeHandler.stringValue?.let { " ($it)" } ?: "")
        is EnumType -> dom.dynamic.value?.let { "dynamic" }
        is Relation -> dom.localized.value?.let { "localized" }
        is AtomicType -> dom.extends.stringValue
        is CustomProperty -> dom.value.stringValue
        is IndexKey -> "key"
        is IndexInclude -> "include"
        else -> null
    }

    private fun resolveValue(attributeValue: GenericAttributeValue<String>?) = attributeValue?.stringValue

    private fun resolveLocationString(dom: MapType) = (dom.argumentType.stringValue ?: "?") + " : " + (dom.returnType.stringValue ?: "?")

    private fun resolveLocationString(dom: CollectionType) =
        (dom.type.stringValue ?: "collection") + (dom.elementType.stringValue?.let { " of $it" } ?: "")

    private fun resolveLocationString(dom: Description): String? {
        val xmlElement = dom.xmlElement
        if (xmlElement is XmlTag) {
            return xmlElement.value.trimmedText
        }
        return null
    }

    private fun resolveLocationString(dom: Deployment) = listOfNotNull(
        dom.table.stringValue,
        dom.typeCode.stringValue?.let { "($it)" },
    ).joinToString("")

    private fun resolveLocationString(dom: ModelMethod) = listOfNotNull(
        dom.name.stringValue,
        dom.default.value?.let { "default($it)" },
        dom.deprecated.value?.let { "deprecated($it)" },
        dom.deprecatedSince.stringValue?.let { "since $it" }
    ).joinToString()

    private fun resolveLocationString(dom: RelationElement) = listOfNotNull(
        dom.type.stringValue,
        dom.collectionType.stringValue ?: "collection",
        dom.cardinality.stringValue,
        dom.ordered.stringValue?.let { "ordered($it)" }
    ).joinToString()

    private fun resolveLocationString(dom: Index) = listOfNotNull(
        dom.creationMode.value?.let { "mode(${it.name})" },
        dom.remove.value?.let { "${Index.REMOVE}($it)" },
        dom.replace.value?.let { "${Index.REPLACE}($it)" },
        dom.unique.value?.let { "${Index.UNIQUE}($it)" }
    ).joinToString()

    private fun resolveLocationString(dom: Modifiers) = listOfNotNull(
        dom.doNotOptimize.value?.let { "${Modifiers.DONT_OPTIMIZE}($it)" },
        dom.encrypted.value?.let { "${Modifiers.ENCRYPTED}($it)" },
        dom.initial.value?.let { "${Modifiers.INITIAL}($it)" },
        dom.optional.value?.let { "${Modifiers.OPTIONAL}($it)" },
        dom.partOf.value?.let { "${Modifiers.PART_OF}($it)" },
        dom.private.value?.let { "${Modifiers.PRIVATE}($it)" },
        dom.read.value?.let { "${Modifiers.READ}($it)" },
        dom.removable.value?.let { "${Modifiers.REMOVABLE}($it)" },
        dom.search.value?.let { "${Modifiers.SEARCH}($it)" },
        dom.unique.value?.let { "${Modifiers.UNIQUE}($it)" },
        dom.write.value?.let { "${Modifiers.WRITE}($it)" }
    ).joinToString()

    private fun resolveLocationString(dom: ItemType) = resolveValue(dom.extends)?.let { ": $it" } ?: ": ${HybrisConstants.TS_TYPE_GENERIC_ITEM}"

    private fun resolveLocationString(dom: Attribute): String? {
        var value = resolveValue(dom.type) ?: return null
        if (value.startsWith(TS_ATTRIBUTE_LOCALIZED_PREFIX)) {
            value = value.substring(TS_ATTRIBUTE_LOCALIZED_PREFIX.length)
            value += " (localized)"
        }
        if (value.startsWith("java.lang.")) {
            value = value.substring("java.lang.".length)
        }
        return value
    }

    override fun getIcon(open: Boolean) = when (element) {
        is Description -> AllIcons.Windows.Help
        is AtomicType -> HybrisIcons.ATOMIC
        is EnumType -> HybrisIcons.ENUM
        is EnumValue -> HybrisIcons.ENUM_VALUE
        is Attribute -> HybrisIcons.ATTRIBUTE
        is Index -> HybrisIcons.INDEX
        is ItemType -> HybrisIcons.ITEM
        is CollectionType -> HybrisIcons.COLLECTION
        is CustomProperty -> HybrisIcons.PROPERTY
        is MapType -> HybrisIcons.MAP
        is Relation -> HybrisIcons.RELATION
        is RelationSourceElement -> HybrisIcons.RELATION_SOURCE
        is RelationTargetElement -> HybrisIcons.RELATION_TARGET
        else -> null
    }

//    : Icon? {
//        val dom = element
//        // TODO: add icons
//        if (dom is Attribute) {
//            val value = resolveValue(dom.type)
//            if (StringUtils.startsWith(value, TS_ATTRIBUTE_LOCALIZED_PREFIX)) {
//                return HybrisIcons.LOCALIZED
//            }
//        }
//        return null
//    }
}