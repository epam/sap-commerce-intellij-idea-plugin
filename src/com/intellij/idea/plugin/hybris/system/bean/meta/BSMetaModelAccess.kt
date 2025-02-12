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
package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.system.bean.meta.impl.BSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
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
import kotlinx.coroutines.*

@Service(Service.Level.PROJECT)
class BSMetaModelAccess(private val project: Project, private val coroutineScope: CoroutineScope) {

    companion object {
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<BSMetaModel>>("SINGLE_BEAN_SYSTEM_MODEL_CACHE")
        val TOPIC = Topic("HYBRIS_BEANS_LISTENER", BSChangeListener::class.java)

        fun getInstance(project: Project): BSMetaModelAccess = project.getService(BSMetaModelAccess::class.java)
    }

    private val myGlobalMetaModel = BSGlobalMetaModel()
    private val myMessageBus = project.messageBus

    @Volatile
    private var building: Boolean = false

    @Volatile
    private var initialized: Boolean = false

    private val myGlobalMetaModelCache = CachedValuesManager.getManager(project).createCachedValue(
        {
            val localMetaModels = runBlocking {
                withBackgroundProgress(project, "Re-building Bean System...", true) {
                    val collectedDependencies = BSMetaModelCollector.getInstance(project).collectDependencies()

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

                    BSMetaModelMerger.merge(myGlobalMetaModel, localMetaModels)

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
                readAction {
                    myGlobalMetaModelCache.value
                }
            }
            .invokeOnCompletion {
                building = false
                initialized = true

                myMessageBus.syncPublisher(TOPIC).beanSystemChanged(myGlobalMetaModel)
            }
    }

    fun getMetaModel(): BSGlobalMetaModel {
        if (building || !initialized || DumbService.isDumb(project)) throw ProcessCanceledException()

        if (myGlobalMetaModelCache.hasUpToDateValue()) {
            return myGlobalMetaModelCache.value
        }

        initMetaModel()

        throw ProcessCanceledException()
    }

    fun getAllBeans() = getAll<BSGlobalMetaBean>(BSMetaType.META_BEAN) +
        getAll(BSMetaType.META_WS_BEAN) +
        getAll(BSMetaType.META_EVENT)

    fun getAllEnums() = getAll<BSGlobalMetaEnum>(BSMetaType.META_ENUM)

    fun <T : BSGlobalMetaClassifier<*>> getAll(metaType: BSMetaType): Collection<T> = getMetaModel().getMetaType<T>(metaType).values

    fun findMetaForDom(dom: Enum) = findMetaEnumByName(BSMetaModelNameProvider.extract(dom))
    fun findMetasForDom(dom: Bean): List<BSGlobalMetaBean> = BSMetaModelNameProvider.extract(dom)
        ?.let { findMetaBeansByName(it) }
        ?: emptyList()

    fun findMetaBeanByName(name: String?) = listOfNotNull(
        findMetaByName(BSMetaType.META_BEAN, name),
        findMetaByName(BSMetaType.META_WS_BEAN, name),
        findMetaByName(BSMetaType.META_EVENT, name)
    )
        .map { it as? BSGlobalMetaBean }
        .firstOrNull()

    fun findMetaBeansByName(name: String?): List<BSGlobalMetaBean> = listOfNotNull(
        findMetaByName(BSMetaType.META_BEAN, name),
        findMetaByName(BSMetaType.META_WS_BEAN, name),
        findMetaByName(BSMetaType.META_EVENT, name)
    )

    fun findMetasByName(name: String): List<BSGlobalMetaClassifier<*>> = listOfNotNull(
        findMetaByName(BSMetaType.META_ENUM, name),
        findMetaByName(BSMetaType.META_BEAN, name),
        findMetaByName(BSMetaType.META_WS_BEAN, name),
        findMetaByName(BSMetaType.META_EVENT, name)
    )

    fun findMetaEnumByName(name: String?) = findMetaByName<BSGlobalMetaEnum>(BSMetaType.META_ENUM, name)

    private fun <T : BSGlobalMetaClassifier<*>> findMetaByName(metaType: BSMetaType, name: String?): T? =
        getMetaModel().getMetaType<T>(metaType)[name]

    private fun retrieveSingleMetaModelPerFile(psiFile: PsiFile): BSMetaModel = CachedValuesManager.getManager(project).getCachedValue(
        psiFile, SINGLE_MODEL_CACHE_KEY,
        {
            val value = runBlocking {
                BSMetaModelProcessor.getInstance(project).process(this, psiFile)
            }

            CachedValueProvider.Result.create(value, psiFile)
        },
        false
    )
}