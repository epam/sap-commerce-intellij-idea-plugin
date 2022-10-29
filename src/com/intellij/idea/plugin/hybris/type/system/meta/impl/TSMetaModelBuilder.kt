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
package com.intellij.idea.plugin.hybris.type.system.meta.impl

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel
import com.intellij.idea.plugin.hybris.type.system.meta.model.*
import com.intellij.idea.plugin.hybris.type.system.meta.model.impl.*
import com.intellij.idea.plugin.hybris.type.system.model.*
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import org.apache.commons.lang3.StringUtils

class TSMetaModelBuilder(
    private val myModule: Module,
    private val myPsiFile: PsiFile,
    private val myCustom: Boolean
) {

    private val myMetaModel = TSMetaModel(myModule, myPsiFile, myCustom)

    private fun create(dom: ItemType): TSMetaItem? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaItemImpl(dom, myModule, name, myCustom)
    }

    private fun create(dom: EnumType): TSMetaEnum? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaEnumImpl(dom, myModule, name, myCustom)
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
        return TSMetaRelationImpl(dom, myModule, name, myCustom)
    }

    private fun create(dom: MapType): TSMetaMap? {
        val name = TSMetaModelNameProvider.extract(dom) ?: return null
        return TSMetaMapImpl(dom, myModule, name, myCustom)
    }

    fun withItemTypes(types: List<ItemType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, MetaType.META_ITEM) }

        return this
    }

    fun withEnumTypes(types: List<EnumType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, MetaType.META_ENUM) }

        return this
    }

    fun withCollectionTypes(types: List<CollectionType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, MetaType.META_COLLECTION) }

        return this
    }

    fun withMapTypes(types: List<MapType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, MetaType.META_MAP) }

        return this
    }

    fun withRelationTypes(types: List<Relation>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach {
                myMetaModel.addMetaModel(it, MetaType.META_RELATION)
                registerReferenceEnd(it.source, it.target)
                registerReferenceEnd(it.target, it.source)
            }

        return this
    }

    fun withAtomicTypes(types: List<AtomicType>): TSMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, MetaType.META_ATOMIC) }

        return this
    }

    private fun registerReferenceEnd(ownerEnd: TSMetaRelation.TSMetaRelationElement, targetEnd: TSMetaRelation.TSMetaRelationElement) {
        if (!targetEnd.isNavigable) return

        val ownerTypeName = ownerEnd.type

        if (StringUtils.isNotEmpty(ownerTypeName)) {
            myMetaModel.getReferences()[ownerTypeName] = targetEnd
        }
    }

    fun build() = myMetaModel

}