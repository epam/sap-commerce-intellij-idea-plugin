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
package com.intellij.idea.plugin.hybris.system.type.meta.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.impl.*
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import org.apache.commons.lang3.StringUtils

class TSMetaModelBuilder(
    private val myModule: Module,
    myPsiFile: PsiFile,
    private val myCustom: Boolean
) {

    private val myMetaModel = TSMetaModel(myModule, myPsiFile.virtualFile, myCustom)

    fun build() = myMetaModel

    fun withItemTypes(types: List<ItemType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, TSMetaType.META_ITEM) }

        return this
    }

    fun withEnumTypes(types: List<EnumType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, TSMetaType.META_ENUM) }

        return this
    }

    fun withCollectionTypes(types: List<CollectionType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, TSMetaType.META_COLLECTION) }

        return this
    }

    fun withMapTypes(types: List<MapType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, TSMetaType.META_MAP) }

        return this
    }

    fun withRelationTypes(types: List<Relation>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach {
                myMetaModel.addMetaModel(it, TSMetaType.META_RELATION)
                registerReferenceEnd(it.source, it.target)
                registerReferenceEnd(it.target, it.source)
            }

        return this
    }

    fun withAtomicTypes(types: List<AtomicType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, TSMetaType.META_ATOMIC) }

        return this
    }

    private fun create(dom: ItemType): TSMetaItem? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaItemImpl(
            dom, myModule, name, myCustom,
            attributes = create(dom, dom.attributes),
            indexes = create(dom.indexes),
            customProperties = create(dom.customProperties),
            deployment = create(dom.deployment)
        )
    }

    private fun create(dom: EnumType): TSMetaEnum? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaEnumImpl(
            dom, myModule, name, myCustom,
            values = createEnumValues(dom)
        )
    }

    private fun create(dom: AtomicType): TSMetaAtomic? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaAtomicImpl(dom, myModule, name, myCustom)
    }

    private fun create(dom: CollectionType): TSMetaCollection? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaCollectionImpl(dom, myModule, name, myCustom)
    }

    private fun create(dom: Relation): TSMetaRelation? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        val source = create(dom.sourceElement, TSMetaRelation.RelationEnd.SOURCE)
        val target = create(dom.targetElement, TSMetaRelation.RelationEnd.TARGET)
        val orderingAttribute = create(source, target)

        return TSMetaRelationImpl(
            dom, myModule, name, myCustom,
            deployment = create(dom.deployment),
            source = source,
            target = target,
            orderingAttribute = orderingAttribute
        )
    }

    private fun create(source: TSMetaRelation.TSMetaRelationElement, target: TSMetaRelation.TSMetaRelationElement): TSMetaRelation.TSMetaOrderingAttribute? {
        if (target.isOrdered && source.cardinality == Cardinality.ONE && target.cardinality == Cardinality.MANY && source.qualifier != null) {
            return create(target, target.retrieveDom()!!, source.qualifier!!)
        }
        if (source.isOrdered && source.cardinality == Cardinality.MANY && target.cardinality == Cardinality.ONE && target.qualifier != null) {
            return create(source, source.retrieveDom()!!, target.qualifier!!)
        }

        return null
    }

    private fun create(
        owner: TSMetaRelation.TSMetaRelationElement,
        dom: RelationElement,
        qualifierPrefix: String
    ) = TSMetaRelationImpl.TSMetaOrderingAttributeImpl(
        dom = dom,
        owner = owner,
        module = myModule,
        isCustom = myCustom,
        qualifier = qualifierPrefix + HybrisConstants.TS_RELATION_ORDERING_POSTFIX
    )

    private fun create(dom: RelationElement, end: TSMetaRelation.RelationEnd): TSMetaRelation.TSMetaRelationElement {
        return TSMetaRelationImpl.TSMetaRelationElementImpl(
            dom, myModule, myCustom, end,
            modifiers = create(dom.modifiers),
            customProperties = create(dom.customProperties)
        )
    }

    private fun create(dom: MapType): TSMetaMap? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaMapImpl(dom, myModule, name, myCustom)
    }

    private fun create(dom: Modifiers): TSMetaModifiers {
        return TSMetaModifiersImpl(dom, myModule, myCustom)
    }

    private fun create(dom: EnumValue): TSMetaEnum.TSMetaEnumValue? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaEnumImpl.TSMetaEnumValueImpl(dom, myModule, myCustom, name)
    }

    private fun create(dom: CustomProperty): TSMetaCustomProperty? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaCustomPropertyImpl(dom, myModule, myCustom, name)
    }

    private fun create(dom: Index): TSMetaItem.TSMetaItemIndex? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaItemImpl.TSMetaItemIndexImpl(dom, myModule, name, myCustom)
    }

    private fun create(dom: Deployment) =
        if (dom.exists())
            TSMetaDeploymentImpl(dom, myModule, TSMetaModelNameProvider.extract(dom), myCustom)
        else null

    private fun create(itemTypeDom: ItemType, attributeDom: Attribute, dom: Persistence) = TSMetaPersistenceImpl(
        itemTypeDom,
        attributeDom,
        dom,
        myModule,
        TSMetaModelNameProvider.extract(dom),
        myCustom
    )

    private fun create(itemTypeDom: ItemType, dom: Attribute): TSMetaItem.TSMetaItemAttribute? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaItemImpl.TSMetaItemAttributeImpl(
            dom, myModule, name, myCustom,
            customProperties = create(dom.customProperties),
            persistence = create(itemTypeDom, dom, dom.persistence),
            modifiers = create(dom.modifiers)
        )
    }

    private fun create(itemTypeDom: ItemType, dom: Attributes): Map<String, TSMetaItem.TSMetaItemAttribute> = dom.attributes
        .mapNotNull { attr -> create(itemTypeDom, attr) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { attr -> attr.name.trim { it <= ' ' } }

    private fun create(dom: CustomProperties): Map<String, TSMetaCustomProperty> = dom.properties
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { attr -> attr.name.trim { it <= ' ' } }

    private fun create(dom: Indexes): Map<String, TSMetaItem.TSMetaItemIndex> = dom.indexes
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { attr -> attr.name.trim { it <= ' ' } }

    private fun createEnumValues(dom: EnumType): Map<String, TSMetaEnum.TSMetaEnumValue> = dom.values
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { attr -> attr.name.trim { it <= ' ' } }

    private fun registerReferenceEnd(ownerEnd: TSMetaRelation.TSMetaRelationElement, targetEnd: TSMetaRelation.TSMetaRelationElement) {
        if (!targetEnd.isNavigable) return

        val ownerTypeName = ownerEnd.type

        if (StringUtils.isNotEmpty(ownerTypeName)) {
            myMetaModel.getRelations().putValue(ownerTypeName, targetEnd)
        }
    }

}