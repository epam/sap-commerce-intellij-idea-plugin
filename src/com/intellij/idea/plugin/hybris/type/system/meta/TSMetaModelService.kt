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
package com.intellij.idea.plugin.hybris.type.system.meta

import com.intellij.idea.plugin.hybris.common.utils.CollectionUtils
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation.ReferenceEnd
import com.intellij.idea.plugin.hybris.type.system.meta.impl.*
import com.intellij.idea.plugin.hybris.type.system.model.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.xml.DomElement
import java.util.*
import java.util.stream.Collectors

/**
 * Each "findOrCreate" method should find metaType only when it is already defined for the same items.xml file.
 * In such a case self-merge will be performed to ensure consistency between all anchors
 */
class TSMetaModelService(private val myProject: Project) {

    private fun extractName(dom: ItemType): String? = dom.code.value
    private fun extractName(dom: EnumType): String? = dom.code.value
    private fun extractName(dom: CollectionType): String? = dom.code.value
    private fun extractName(dom: Relation): String? = dom.code.value
    private fun extractName(dom: AtomicType): String? = dom.clazz.value
    private fun extractName(dom: MapType): String? = dom.code.value

    fun findOrCreate(dom: ItemType): TSMetaItem? {
        val name = extractName(dom) ?: return null
        val typeCode = dom.deployment.typeCode.stringValue
        val items = metaModel().getMetaType<TSMetaItem>(MetaType.META_ITEM)
        var impl = items[name]

        if (impl == null) {
            impl = TSMetaItemImpl(myProject, name, typeCode, dom)
            items[name] = impl
        } else {
            impl.merge(TSMetaItemImpl(myProject, name, typeCode, dom))
        }
        return impl
    }

    fun findOrCreate(dom: EnumType): TSMetaEnum? {
        val name = extractName(dom) ?: return null
        val enums = metaModel().getMetaType<TSMetaEnum>(MetaType.META_ENUM)
        var impl = enums[name]

        if (impl == null) {
            impl = TSMetaEnumImpl(myProject, name, dom)
            enums[name] = impl
        }
        return impl
    }

    fun findOrCreate(dom: AtomicType): TSMetaAtomic? {
        val clazzName = extractName(dom) ?: return null

        return metaModel().getMetaType<TSMetaAtomic>(MetaType.META_ATOMIC)
            .computeIfAbsent(clazzName)
            { key: String -> TSMetaAtomicImpl(myProject, key, dom) }
    }

    fun findOrCreate(dom: CollectionType): TSMetaCollection? {
        val name = extractName(dom) ?: return null

        return metaModel().getMetaType<TSMetaCollection>(MetaType.META_COLLECTION)
            .computeIfAbsent(name)
            { key: String? -> TSMetaCollectionImpl(myProject, key, dom) }
    }

    fun findOrCreate(dom: Relation): TSMetaRelation? {
        val name = extractName(dom)
        val typeCode = dom.deployment.typeCode.stringValue

        if (name == null || typeCode == null) return null

        return metaModel().getMetaType<TSMetaRelation>(MetaType.META_RELATION)
            .computeIfAbsent(name) { key: String ->
                val impl: TSMetaRelation = TSMetaRelationImpl(myProject, key, typeCode, dom)
                registerReferenceEnd(impl.source, impl.target)
                registerReferenceEnd(impl.target, impl.source)
                impl
            }
    }

    fun findOrCreate(dom: MapType): TSMetaMap? {
        val name = extractName(dom) ?: return null

        val maps = metaModel().getMetaType<TSMetaMap>(MetaType.META_MAP)
        var map = maps[name]

        if (map == null) {
            map = TSMetaMapImpl(myProject, name, dom)
            maps[name] = map
        } else {
            map.merge(TSMetaMapImpl(myProject, name, dom))
        }

        return map;
    }

    fun <T : TSMetaClassifier<out DomElement>?> getAll(metaType: MetaType): Collection<T> = metaModel().getMetaType<T>(metaType).values

    private fun <T : TSMetaClassifier<out DomElement>?> findMetaByName(metaType: MetaType, name: String?): T? = metaModel().getMetaType<T>(metaType)[name]

    fun findMetaItemForDom(dom: ItemType): TSMetaItem? = findMetaItemByName(extractName(dom))

    fun findMetaItemByName(name: String?): TSMetaItem? = findMetaByName<TSMetaItem>(MetaType.META_ITEM, name)

    fun findMetaEnumByName(name: String): TSMetaEnum? = findMetaByName<TSMetaEnum>(MetaType.META_ENUM, name)

    fun findMetaAtomicByName(name: String): TSMetaAtomic? = findMetaByName<TSMetaAtomic>(MetaType.META_ATOMIC, name)

    fun findMetaCollectionByName(name: String): TSMetaCollection? = findMetaByName<TSMetaCollection>(MetaType.META_COLLECTION, name)

    fun findMetaMapByName(name: String): TSMetaMap? = findMetaByName<TSMetaMap>(MetaType.META_MAP, name)

    fun findRelationByName(name: String): List<TSMetaRelation> = CollectionUtils.emptyCollectionIfNull(metaModel().getReferences().values()).stream()
            .filter { obj: Any? -> Objects.nonNull(obj) }
            .map { referenceEnd: ReferenceEnd -> referenceEnd.owningReference }
            .filter { ref: TSMetaRelation -> name == ref.name }
            .collect(Collectors.toList())

    fun findMetaClassifierByName(name: String): TSMetaClassifier<out DomElement>? {
        var result: TSMetaClassifier<out DomElement>? = findMetaItemByName(name)
        if (result == null) {
            result = findMetaCollectionByName(name)
        }
        if (result == null) {
            result = findMetaEnumByName(name)
        }
        return result
    }

    fun collectReferencesForSourceType(source: TSMetaItem, out: MutableCollection<ReferenceEnd?>) {
        out.addAll(metaModel().getReference(source.name))
    }

    /**
     * Meta Model will be present in user data during re-creation of the TSMetaModel cache object
     * to eliminate recursion invocation of the TSMetaModel creation by the same Thread
     */
    fun metaModel(): TSMetaModel = myProject.getUserData(TSMetaModelAccessImpl.GLOBAL_META_MODEL_CACHE_KEY)
        ?: TSMetaModelAccess.getInstance(myProject).metaModel

    private fun registerReferenceEnd(ownerEnd: ReferenceEnd, targetEnd: ReferenceEnd) {
        if (!targetEnd.isNavigable) return

        val ownerTypeName = ownerEnd.typeName

        if (!StringUtil.isEmpty(ownerTypeName)) {
            metaModel().getReferences().putValue(ownerTypeName, targetEnd)
        }
    }

    companion object {
        fun getInstance(project: Project): TSMetaModelService {
            return project.getService(TSMetaModelService::class.java) as TSMetaModelService
        }
    }
}