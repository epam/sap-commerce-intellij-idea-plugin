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
package com.intellij.idea.plugin.hybris.system.cockpitng.meta.impl

import com.intellij.idea.plugin.hybris.system.SystemChangeListener
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.*
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

/**
 * Global Meta Model can be retrieved at any time and will ensure that only single Thread can perform its initialization/update
 *
 *
 * Main idea is that we have two levels of Meta Model cache:
 * 1. Global Meta Model cached at Project level with dependencies to all items.xml files in the Project.
 * - processing of the dependant PsiFiles is ignored and done during retrieval from the PsiFile cache
 * - once all dependant PsiFiles processed, each Meta Model will be merged into single one
 * 2. PsiFile (-config.xml) specific cache
 * - retrieving of that cache also performs processing of the PsiFile and pre-filling into MetaModel caches
 *
 * It is quite important to take into account possibility of interruption of the process, especially during Inspection and other heavy operations
 */
class CngMetaModelAccessImpl(private val myProject: Project) : CngMetaModelAccess {

    private val myMessageBus = myProject.messageBus

    private val myGlobalMetaModel = CachedValuesManager.getManager(myProject).createCachedValue(
        {
            val localMetaModels = CngMetaModelCollector.getInstance(myProject).collectDependencies()
                .filter { obj: PsiFile? -> Objects.nonNull(obj) }
                .map { psiFile: PsiFile -> retrieveSingleMetaModelPerFile(psiFile) }
                .map { obj: CachedValue<CngMetaModel> -> obj.value }

            val dependencies = localMetaModels
                .map { it.psiFile }
                .toTypedArray()
            val globalMetaModel = CngMetaModelMerger.getInstance(myProject).merge(localMetaModels)

            CachedValueProvider.Result.create(globalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    override fun getMetaModel(): CngGlobalMetaModel {
        if (myGlobalMetaModel.hasUpToDateValue() || lock.isWriteLocked || writeLock.isHeldByCurrentThread) {
            return readMetaModelWithLock()
        }
        return writeMetaModelWithLock()
    }

    // parameter for Meta Model cached value is not required, we have to pass new cache holder only during write process
    private fun readMetaModelWithLock(): CngGlobalMetaModel {
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

    private fun writeMetaModelWithLock(): CngGlobalMetaModel {
        try {
            writeLock.lock()
            val globalMetaModel = myGlobalMetaModel.value
            myMessageBus.syncPublisher(topic).cngSystemChanged(globalMetaModel)

            return globalMetaModel
        } finally {
            writeLock.unlock()
        }
    }

    private fun retrieveSingleMetaModelPerFile(psiFile: PsiFile): CachedValue<CngMetaModel> {
        return Optional.ofNullable(psiFile.getUserData(SINGLE_MODEL_CACHE_KEY))
            .orElseGet {
                val cachedValue = createSingleMetaModelCachedValue(myProject, psiFile)
                psiFile.putUserData(SINGLE_MODEL_CACHE_KEY, cachedValue)
                cachedValue
            }
    }

    private fun createSingleMetaModelCachedValue(project: Project, psiFile: PsiFile): CachedValue<CngMetaModel> {
        return CachedValuesManager.getManager(project).createCachedValue(
            {
                ApplicationManager.getApplication().runReadAction(
                    Computable {
                        CachedValueProvider.Result.create(CngMetaModelProcessor.getInstance(myProject).process(psiFile), psiFile)
                    } as Computable<CachedValueProvider.Result<CngMetaModel>>)
            }, false
        )
    }

    companion object {
        val topic = Topic("HYBRIS_COCKPITNG_SYSTEM_LISTENER", SystemChangeListener::class.java)
        private val SINGLE_MODEL_CACHE_KEY = Key.create<CachedValue<CngMetaModel>>("SINGLE_CNG_MODEL_CACHE")
        private val lock = ReentrantReadWriteLock()
        private val readLock = lock.readLock()
        private val writeLock = lock.writeLock()
    }
}