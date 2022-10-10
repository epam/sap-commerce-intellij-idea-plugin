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
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaReference.ReferenceEnd
import com.intellij.idea.plugin.hybris.type.system.meta.impl.*
import com.intellij.idea.plugin.hybris.type.system.model.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.xml.DomElement
import java.util.*
import java.util.stream.Collectors

class TSMetaService(val myProject: Project) {

    private fun extractName(dom: ItemType): String? = dom.code.value
    private fun extractName(dom: EnumType): String? = dom.code.value
    private fun extractName(dom: CollectionType): String? = dom.code.value
    private fun extractName(dom: Relation): String? = dom.code.value
    private fun extractName(dom: AtomicType): String? = dom.clazz.value

    fun findOrCreate(metaModel: TSMetaModel, domItemType: ItemType): TSMetaClass? {
        val name = extractName(domItemType) ?: return null
        val typeCode = domItemType.deployment.typeCode.stringValue
        val classes = cache().getMetaType<TSMetaClassImpl>(MetaType.META_CLASS)
        var impl = classes[name]

        if (impl == null) {
            impl = TSMetaClassImpl(myProject, metaModel, name, typeCode, domItemType)
            classes[name] = impl
        } else {
            impl.addDomRepresentation(domItemType)
        }
        return impl
    }

    fun findOrCreate(domEnumType: EnumType): TSMetaEnum? {
        val name = extractName(domEnumType) ?: return null
        val enums = cache().getMetaType<TSMetaEnum>(MetaType.META_ENUM)
        var impl = enums[name]

        if (impl == null) {
            impl = TSMetaEnumImpl(myProject, name, domEnumType)
            enums[name] = impl
        }
        return impl
    }

    fun findOrCreate(atomicType: AtomicType): TSMetaAtomic? {
        val clazzName = extractName(atomicType) ?: return null

        return cache().getMetaType<TSMetaAtomic>(MetaType.META_ATOMIC)
            .computeIfAbsent(clazzName)
            { key: String -> TSMetaAtomicImpl(myProject, key, atomicType) }
    }

    fun findOrCreate(metaModel: TSMetaModel, domCollectionType: CollectionType): TSMetaCollection? {
        val name = extractName(domCollectionType) ?: return null

        return cache().getMetaType<TSMetaCollection>(MetaType.META_COLLECTION)
            .computeIfAbsent(name)
            { key: String? -> TSMetaCollectionImpl(myProject, metaModel, key, domCollectionType) }
    }

    fun findOrCreate(metaModel: TSMetaModel, domRelationType: Relation) : TSMetaReference? {
        val name = extractName(domRelationType)
        val typeCode = domRelationType.deployment.typeCode.stringValue

        if (name == null || typeCode == null) return null

        return cache().getMetaType<TSMetaReference>(MetaType.META_RELATION)
            .computeIfAbsent(name) { key: String ->
                val impl: TSMetaReference = TSMetaReferenceImpl(myProject, metaModel, key, typeCode, domRelationType)
                registerReferenceEnd(impl.source, impl.target)
                registerReferenceEnd(impl.target, impl.source)
                impl
            }

    }

    @Suppress("UNCHECKED_CAST")
    fun <T : TSMetaClassifier<out DomElement>?> getAll(metaType: MetaType): List<T> = cache().getMetaType<T>(metaType).values as List<T>

    fun <T : TSMetaClassifier<out DomElement>?> findMetaByName(metaType: MetaType, name: String?): T? = cache().getMetaType<T>(metaType)[name]

    fun findMetaClassForDom(dom: ItemType): TSMetaClass? = findMetaClassByName(extractName(dom))

    fun findMetaClassByName(name: String?): TSMetaClass? =  findMetaByName<TSMetaClass>(MetaType.META_CLASS, name)

    fun findMetaEnumByName(name: String): TSMetaEnum? = findMetaByName<TSMetaEnum>(MetaType.META_ENUM, name)

    fun findMetaAtomicByName(name: String): TSMetaAtomic? = findMetaByName<TSMetaAtomic>(MetaType.META_ATOMIC, name)

    fun findMetaCollectionByName(name: String): TSMetaCollection? = findMetaByName<TSMetaCollection>(MetaType.META_COLLECTION, name)

    fun findRelationByName(name: String) : List<TSMetaReference> {
        return CollectionUtils.emptyCollectionIfNull(cache().referencesBySourceTypeName.values()).stream()
            .filter { obj: Any? -> Objects.nonNull(obj) }
            .map { referenceEnd: ReferenceEnd -> referenceEnd.owningReference }
            .filter { ref: TSMetaReference -> name == ref.name }
            .collect(Collectors.toList())
    }

    fun findMetaClassifierByName(name: String): TSMetaClassifier<out DomElement>? {
        var result: TSMetaClassifier<out DomElement>? = findMetaClassByName(name)
        if (result == null) {
            result = findMetaCollectionByName(name)
        }
        if (result == null) {
            result = findMetaEnumByName(name)
        }
        return result
    }

    fun collectReferencesForSourceType(source: TSMetaClass, out: MutableCollection<ReferenceEnd?>) {
        out.addAll(cache().referencesBySourceTypeName[source.name])
    }

    private fun cache(): TSMetaCache = TSMetaModelAccess.getInstance(myProject).metaCache

    private fun registerReferenceEnd(ownerEnd: ReferenceEnd, targetEnd: ReferenceEnd) {
        if (!targetEnd.isNavigable) return

        val ownerTypeName = ownerEnd.typeName

        if (!StringUtil.isEmpty(ownerTypeName)) {
            cache().referencesBySourceTypeName.putValue(ownerTypeName, targetEnd)
        }
    }

    companion object {
        fun getInstance(project: Project): TSMetaService {
            return project.getService(TSMetaService::class.java) as TSMetaService
        }
    }
}