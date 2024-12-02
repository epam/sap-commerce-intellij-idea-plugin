/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.root
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.system.type.meta.impl.TSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.messages.Topic
import com.intellij.util.xml.DomElement
import kotlinx.coroutines.*
import org.apache.commons.collections4.CollectionUtils
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.time.measureTime

/**
 * Global Meta Model can be retrieved at any time and will ensure that only a single Thread can perform its initialization/update
 *
 * The main idea is that we have two levels of Meta Model cache:
 * 1. Global Meta Model cached at Project level with dependencies to all items.xml files in the Project.
 * - processing of the dependant PsiFiles is ignored and done during retrieval from the PsiFile cache
 * - once all dependant PsiFiles processed, each Meta Model will be merged into single one
 * 2. PsiFile (items.xml) specific cache
 * - retrieving of that cache also performs processing of the PsiFile and pre-filling into MetaModel caches
 *
 * It is quite important to take into account the possibility of interruption of the process, especially during Inspection and other heavy operations
 */
@Service(Service.Level.PROJECT)
class TSMetaModelAccess(private val project: Project, private val coroutineScope: CoroutineScope) {

    companion object {
        val TOPIC = Topic("HYBRIS_TYPE_SYSTEM_LISTENER", TSChangeListener::class.java)
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<TSMetaModel>>("SINGLE_TS_MODEL_CACHE")

        @JvmStatic
        fun getInstance(project: Project): TSMetaModelAccess = project.getService(TSMetaModelAccess::class.java)
    }

