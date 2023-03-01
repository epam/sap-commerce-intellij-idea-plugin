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
package com.intellij.idea.plugin.hybris.system.bean.meta.impl

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.bean.meta.*
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.impl.TSMetaModelAccessImpl
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
import com.intellij.util.messages.Topic
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock

class BSMetaModelAccessImpl(private val myProject: Project) : BSMetaModelAccess {

    private val myMessageBus = myProject.messageBus
    @Volatile
    private var building: Boolean = false

    private val myGlobalMetaModel = CachedValuesManager.getManager(myProject).createCachedValue(
        {
            val localMetaModels = BSMetaModelCollector.getInstance(myProject).collectDependencies()
                .filter { obj: PsiFile? -> Objects.nonNull(obj) }
                .map { psiFile: PsiFile -> retrieveSingleMetaModelPerFile(psiFile) }
                .map { obj: CachedValue<BSMetaModel> -> obj.value }
                .sortedBy { !it.custom }

            val dependencies = localMetaModels
                .map { it.psiFile }
                .toTypedArray()
            val globalMetaModel = BSMetaModelMerger.getInstance(myProject).merge(localMetaModels)

            CachedValueProvider.Result.create(globalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    override fun getMetaModel(): BSGlobalMetaModel {
        if (building || DumbService.isDumb(myProject)) throw ProcessCanceledException()

        if (myGlobalMetaModel.hasUpToDateValue()) {
            return myGlobalMetaModel.value
        }

        val task = object : Task.Backgroundable(myProject, message("hybris.bs.access.progress.title.building")) {
            override fun run(indicator: ProgressIndicator) {
                DumbService.getInstance(project).runReadActionInSmartMode(
                    Computable {
                        if (DumbService.isDumb(project)) throw ProcessCanceledException()

                        building = true
                        val globalMetaModel = myGlobalMetaModel.value
                        building = false
                        myMessageBus.syncPublisher(topic).beanSystemChanged(globalMetaModel)
                    }
                )
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))

        throw ProcessCanceledException()
    }

    override fun <T : BSGlobalMetaClassifier<*>> getAll(metaType: BSMetaType) = getMetaModel().getMetaType<T>(metaType).values

    override fun findMetaForDom(dom: Enum) = findMetaEnumByName(BSMetaModelNameProvider.extract(dom))
    override fun findMetasForDom(dom: Bean): List<BSGlobalMetaBean> {
        val name = BSMetaModelNameProvider.extract(dom) ?: return emptyList()
        return findMetaBeansByName(name)
    }

    override fun findMetaBeansByName(name: String): List<BSGlobalMetaBean> {
        return listOfNotNull(
            findMetaByName(BSMetaType.META_BEAN, name),
            findMetaByName(BSMetaType.META_WS_BEAN, name),
            findMetaByName(BSMetaType.META_EVENT, name)
        )
    }

    override fun findMetasByName(name: String): List<BSGlobalMetaClassifier<*>> {
        return listOfNotNull(
            findMetaByName(BSMetaType.META_ENUM, name),
            findMetaByName(BSMetaType.META_BEAN, name),
            findMetaByName(BSMetaType.META_WS_BEAN, name),
            findMetaByName(BSMetaType.META_EVENT, name)
        )
    }

    override fun findMetaEnumByName(name: String?) = findMetaByName<BSGlobalMetaEnum>(BSMetaType.META_ENUM, name)

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

    companion object {
        val topic = Topic("HYBRIS_BEANS_LISTENER", BSChangeListener::class.java)
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<BSMetaModel>>("SINGLE_BEAN_SYSTEM_MODEL_CACHE")
    }
}