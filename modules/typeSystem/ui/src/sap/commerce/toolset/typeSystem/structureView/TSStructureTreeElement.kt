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
package sap.commerce.toolset.typeSystem.structureView

import com.intellij.psi.xml.XmlTag
import com.intellij.util.Function
import com.intellij.util.asSafely
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomElementNavigationProvider
import com.intellij.util.xml.DomService
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.structure.DomStructureTreeElement
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisConstants.TS_ATTRIBUTE_LOCALIZED_PREFIX
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.typeSystem.model.*

class TSStructureTreeElement(
    stableCopy: DomElement,
    private val myDescriptor: Function<DomElement, DomService.StructureViewMode>,
    private val myNavigationProvider: DomElementNavigationProvider
) : DomStructureTreeElement(stableCopy, myDescriptor, myNavigationProvider) {

    override fun createChildElement(element: DomElement) = TSStructureTreeElement(element, myDescriptor, myNavigationProvider)

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
        is EnumType -> if (dom.dynamic.value) "dynamic" else null
        is Relation -> dom.localized.value?.let { "localized" }
        is AtomicType -> dom.extends.stringValue
        is CustomProperty -> dom.value.stringValue
        is IndexKey -> "key"
        is IndexInclude -> "include"
        else -> null
    }

    private fun resolveValue(attributeValue: GenericAttributeValue<String>?) = attributeValue?.stringValue

    private fun resolveLocationString(dom: MapType) = (dom.argumentType.stringValue ?: "?") + " : " + (dom.returnType.stringValue ?: "?")

    private fun resolveLocationString(dom: CollectionType) = dom.type.value.toString() + (dom.elementType.stringValue?.let { " of $it" } ?: "")

    private fun resolveLocationString(dom: Description) = dom.xmlElement
        ?.asSafely<XmlTag>()
        ?.value
        ?.trimmedText

    private fun resolveLocationString(dom: Deployment) = listOfNotNull(
        dom.table.stringValue,
        dom.typeCode.stringValue?.let { "($it)" },
    ).joinToString("")

    private fun resolveLocationString(dom: ModelMethod) = listOfNotNull(
        dom.name.stringValue,
        if (dom.default.value) "default" else null,
        if (dom.deprecated.value) "deprecated" else null,
        dom.deprecatedSince.stringValue?.let { "since $it" }
    ).joinToString()

    private fun resolveLocationString(dom: RelationElement) = listOfNotNull(
        dom.type.stringValue,
        dom.collectionType.value,
        dom.cardinality.value,
        if (dom.ordered.value) "ordered" else null,
    ).joinToString()

    private fun resolveLocationString(dom: Index) = listOfNotNull(
        dom.creationMode.value?.let { "mode(${it.name})" },
        if (dom.remove.value) Index.REMOVE else null,
        if (dom.replace.value) Index.REPLACE else null,
        if (dom.unique.value) Index.UNIQUE else null,
    ).joinToString()

    private fun resolveLocationString(dom: Modifiers) = listOfNotNull(
        if (dom.doNotOptimize.value) Modifiers.DONT_OPTIMIZE else null,
        if (dom.encrypted.value) Modifiers.ENCRYPTED else null,
        if (dom.initial.value) Modifiers.INITIAL else null,
        if (dom.optional.value) Modifiers.OPTIONAL else null,
        if (dom.partOf.value) Modifiers.PART_OF else null,
        if (dom.private.value) Modifiers.PRIVATE else null,
        if (dom.read.value) Modifiers.READ else null,
        if (dom.removable.value) Modifiers.REMOVABLE else null,
        if (dom.search.value) Modifiers.SEARCH else null,
        if (dom.unique.value) Modifiers.UNIQUE else null,
        if (dom.write.value) Modifiers.WRITE else null,
    ).joinToString()

    private fun resolveLocationString(dom: ItemType) = resolveValue(dom.extends)?.let { ": $it" }
        ?: ": ${HybrisConstants.TS_TYPE_GENERIC_ITEM}"

    private fun resolveLocationString(dom: Attribute): String? {
        var value = resolveValue(dom.type) ?: return null
        if (value.startsWith(TS_ATTRIBUTE_LOCALIZED_PREFIX)) {
            value = value.substring(TS_ATTRIBUTE_LOCALIZED_PREFIX.length)
            value += " (localized)"
        }
        if (value.startsWith(HybrisConstants.TS_JAVA_LANG_PREFIX)) {
            value = value.substring(HybrisConstants.TS_JAVA_LANG_PREFIX.length)
        }
        return value
    }

    override fun getIcon(open: Boolean) = when (element) {
        is Description -> HybrisIcons.TypeSystem.DESCRIPTION
        is AtomicTypes -> HybrisIcons.TypeSystem.GROUP_ATOMIC
        is CollectionTypes -> HybrisIcons.TypeSystem.GROUP_COLLECTION
        is MapTypes -> HybrisIcons.TypeSystem.GROUP_MAP
        is Relations -> HybrisIcons.TypeSystem.GROUP_RELATION
        is ItemTypes -> HybrisIcons.TypeSystem.GROUP_ITEM
        is TypeGroup -> HybrisIcons.TypeSystem.GROUP_ITEM
        is EnumTypes -> HybrisIcons.TypeSystem.GROUP_ENUM
        is AtomicType -> HybrisIcons.TypeSystem.Types.ATOMIC
        is EnumType -> HybrisIcons.TypeSystem.Types.ENUM
        is EnumValue -> HybrisIcons.TypeSystem.ENUM_VALUE
        is Attribute -> HybrisIcons.TypeSystem.ATTRIBUTE
        is Index -> HybrisIcons.TypeSystem.INDEX
        is ItemType -> HybrisIcons.TypeSystem.Types.ITEM
        is CollectionType -> HybrisIcons.TypeSystem.Types.COLLECTION
        is CustomProperty -> HybrisIcons.TypeSystem.CUSTOM_PROPERTY
        is MapType -> HybrisIcons.TypeSystem.Types.MAP
        is Relation -> HybrisIcons.TypeSystem.Types.RELATION
        is RelationSourceElement -> HybrisIcons.TypeSystem.RELATION_SOURCE
        is RelationTargetElement -> HybrisIcons.TypeSystem.RELATION_TARGET
        else -> null
    }

}