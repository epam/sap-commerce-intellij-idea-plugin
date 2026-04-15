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

package sap.commerce.toolset.project.welcomescreen.cache

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import sap.commerce.toolset.project.welcomescreen.reader.GitHeadReader
import java.util.concurrent.ConcurrentHashMap

/**
 * Application-level cache for git branch names read from `.git/HEAD`.
 *
 * Mirrors [HybrisProjectSettingsCache]: synchronous [get] for cached values,
 * asynchronous [warmUp] for background loading, [Listener] for completion
 * notifications. Stores `Branch.NotAGitRepo` for projects without a `.git`
 * directory so we don't keep retrying them on every reload.
 */
@Service(Service.Level.APP)
class GitHeadCache(private val scope: CoroutineScope) {

    sealed interface Branch {
        data class Named(val name: String) : Branch
        data object NotAGitRepo : Branch
    }

    fun interface Listener {
        fun onBranchLoaded(projectLocation: String, branch: Branch)
    }

    private val cache = ConcurrentHashMap<String, Branch>()
    private val inFlight = ConcurrentHashMap<String, Deferred<Branch>>()
    private val listeners = mutableListOf<Listener>()

    fun get(projectLocation: String): Branch? = cache[projectLocation]

    fun isLoaded(projectLocation: String): Boolean = cache.containsKey(projectLocation)

    fun warmUp(projectLocation: String) {
        if (cache.containsKey(projectLocation)) return

        inFlight.computeIfAbsent(projectLocation) { location ->
            scope.async(Dispatchers.Default) {
                val branch = GitHeadReader.read(location)
                    ?.let { Branch.Named(it) }
                    ?: Branch.NotAGitRepo
                cache[location] = branch
                inFlight.remove(location)
                fireLoaded(location, branch)
                branch
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

    private fun fireLoaded(location: String, branch: Branch) {
        val snapshot = synchronized(listeners) { listeners.toList() }
        for (l in snapshot) {
            runCatching { l.onBranchLoaded(location, branch) }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): GitHeadCache =
            ApplicationManager.getApplication().service()
    }
}