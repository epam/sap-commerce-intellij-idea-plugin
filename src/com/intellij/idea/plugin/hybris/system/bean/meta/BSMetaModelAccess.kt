/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.impl.BSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
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
import com.intellij.util.messages.Topic
import java.util.*
import java.util.concurrent.Semaphore

@Service(Service.Level.PROJECT)
class BSMetaModelAccess(private val myProject: Project) {

    companion object {
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<BSMetaModel>>("SINGLE_BEAN_SYSTEM_MODEL_CACHE")
        val TOPIC = Topic("HYBRIS_BEANS_LISTENER", BSChangeListener::class.java)

        fun getInstance(project: Project): BSMetaModelAccess = project.getService(BSMetaModelAccess::class.java)
    }

    private val myGlobalMetaModel = BSGlobalMetaModel()
    private val myMessageBus = myProject.messageBus

    @Volatile
    private var building: Boolean = false

    @Volatile
    private var initialized: Boolean = false
    private val semaphore = Semaphore(1)

    private val myGlobalMetaModelCache = CachedValuesManager.getManager(myProject).createCachedValue(
        {
            val localMetaModels = BSMetaModelCollector.getInstance(myProject).collectDependencies()
                .filter { obj: PsiFile? -> Objects.nonNull(obj) }
                .map { psiFile: PsiFile -> retrieveSingleMetaModelPerFile(psiFile) }
                .map { obj: CachedValue<BSMetaModel> -> obj.value }
                .sortedBy { !it.custom }

            val dependencies = localMetaModels
                .map { it.psiFile }
                .toTypedArray()
            BSMetaModelMerger.merge(myGlobalMetaModel, localMetaModels)

            CachedValueProvider.Result.create(myGlobalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    private val task = object : Task.Backgroundable(myProject, HybrisI18NBundleUtils.message("hybris.bs.access.progress.title.building")) {
        override fun run(indicator: ProgressIndicator) {
            indicator.text2 = HybrisI18NBundleUtils.message("hybris.bs.access.progress.subTitle.waitingForIndex")

            DumbService.getInstance(project).runReadActionInSmartMode(
                Computable {
                    val lock = semaphore.tryAcquire()

                    if (lock) {
                        try {
                            building = true
                            val globalMetaModel = myGlobalMetaModelCache.value
                            building = false
                            initialized = true

                            myMessageBus.syncPublisher(BSMetaModelAccess.TOPIC).beanSystemChanged(globalMetaModel)
                        } finally {
                            semaphore.release()
                        }
                    }
                }
            )
        }
    }

    fun isInitialized() = initialized

    fun initMetaModel() {
        building = true
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))
    }

    fun getMetaModel(): BSGlobalMetaModel {
        if (building || !initialized || DumbService.isDumb(myProject)) throw ProcessCanceledException()

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

    private fun retrieveSingleMetaModelPerFile(psiFile: PsiFile): CachedValue<BSMetaModel> {
        return Optional.ofNullable(psiFile.getUserData(SINGLE_MODEL_CACHE_KEY))
            .orElseGet {
                val cachedValue = createSingleMetaModelCachedValue(myProject, psiFile)
                psiFile.putUserData(SINGLE_MODEL_CACHE_KEY, cachedValue)
                cachedValue
            }
    }

    private fun createSingleMetaModelCachedValue(project: Project, psiFile: PsiFile): CachedValue<BSMetaModel> {
        return CachedValuesManager.getManager(project).createCachedValue(
            {
                ApplicationManager.getApplication().runReadAction(
                    Computable {
                        CachedValueProvider.Result.create(BSMetaModelProcessor.getInstance(myProject).process(psiFile), psiFile)
                    } as Computable<CachedValueProvider.Result<BSMetaModel>>)
            }, false
        )
    }
}