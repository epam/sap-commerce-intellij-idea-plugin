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

import com.intellij.idea.plugin.hybris.system.cockpitng.meta.*
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.model.*
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.ActionDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.EditorDefinition
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.WidgetDefinition
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProcessCanceledException
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
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomFileElement
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
            val processor = CngMetaModelProcessor.getInstance(myProject)
            val (configs, configDependencies) = collectLocalMetaModels(SINGLE_CONFIG_MODEL_CACHE_KEY, Config::class.java,
                { file -> processor.processConfig(file) },
                { _ -> true }
            )
            val (actions, actionDependencies) = collectLocalMetaModels(SINGLE_ACTION_DEFINITION_MODEL_CACHE_KEY, ActionDefinition::class.java,
                { file -> processor.processActionDefinition(file) },
                { dom -> dom.rootElement.id.exists() }
            )
            val (widgets, widgetDependencies) = collectLocalMetaModels(SINGLE_WIDGET_DEFINITION_MODEL_CACHE_KEY, WidgetDefinition::class.java,
                { file -> processor.processWidgetDefinition(file) },
                { dom -> dom.rootElement.id.exists() }
            )
            val (editors, editorDependencies) = collectLocalMetaModels(SINGLE_EDITOR_DEFINITION_MODEL_CACHE_KEY, EditorDefinition::class.java,
                { file -> processor.processEditorDefinition(file) },
                { dom -> dom.rootElement.id.exists() }
            )
            val globalMetaModel = CngMetaModelMerger.getInstance(myProject).merge(
                configs, actions, widgets, editors
            )

            val dependencies = configDependencies + actionDependencies + widgetDependencies + editorDependencies

            CachedValueProvider.Result.create(globalMetaModel, dependencies.ifEmpty { ModificationTracker.EVER_CHANGED })
        }, false
    )

    private fun <D : DomElement, T : CngMetaModel<D>> collectLocalMetaModels(
        key: Key<CachedValue<T>>,
        clazz: Class<D>,
        resultProcessor: (input: PsiFile) -> T?,
        shouldCollect: (DomFileElement<D>) -> Boolean
    ): Pair<List<T>, Array<PsiFile>> {
        val localConfigMetaModels = CngMetaModelCollector.getInstance(myProject).collectDependencies(clazz, shouldCollect)
            .filter { obj: PsiFile? -> Objects.nonNull(obj) }
            .map { psiFile: PsiFile -> retrieveSingleMetaModelPerFile(psiFile, key, resultProcessor) }
            .map { obj: CachedValue<T> -> obj.value }

        val dependencies = localConfigMetaModels
            .map { it.psiFile }
            .toTypedArray()
        return Pair(localConfigMetaModels, dependencies)
    }

    override fun getMetaModel(): CngGlobalMetaModel {
        return DumbService.getInstance(myProject).tryRunReadActionInSmartMode(
            Computable {
                if (myGlobalMetaModel.hasUpToDateValue() || lock.isWriteLocked || writeLock.isHeldByCurrentThread) {
                    return@Computable readMetaModelWithLock()
                }
                return@Computable writeMetaModelWithLock()
            },
            "Computing Cockpitng System"
        ) ?: throw ProcessCanceledException()
//        if (DumbService.isDumb(myProject)) throw ProcessCanceledException()
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

    private fun <D : DomElement, T : CngMetaModel<D>> retrieveSingleMetaModelPerFile(
        psiFile: PsiFile,
        key: Key<CachedValue<T>>,
        resultProcessor: (input: PsiFile) -> T?
    ): CachedValue<T> {
        return Optional.ofNullable(psiFile.getUserData(key))
            .orElseGet {
                val cachedValue = createSingleMetaModelCachedValue(myProject, psiFile, resultProcessor)
                psiFile.putUserData(key, cachedValue)
                cachedValue
            }
    }

    private fun <D : DomElement, T : CngMetaModel<D>> createSingleMetaModelCachedValue(
        project: Project,
        psiFile: PsiFile,
        resultProcessor: (input: PsiFile) -> T?
    ): CachedValue<T> {
        return CachedValuesManager.getManager(project).createCachedValue(
            {
                ApplicationManager.getApplication().runReadAction(
                    Computable {
                        CachedValueProvider.Result.create(resultProcessor.invoke(psiFile), psiFile)
                    } as Computable<CachedValueProvider.Result<T>>)
            }, false
        )
    }

    companion object {
        val topic = Topic("HYBRIS_COCKPITNG_SYSTEM_LISTENER", CngChangeListener::class.java)
        private val SINGLE_CONFIG_MODEL_CACHE_KEY = Key.create<CachedValue<CngConfigMetaModel>>("SINGLE_CNG_CONFIG_MODEL_CACHE")
        private val SINGLE_ACTION_DEFINITION_MODEL_CACHE_KEY =
            Key.create<CachedValue<CngActionDefinitionMetaModel>>("SINGLE_ACTION_DEFINITION_MODEL_CACHE")
        private val SINGLE_WIDGET_DEFINITION_MODEL_CACHE_KEY =
            Key.create<CachedValue<CngWidgetDefinitionMetaModel>>("SINGLE_WIDGET_DEFINITION_MODEL_CACHE")
        private val SINGLE_EDITOR_DEFINITION_MODEL_CACHE_KEY =
            Key.create<CachedValue<CngEditorDefinitionMetaModel>>("SINGLE_EDITOR_DEFINITION_MODEL_CACHE")
        private val lock = ReentrantReadWriteLock()
        private val readLock = lock.readLock()
        private val writeLock = lock.writeLock()
    }
}