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

package sap.commerce.toolset.project.welcomescreen

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.concurrent.ConcurrentHashMap

/**
 * Application-level cache for parsed `.idea/hybrisProjectSettings.xml`.
 *
 * - [get] returns cached settings synchronously or `null` if not yet loaded.
 * - [warmUp] schedules background parsing for a path; safe to call repeatedly —
 *   concurrent calls for the same path are deduplicated.
 * - [invalidate] removes a cached entry (e.g., to force a re-read).
 *
 * Backed by a coroutine scope owned by the service, which is cancelled on
 * application shutdown. Notify listeners via [Listener] when an entry becomes
 * available so UI can refresh that row.
 */
@Service(Service.Level.APP)
class HybrisProjectSettingsCache(private val scope: CoroutineScope) {

    fun interface Listener {
        fun onSettingsLoaded(projectLocation: String, settings: HybrisProjectSettingsReader.Settings)
    }

    private val cache = ConcurrentHashMap<String, HybrisProjectSettingsReader.Settings>()
    private val inFlight = ConcurrentHashMap<String, Deferred<HybrisProjectSettingsReader.Settings>>()
    private val listeners = mutableListOf<Listener>()

    fun get(projectLocation: String): HybrisProjectSettingsReader.Settings? = cache[projectLocation]

    fun warmUp(projectLocation: String) {
        if (cache.containsKey(projectLocation)) return

        inFlight.computeIfAbsent(projectLocation) { location ->
            scope.async(Dispatchers.Default) {
                val settings = HybrisProjectSettingsReader.read(location)
                cache[location] = settings
                inFlight.remove(location)
                fireLoaded(location, settings)
                settings
            }
        }
    }

    fun invalidate(projectLocation: String) {
        cache.remove(projectLocation)
        inFlight.remove(projectLocation)?.cancel()
    }

    fun addListener(listener: Listener) {
        synchronized(listeners) { listeners.add(listener) }
    }

    fun removeListener(listener: Listener) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    private fun fireLoaded(location: String, settings: HybrisProjectSettingsReader.Settings) {
        val snapshot = synchronized(listeners) { listeners.toList() }
        for (l in snapshot) {
            runCatching { l.onSettingsLoaded(location, settings) }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): HybrisProjectSettingsCache =
            ApplicationManager.getApplication().service()
    }
}