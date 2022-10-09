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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaReference.ReferenceEnd
import com.intellij.idea.plugin.hybris.type.system.meta.impl.*
import com.intellij.idea.plugin.hybris.type.system.model.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile

class TSMetaService {

    fun extractName(dom: ItemType): String? = dom.code.value
    fun extractName(dom: EnumType): String? = dom.code.value
    fun extractName(dom: CollectionType): String? = dom.code.value
    fun extractName(dom: Relation): String? = dom.code.value
    fun extractName(dom: AtomicType): String? = dom.clazz.value

    fun findOrCreate(metaModel: TSMetaModel, psiFile: PsiFile, domItemType: ItemType): TSMetaClass? {
        val name = extractName(domItemType) ?: return null
        val typeCode = domItemType.deployment.typeCode.stringValue
        val classes = TSMetaCache.getInstance(psiFile).getMetaType<TSMetaClassImpl>(MetaType.META_CLASS)
        var impl = classes[name]

        if (impl == null) {
            impl = TSMetaClassImpl(metaModel, name, typeCode, domItemType)
            classes[name] = impl
        } else {
            impl.addDomRepresentation(domItemType)
        }
        return impl
    }

    fun findOrCreate(metaModel : TSMetaModel, psiFile: PsiFile, domEnumType: EnumType): TSMetaEnum? {
        val name = extractName(domEnumType) ?: return null
        val enums = TSMetaCache.getInstance(psiFile).getMetaType<TSMetaEnum>(MetaType.META_ENUM)
        var impl = enums[name]

        if (impl == null) {
            impl = TSMetaEnumImpl(name, domEnumType)
            enums[name] = impl
        }
        return impl
    }

    fun findOrCreate(metaModel : TSMetaModel, psiFile: PsiFile, atomicType: AtomicType): TSMetaAtomic? {
        val clazzName = extractName(atomicType) ?: return null

        return TSMetaCache.getInstance(psiFile).getMetaType<TSMetaAtomic>(MetaType.META_ATOMIC)
            .computeIfAbsent(clazzName)
            { key: String -> TSMetaAtomicImpl(key, atomicType) }
    }

    fun findOrCreate(metaModel : TSMetaModel, psiFile: PsiFile, domCollectionType: CollectionType): TSMetaCollection? {
        val name = extractName(domCollectionType) ?: return null

        return TSMetaCache.getInstance(psiFile).getMetaType<TSMetaCollection>(MetaType.META_COLLECTION)
            .computeIfAbsent(name)
            { key: String? -> TSMetaCollectionImpl(metaModel, key, domCollectionType) }
    }

    fun findOrCreate(metaModel : TSMetaModel, psiFile: PsiFile, domRelationType: Relation) : TSMetaReference? {
        val name = extractName(domRelationType)
        val typeCode = domRelationType.deployment.typeCode.stringValue

        if (name == null || typeCode == null) return null

        return TSMetaCache.getInstance(psiFile).getMetaType<TSMetaReference>(MetaType.META_RELATION)
            .computeIfAbsent(name) { key: String ->
                val impl: TSMetaReference = TSMetaReferenceImpl(metaModel, key, typeCode, domRelationType)
                registerReferenceEnd(psiFile, impl.source, impl.target)
                registerReferenceEnd(psiFile, impl.target, impl.source)
                impl
            }

    }

    private fun registerReferenceEnd(psiFile: PsiFile, ownerEnd: ReferenceEnd, targetEnd: ReferenceEnd) {
        if (!targetEnd.isNavigable) return

        val ownerTypeName = ownerEnd.typeName

        if (!StringUtil.isEmpty(ownerTypeName)) {
            TSMetaCache.getInstance(psiFile).referencesBySourceTypeName.putValue(ownerTypeName, targetEnd)
        }
    }

    companion object {
        fun getInstance(project: Project): TSMetaService {
            return project.getService(TSMetaService::class.java) as TSMetaService
        }
    }
}