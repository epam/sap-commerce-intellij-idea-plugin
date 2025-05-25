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

package com.intellij.idea.plugin.hybris.system.meta

import com.intellij.openapi.Disposable
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import com.intellij.util.messages.Topic
import com.intellij.util.xml.DomElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CachedState<T>(
    val value: T?,
    val computed: Boolean,
    val computing: Boolean
)

abstract class MetaModelStateService<G, M, D : DomElement>(
    protected val project: Project,
    private val coroutineScope: CoroutineScope,
    private val systemName: String,
    private val metaCollector: MetaCollector<D>,
    private val metaModelProcessor: MetaModelProcessor<D, M>
) : Disposable {

    companion object {
        val TOPIC = Topic("HYBRIS_META_SYSTEM_LISTENER", MetaModelChangeListener::class.java)
    }

    protected val _metaModelsState = MutableStateFlow<Map<String, M>>(emptyMap())
    protected val _metaModelState = MutableStateFlow(CachedState<G>(null, computed = false, computing = false))
    protected val _recomputeMetasState = MutableStateFlow<Collection<String>?>(null)
    protected val recomputeMetasState = _recomputeMetasState.asStateFlow()
    protected val metaModelsState = _metaModelsState.asStateFlow()
    protected val metaModelState = _metaModelState.asStateFlow()

    protected abstract suspend fun create(metaModelsToMerge: Collection<M>): G
    protected abstract fun onCompletion(newState: G)

    fun init() {
        processState()
    }

    fun initialized() = metaModelState.value.computed

    fun get(): G {
        val modifiedMetas = recomputeMetasState.value

        if (modifiedMetas == null) {
            return getCurrentState()
        }

        processState(modifiedMetas)
        throw ProcessCanceledException()
    }

    private fun processState(metaModels: Collection<String> = emptyList()) {
        if (metaModelState.value.computing) return

        _metaModelState.value = CachedState(null, computed = false, computing = true)

        DumbService.Companion.getInstance(project).runWhenSmart {
            coroutineScope.launch {
                val newState = withBackgroundProgress(project, "Re-building $systemName System...", true) {
                    val collectedDependencies = metaCollector.collectDependencies()

                    val localMetaModels = reportProgress(collectedDependencies.size) { progressReporter ->
                        collectedDependencies
                            .map {
                                progressReporter.sizedStep(1, "Processing: ${it.name}...") {
                                    async {
                                        val cachedMetaModel = metaModelsState.value[it.name]
                                        if (cachedMetaModel == null || metaModels.contains(it.name)) {
                                            it.name to metaModelProcessor.process(it)
                                        } else {
                                            it.name to cachedMetaModel
                                        }
                                    }
                                }
                            }
                            .awaitAll()
                            .filter { (_, model) -> model != null }
                            .distinctBy { it.first }
                            .associate { it.first to it.second!! }
                    }

                    _metaModelsState.value = localMetaModels

                    create(metaModelsState.value.values)
                }

                _metaModelState.value = CachedState(newState, computed = true, computing = false)
                _recomputeMetasState.value = null

                onCompletion(newState)
            }
        }
    }

    fun update(metaModels: Collection<String>) {
        val metas = _recomputeMetasState.value
        if (metas == null) {
            _recomputeMetasState.value = metaModels.toSet()
        } else {
            _recomputeMetasState.value = (metas + metaModels).toSet()
        }
    }

    protected fun getCurrentState(): G {
        val state = metaModelState.value

        if (!state.computed || state.value == null || DumbService.isDumb(project)) {
            throw ProcessCanceledException()
        }
        return state.value
    }

    override fun dispose() {
    }
}
