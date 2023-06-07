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
package com.intellij.idea.plugin.hybris.system.type.meta.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.meta.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.xml.DomElement
import org.apache.commons.collections4.CollectionUtils
import java.util.*
import java.util.concurrent.Semaphore

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

    private val myGlobalMetaModel = TSGlobalMetaModel()
    private val myMessageBus = myProject.messageBus

    @Volatile
    private var building: Boolean = false
    private val semaphore = Semaphore(1)

    private val myGlobalMetaModelCache = CachedValuesManager.getManager(myProject).createCachedValue(
        {
            val localMetaModels = TSMetaModelCollector.getInstance(myProject).collectDependencies()
                .filter { obj: PsiFile? -> Objects.nonNull(obj) }
                .map { psiFile: PsiFile -> retrieveSingleMetaModelPerFile(psiFile) }
                .map { obj: CachedValue<TSMetaModel> -> obj.value }
                .sortedBy { !it.custom }

            val dependencies = localMetaModels
                .map { it.psiFile }
                .toTypedArray()

            TSMetaModelMerger.getInstance(myProject).merge(myGlobalMetaModel, localMetaModels)

            CachedValueProvider.Result.create(myGlobalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    private val task = object : Task.Backgroundable(myProject, message("hybris.ts.access.progress.title.building")) {
        override fun run(indicator: ProgressIndicator) {
            indicator.text2 = message("hybris.ts.access.progress.subTitle.waitingForIndex")

            DumbService.getInstance(project).runReadActionInSmartMode(
                Computable {
                    if (DumbService.isDumb(project)) throw ProcessCanceledException()
                    val lock = semaphore.tryAcquire()

                    if (lock) {
                        try {
                            building = true
                            val globalMetaModel = myGlobalMetaModelCache.value
                            building = false
                            myMessageBus.syncPublisher(TSMetaModelAccess.TOPIC).typeSystemChanged(globalMetaModel)
                        } finally {
                            semaphore.release();
                        }
                    }
                }
            )
        }
    }

    override fun getMetaModel(): TSGlobalMetaModel {
        if (building || DumbService.isDumb(myProject)) throw ProcessCanceledException()

        if (myGlobalMetaModelCache.hasUpToDateValue()) {
            return myGlobalMetaModelCache.value
        }

        building = true

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))

        throw ProcessCanceledException()
    }

    override fun <T : TSGlobalMetaClassifier<*>> getAll(metaType: TSMetaType) = getMetaModel().getMetaType<T>(metaType).values
    override fun getAll(): Collection<TSGlobalMetaClassifier<*>> = TSMetaType.values()
        .flatMap { getAll(it) }

    override fun findMetaForDom(dom: ItemType) = findMetaItemByName(TSMetaModelNameProvider.extract(dom))
    override fun findMetaForDom(dom: EnumType) = findMetaEnumByName(TSMetaModelNameProvider.extract(dom))

    override fun findMetaItemByName(name: String?) = findMetaByName<TSGlobalMetaItem>(TSMetaType.META_ITEM, name)
    override fun findMetaEnumByName(name: String?) = findMetaByName<TSGlobalMetaEnum>(TSMetaType.META_ENUM, name)
    override fun findMetaAtomicByName(name: String?) = findMetaByName<TSGlobalMetaAtomic>(TSMetaType.META_ATOMIC, name)
    override fun findMetaCollectionByName(name: String?) = findMetaByName<TSGlobalMetaCollection>(TSMetaType.META_COLLECTION, name)
    override fun findMetaMapByName(name: String?) = findMetaByName<TSGlobalMetaMap>(TSMetaType.META_MAP, name)
    override fun findMetaRelationByName(name: String?) = findMetaByName<TSGlobalMetaRelation>(TSMetaType.META_RELATION, name)

    override fun findRelationByName(name: String?) = CollectionUtils.emptyIfNull(getMetaModel().getAllRelations().values())
        .mapNotNull { metaRelationElement -> metaRelationElement.owner }
        .filter { ref: TSMetaRelation -> name == ref.name }

    override fun findMetaClassifierByName(name: String?): TSGlobalMetaClassifier<out DomElement>? = findMetaItemByName(name)
        ?: findMetaCollectionByName(name)
        ?: findMetaEnumByName(name)
        ?: findMetaMapByName(name)
        ?: findMetaAtomicByName(name)

    override fun getNextAvailableTypeCode(): Int = getMetaModel().getDeploymentTypeCodes().keys
        .asSequence()
        .filter { it < HybrisConstants.TS_TYPECODE_RANGE_PROCESSING.first }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_B2BCOMMERCE }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_COMMONS }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_XPRINT }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_PRINT }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_PROCESSING }
        .maxOf { it } + 1

    private fun <T : TSGlobalMetaClassifier<*>> findMetaByName(metaType: TSMetaType, name: String?): T? =
        getMetaModel().getMetaType<T>(metaType)[name]

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
                        CachedValueProvider.Result.create(TSMetaModelProcessor.getInstance(myProject).process(psiFile), psiFile)
                    } as Computable<CachedValueProvider.Result<TSMetaModel>>)
            }, false
        )
    }

    companion object {
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<TSMetaModel>>("SINGLE_TS_MODEL_CACHE")
    }
}