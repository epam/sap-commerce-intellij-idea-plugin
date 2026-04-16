/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.welcomescreen.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

abstract class StateFlowCache<V : Any>(protected val scope: CoroutineScope) {
    private val _data = MutableStateFlow<Map<String, V>>(emptyMap())
    val data: StateFlow<Map<String, V>> = _data.asStateFlow()
    private val loadJobs = ConcurrentHashMap<String, Job>()

    fun get(key: String): V? = _data.value[key]
    fun isLoaded(key: String): Boolean = _data.value.containsKey(key)

    fun warmUp(key: String) {
        if (isLoaded(key)) return
        loadJobs.computeIfAbsent(key) { k ->
            scope.launch {
                try {
                    val value = load(k)
                    _data.update { it + (k to value) }
                } finally {
                    loadJobs.remove(k)
                }
            }
        }
    }

    fun invalidate(key: String) {
        loadJobs.remove(key)?.cancel()
        _data.update { it - key }
    }

    protected abstract suspend fun load(key: String): V
}