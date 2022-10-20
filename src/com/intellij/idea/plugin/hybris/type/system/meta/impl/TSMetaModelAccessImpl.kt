/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

import com.intellij.idea.plugin.hybris.common.utils.CollectionUtils
import com.intellij.idea.plugin.hybris.type.system.meta.*
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation.TSMetaRelationElement
import com.intellij.idea.plugin.hybris.type.system.model.ItemType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.messages.Topic
import com.intellij.util.xml.DomElement
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Collectors

/**
 * Global Meta Model can be retrieved at any time and will ensure that only single Thread can perform its initialization/update
 *
 *
 * Main idea is that we have two levels of Meta Model cache:
 * 1. Global Meta Model cached at Project level with dependencies to all items.xml files in the Project.
 * - processing of the dependant PsiFiles is ignored and done during retrieval from the PsiFile cache
 * - once all dependant PsiFiles processed, each Meta Model will be merged into single one
 * 2. PsiFile (items.xml) specific cache
 * - retrieving of that cache also performs processing of the PsiFile and pre-filling into MetaModel caches
 *
 * It is quite important to take into account possibility of interruption of the process, especially during Inspection and other heavy operations
 */
class TSMetaModelAccessImpl(private val myProject: Project) : TSMetaModelAccess {

    private val myMessageBus = myProject.messageBus

    private val myGlobalMetaModel = CachedValuesManager.getManager(myProject).createCachedValue(
        {
            val metaModels = TSMetaModelCollector(myProject).collectDependencies()
                .filter { obj: PsiFile? -> Objects.nonNull(obj) }
                .map { psiFile: PsiFile -> retrieveSingleMetaModelPerFile(psiFile) }
                .map { obj: CachedValue<TSMetaModel> -> obj.value }

            val dependencies = metaModels
                .map { it.psiFile }
                .toTypedArray()
            val globalMetaModel = TSGlobalMetaModel().merge(metaModels)

            CachedValueProvider.Result.create(globalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }
        , false
    )

    override fun getMetaModel(): TSGlobalMetaModel {
        if (myGlobalMetaModel.hasUpToDateValue() || lock.isWriteLocked || writeLock.isHeldByCurrentThread) {
            return readMetaModelWithLock()
        }
        return writeMetaModelWithLock()
    }

    override fun <T : TSMetaClassifier<out DomElement>?> getAll(metaType: MetaType): Collection<T> = getMetaModel().getMetaType<T>(metaType).values

    override fun findMetaItemForDom(dom: ItemType): TSMetaItem? = findMetaItemByName(extractName(dom))

    override fun findMetaItemByName(name: String?): TSMetaItem? = findMetaByName<TSMetaItem>(MetaType.META_ITEM, name)

    override fun findMetaEnumByName(name: String?): TSMetaEnum? = findMetaByName<TSMetaEnum>(MetaType.META_ENUM, name)

    override fun findMetaAtomicByName(name: String?): TSMetaAtomic? = findMetaByName<TSMetaAtomic>(MetaType.META_ATOMIC, name)

    override fun findMetaCollectionByName(name: String?): TSMetaCollection? = findMetaByName<TSMetaCollection>(MetaType.META_COLLECTION, name)

    override fun findMetaMapByName(name: String?): TSMetaMap? = findMetaByName<TSMetaMap>(MetaType.META_MAP, name)

    override fun findRelationByName(name: String?): List<TSMetaRelation> = CollectionUtils.emptyCollectionIfNull(getMetaModel().getReferences().values()).stream()
        .filter { obj: Any? -> Objects.nonNull(obj) }
        .map { metaRelationElement -> metaRelationElement.owningRelation }
        .filter { ref: TSMetaRelation -> name == ref.name }
        .collect(Collectors.toList())

    override fun findMetaClassifierByName(name: String?): TSMetaClassifier<out DomElement>? {
        var result: TSMetaClassifier<out DomElement>? = findMetaItemByName(name)
        if (result == null) {
            result = findMetaCollectionByName(name)
        }
        if (result == null) {
            result = findMetaEnumByName(name)
        }
        return result
    }

    override fun collectReferencesForSourceType(source: TSMetaItem, out: LinkedList<TSMetaRelationElement?>) {
        out.addAll(getMetaModel().getReference(source.name))
    }

    private fun extractName(dom: ItemType): String? = dom.code.value

    private fun <T : TSMetaClassifier<out DomElement>?> findMetaByName(metaType: MetaType, name: String?): T? = getMetaModel().getMetaType<T>(metaType)[name]

    // parameter for Meta Model cached value is not required, we have to pass new cache holder only during write process
    private fun readMetaModelWithLock(): TSGlobalMetaModel {
        try {
            readLock.lock()
            if (lock.isWriteLocked && writeLock.isHeldByCurrentThread) {
                // Same thread cannot be used to read and write TypeSystem Model, double check all getters
                throw ProcessCanceledException()
            }
            return myGlobalMetaModel.value
        } finally {
            readLock.unlock()
        }
    }

    private fun writeMetaModelWithLock(): TSGlobalMetaModel {
        try {
            writeLock.lock()
            val globalMetaModel = myGlobalMetaModel.value
            myMessageBus.syncPublisher(topic).typeSystemChanged(globalMetaModel)

            return globalMetaModel
        } finally {
            writeLock.unlock()
        }
    }

    private fun retrieveSingleMetaModelPerFile(psiFile: PsiFile): CachedValue<TSMetaModel> {
        return Optional.ofNullable(psiFile.getUserData(SINGLE_MODEL_CACHE_KEY))
            .orElseGet {
                val cachedValue = createSingleMetaModelCachedValue(myProject, psiFile)
                psiFile.putUserData(SINGLE_MODEL_CACHE_KEY, cachedValue)
                cachedValue
            }
    }

    private fun createSingleMetaModelCachedValue(project: Project, psiFile: PsiFile): CachedValue<TSMetaModel> {
        return CachedValuesManager.getManager(project).createCachedValue(
            {
                ApplicationManager.getApplication().runReadAction(
                    Computable {
                        val processor = TSMetaModelProcessor(myProject)
                        processor.process(psiFile)
                        CachedValueProvider.Result.create(processor.processedMetaModel, psiFile)
                    } as Computable<CachedValueProvider.Result<TSMetaModel>>)
            }, false
        )
    }

    companion object {
        val topic = Topic("HYBRIS_TYPE_SYSTEM_LISTENER", TSListener::class.java)
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<TSMetaModel>>("SINGLE_TS_MODEL_CACHE")
        private val lock = ReentrantReadWriteLock()
        private val readLock = lock.readLock()
        private val writeLock = lock.writeLock()
    }
}