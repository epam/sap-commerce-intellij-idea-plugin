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
package com.intellij.idea.plugin.hybris.beans.meta.impl

import com.intellij.idea.plugin.hybris.beans.meta.*
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaBean
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaEnum
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaType
import com.intellij.idea.plugin.hybris.beans.model.Bean
import com.intellij.idea.plugin.hybris.beans.model.Enum
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
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock

class BeansMetaModelAccessImpl(private val myProject: Project) : BeansMetaModelAccess {

    private val myMessageBus = myProject.messageBus

    private val myGlobalMetaModel = CachedValuesManager.getManager(myProject).createCachedValue(
        {
            val localMetaModels = BeansMetaModelCollector.getInstance(myProject).collectDependencies()
                .filter { obj: PsiFile? -> Objects.nonNull(obj) }
                .map { psiFile: PsiFile -> retrieveSingleMetaModelPerFile(psiFile) }
                .map { obj: CachedValue<BeansMetaModel> -> obj.value }
                .sortedBy { !it.custom }

            val dependencies = localMetaModels
                .map { it.psiFile }
                .toTypedArray()
            val globalMetaModel = BeansMetaModelMerger.getInstance(myProject).merge(localMetaModels)

            CachedValueProvider.Result.create(globalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }
        , false
    )

    override fun getMetaModel(): BeansGlobalMetaModel {
        if (myGlobalMetaModel.hasUpToDateValue() || lock.isWriteLocked || writeLock.isHeldByCurrentThread) {
            return readMetaModelWithLock()
        }
        return writeMetaModelWithLock()
    }

    override fun <T : BeansGlobalMetaClassifier<*>> getAll(metaType: BeansMetaType) = getMetaModel().getMetaType<T>(metaType).values

    override fun findMetaForDom(dom: Enum) = findMetaEnumByName(BeansMetaModelNameProvider.extract(dom))
    override fun findMetasForDom(dom: Bean): List<BeansGlobalMetaBean> {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return emptyList()
        return listOfNotNull(
            findMetaByName(BeansMetaType.META_BEAN, name),
            findMetaByName(BeansMetaType.META_WS_BEAN, name),
            findMetaByName(BeansMetaType.META_EVENT, name)
        )
    }

    override fun findMetaEnumByName(name: String?) = findMetaByName<BeansGlobalMetaEnum>(BeansMetaType.META_ENUM, name)

    private fun <T : BeansGlobalMetaClassifier<*>> findMetaByName(metaType: BeansMetaType, name: String?): T? = getMetaModel().getMetaType<T>(metaType)[name]

    // parameter for Meta Model cached value is not required, we have to pass new cache holder only during write process
    private fun readMetaModelWithLock(): BeansGlobalMetaModel {
        try {
            readLock.lock()
            if (lock.isWriteLocked && writeLock.isHeldByCurrentThread) {
                // Same thread cannot be used to read and write Beans Model, double check all getters
                throw ProcessCanceledException()
            }
            return myGlobalMetaModel.value
        } finally {
            readLock.unlock()
        }
    }

    private fun writeMetaModelWithLock(): BeansGlobalMetaModel {
        try {
            writeLock.lock()
            val globalMetaModel = myGlobalMetaModel.value
            myMessageBus.syncPublisher(topic).beansChanged(globalMetaModel)

            return globalMetaModel
        } finally {
            writeLock.unlock()
        }
    }

    private fun retrieveSingleMetaModelPerFile(psiFile: PsiFile): CachedValue<BeansMetaModel> {
        return Optional.ofNullable(psiFile.getUserData(SINGLE_MODEL_CACHE_KEY))
            .orElseGet {
                val cachedValue = createSingleMetaModelCachedValue(myProject, psiFile)
                psiFile.putUserData(SINGLE_MODEL_CACHE_KEY, cachedValue)
                cachedValue
            }
    }

    private fun createSingleMetaModelCachedValue(project: Project, psiFile: PsiFile): CachedValue<BeansMetaModel> {
        return CachedValuesManager.getManager(project).createCachedValue(
            {
                ApplicationManager.getApplication().runReadAction(
                    Computable {
                        CachedValueProvider.Result.create(BeansMetaModelProcessor.getInstance(myProject).process(psiFile), psiFile)
                    } as Computable<CachedValueProvider.Result<BeansMetaModel>>)
            }, false
        )
    }

    companion object {
        val topic = Topic("HYBRIS_BEANS_LISTENER", BeansListener::class.java)
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<BeansMetaModel>>("SINGLE_BEANS_MODEL_CACHE")
        private val lock = ReentrantReadWriteLock()
        private val readLock = lock.readLock()
        private val writeLock = lock.writeLock()
    }
}