    private val myGlobalMetaModel = TSGlobalMetaModel()
    private val myMessageBus = project.messageBus
    private val myReservedTypeCodes by lazy {
        ModuleManager.getInstance(project)
            .modules
            .firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_CORE }
            ?.root()
            ?.resolve(HybrisConstants.RESERVED_TYPE_CODES_FILE)
            ?.takeIf { it.exists() }
            ?.let {
                it.inputStream().use { fis ->
                    Properties().also { p -> p.load(fis) }
                }
            }
            ?.entries
            ?.mapNotNull {
                val key = it.key.toString().toIntOrNull() ?: return@mapNotNull null
                key to it.value.toString()
            }
            ?.associate { it.first to it.second }
            ?: emptyMap()
    }

    @Volatile
    private var building: Boolean = false

    @Volatile
    private var initialized: Boolean = false

    private val myGlobalMetaModelCache = CachedValuesManager.getManager(project).createCachedValue(
        {
            val localMetaModels = runBlocking {
                withBackgroundProgress(project, "Re-building Type System...", true) {
                    val collectedDependencies = TSMetaModelCollector.getInstance(project).collectDependencies()

                    val localMetaModels = reportProgress(collectedDependencies.size) { progressReporter ->
                        collectedDependencies
                            .map {
                                progressReporter.sizedStep(1, "Processing: ${it.name}...") {
                                    this.async {
                                        retrieveSingleMetaModelPerFile(it)
                                    }
                                }
                            }
                            .awaitAll()
                            .sortedBy { !it.custom }
                    }

                    TSMetaModelMerger.getInstance(project).merge(myGlobalMetaModel, localMetaModels)

                    localMetaModels
                }
            }

            val dependencies = localMetaModels
                .map { it.psiFile }
                .toTypedArray()

            CachedValueProvider.Result.create(myGlobalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    fun isInitialized() = initialized

    fun initMetaModel() {
        building = true

        coroutineScope
            .launch(Dispatchers.IO) {
                val measureTime = measureTime {
                    myGlobalMetaModelCache.value
                }

                withContext(Dispatchers.EDT) {
                    Notifications.create(
                        NotificationType.INFORMATION,
                        "TS loaded in: $measureTime"
                    )
                        .notify(project)
                }
            }
            .invokeOnCompletion {
                building = false
                initialized = true

                myMessageBus.syncPublisher(TOPIC).typeSystemChanged(myGlobalMetaModel)
            }
    }

    fun getMetaModel(): TSGlobalMetaModel {
        if (building || !initialized || DumbService.isDumb(project)) throw ProcessCanceledException()

        if (myGlobalMetaModelCache.hasUpToDateValue()) {
            return myGlobalMetaModelCache.value
        }

        initMetaModel()

        throw ProcessCanceledException()
    }

    fun <T : TSGlobalMetaClassifier<*>> getAll(metaType: TSMetaType) = getMetaModel().getMetaType<T>(metaType).values
    fun getAllOf(vararg metaTypes: TSMetaType): Collection<TSGlobalMetaClassifier<*>> = (metaTypes
        .takeIf { it.isNotEmpty() }
        ?: TSMetaType.entries.toTypedArray()
        )
        .flatMap { getAll(it) }

    fun getAll(): Collection<TSGlobalMetaClassifier<*>> = TSMetaType.entries
        .flatMap { getAll(it) }

    fun findMetaForDom(dom: ItemType) = findMetaItemByName(TSMetaModelNameProvider.extract(dom))
    fun findMetaForDom(dom: EnumType) = findMetaEnumByName(TSMetaModelNameProvider.extract(dom))

    fun findMetaItemByName(name: String?) = findMetaByName<TSGlobalMetaItem>(TSMetaType.META_ITEM, name)
    fun findMetaEnumByName(name: String?) = findMetaByName<TSGlobalMetaEnum>(TSMetaType.META_ENUM, name)
    fun findMetaAtomicByName(name: String?) = findMetaByName<TSGlobalMetaAtomic>(TSMetaType.META_ATOMIC, name)
    fun findMetaCollectionByName(name: String?) = findMetaByName<TSGlobalMetaCollection>(TSMetaType.META_COLLECTION, name)
    fun findMetaMapByName(name: String?) = findMetaByName<TSGlobalMetaMap>(TSMetaType.META_MAP, name)
    fun findMetaRelationByName(name: String?) = findMetaByName<TSGlobalMetaRelation>(TSMetaType.META_RELATION, name)

    fun findRelationByName(name: String?) = CollectionUtils.emptyIfNull(getMetaModel().getAllRelations().values())
        .mapNotNull { metaRelationElement -> metaRelationElement.owner }
        .filter { ref: TSMetaRelation -> name == ref.name }

    fun findMetaClassifierByName(name: String?): TSGlobalMetaClassifier<out DomElement>? = findMetaItemByName(name)
        ?: findMetaCollectionByName(name)
        ?: findMetaRelationByName(name)
        ?: findMetaEnumByName(name)
        ?: findMetaMapByName(name)
        ?: findMetaAtomicByName(name)

    fun getNextAvailableTypeCode(): Int? {
        val projectTypeCodes = getMetaModel().getDeploymentTypeCodes().keys
        val reservedTypesCodes = getReservedTypeCodes().keys
        val keys = projectTypeCodes + reservedTypesCodes

        return (10100..Short.MAX_VALUE)
            .asSequence()
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_COMMONS }
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_XPRINT }
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_PRINT }
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_PROCESSING }
            .firstOrNull { !keys.contains(it) }
    }

    fun getReservedTypeCodes() = myReservedTypeCodes

    private fun <T : TSGlobalMetaClassifier<*>> findMetaByName(metaType: TSMetaType, name: String?): T? =
        getMetaModel().getMetaType<T>(metaType)[name]

    private fun retrieveSingleMetaModelPerFile(psiFile: PsiFile): TSMetaModel = CachedValuesManager.getManager(project).getCachedValue(
        psiFile, SINGLE_MODEL_CACHE_KEY,
        {
            val value = runBlocking {
                TSMetaModelProcessor.getInstance(project).process(this, psiFile)
            }

            CachedValueProvider.Result.create(value, psiFile)
        },
        false
    )
}