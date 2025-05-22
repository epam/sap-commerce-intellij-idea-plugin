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
import com.intellij.util.messages.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CachedState<T>(
    val value: T?,
    val computed: Boolean,
    val computing: Boolean
)

abstract class AbstractMetaModelStateService<M, G>(protected val project: Project) : Disposable {

    companion object {
        val TOPIC = Topic("HYBRIS_META_SYSTEM_LISTENER", MetaModelChangeListener::class.java)
    }

    protected val _metaModelsState = MutableStateFlow<Map<String, M>>(emptyMap())
    protected val _metaModelState = MutableStateFlow(CachedState<G>(null, computed = false, computing = false))
    protected val _recomputeMetas = MutableStateFlow<Collection<String>?>(null)
    protected val recomputeMetas = _recomputeMetas.asStateFlow()
    protected val metaModelsState = _metaModelsState.asStateFlow()
    protected val metaModelState = _metaModelState.asStateFlow()

    protected abstract fun processState(metaModels: Collection<String> = emptyList())

    fun init() {
        processState()
    }

    fun initialized() = metaModelState.value.computed

    fun get(): G {
        val modifiedMetas = recomputeMetas.value

        if (modifiedMetas == null) {
            return getCurrentState()
        }

        processState(modifiedMetas)
        throw ProcessCanceledException()
    }

    fun update(metaModels: Collection<String>) {
        val metas = _recomputeMetas.value
        if (metas == null) {
            _recomputeMetas.value = metaModels.toSet()
        } else {
            _recomputeMetas.value = (metas + metaModels).toSet()
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

abstract class MetaModelStateService<M, G>(project: Project): AbstractMetaModelStateService<M, G>(project) {
}

