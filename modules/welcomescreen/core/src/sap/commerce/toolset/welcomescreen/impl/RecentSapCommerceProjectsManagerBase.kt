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

package sap.commerce.toolset.welcomescreen.impl

import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.asSafely
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.util.fileExists
import sap.commerce.toolset.welcomescreen.RecentSapCommerceProjectsManager
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProjectGitBranch
import sap.commerce.toolset.welcomescreen.reader.GitHeadReader
import sap.commerce.toolset.welcomescreen.reader.HybrisProjectSettingsReader
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Application-level service that materializes [RecentSapCommerceProject] rows for the welcome tab.
 *
 * ## Per-row concurrency model
 *
 * Each call to [recentSapCommerceProjects] produces a fresh batch of rows. For every row we launch
 * a coroutine on the caller-supplied [CoroutineScope] that, in turn, runs two independent I/O tasks
 * (hybris settings + git HEAD) inside a [supervisorScope]. The structure is:
 *
 * ```
 * callerScope
 *   └── projectJob (per row)                      <- child of callerScope
 *        └── supervisorScope
 *             ├── launch settings reader           <- isolated from git reader
 *             └── launch git reader                <- isolated from settings reader
 * ```
 *
 * Guarantees:
 *  - **Parent cancellation propagates.** Cancelling `callerScope` cancels every `projectJob`, which
 *    cancels both I/O children.
 *  - **Sibling isolation.** A failure in one I/O task does not cancel the other (`supervisorScope`),
 *    and a failure in one row's `projectJob` does not cancel other rows' `projectJob`s (each is
 *    an independent child of `callerScope`).
 *  - **Row survives task failure.** The [RecentSapCommerceProject] instance's lifetime is governed
 *    by its own [Disposable], not by coroutine outcome. A failed reader just leaves the
 *    corresponding property at `null` (or flips `settingsLoadedProperty` to `true` so the UI can
 *    stop spinning).
 *  - **Row disposal cancels only that row's work.** When a row is disposed (batch replaced or user
 *    removes the project), its `projectJob` is cancelled; siblings are unaffected.
 *
 * ## Concurrent calls
 *
 * The welcome tab can trigger a reload from several sources (construction, tab-became-visible,
 * `RECENT_PROJECTS_CHANGE_TOPIC` on an arbitrary publisher thread). Each trigger spawns an off-EDT
 * coroutine on the tab's scope which then calls [recentSapCommerceProjects]. Two such coroutines
 * can overlap.
 *
 * To keep the shared `currentBatchDisposable` correct under concurrent callers, the entire method
 * runs under [batchLock]:
 *  1. Dispose the previous batch's disposable (tears down its rows, cancelling their jobs via
 *     the row-level dispose hook).
 *  2. Install a new batch disposable as `current`.
 *  3. Enumerate recent project paths, filter to SAP Commerce projects, and build the row objects
 *     registered against the new batch disposable. Row construction also launches the per-row
 *     coroutines (via [launchProjectLoaders]) which register their own cancel-on-dispose hook.
 *  4. Return the rows to the caller.
 *
 * If a concurrent caller arrives while we hold the lock, they wait. When they acquire the lock
 * next, they dispose *our* batch immediately — which cancels our rows' coroutines and orphans our
 * rows. This is correct: the welcome tab only keeps the result of the last completed call (see
 * `listModel.replaceAll(projects)`), so the losing batch was going to be discarded anyway.
 *
 * Enumeration holds the lock but is intentionally cheap — only path-existence checks. The expensive
 * per-row I/O happens asynchronously on `Dispatchers.IO` outside the lock.
 */
@Service(Service.Level.APP)
class RecentSapCommerceProjectsManagerBase : RecentSapCommerceProjectsManager {

    /**
     * Serializes the read-dispose-write sequence on [currentBatchDisposable]. Callers can arrive
     * from any thread (see class-level docs), so single-threaded assumptions do not hold.
     */
    private val batchLock = ReentrantLock()

    /**
     * Disposable anchoring the most recently produced batch of rows. Disposing it disposes every
     * row in that batch, which in turn cancels each row's coroutine job via the dispose hook
     * installed in [launchProjectLoaders]. Read and written only under [batchLock].
     */
    private var currentBatchDisposable: Disposable? = null

    fun recentSapCommerceProjects(
        scope: CoroutineScope,
        parentDisposable: Disposable,
        onPropertyChange: () -> Unit,
    ): List<RecentSapCommerceProject> = batchLock.withLock {
        // Dispose the previous batch up-front. Because every row registers a cancel-on-dispose
        // hook for its coroutine job, this also cancels any in-flight I/O from the old batch.
        currentBatchDisposable?.let { Disposer.dispose(it) }

        val localDisposable = Disposer.newDisposable(parentDisposable)
        currentBatchDisposable = localDisposable

        runCatching {
            RecentProjectsManager.getInstance()
                .asSafely<RecentProjectsManagerBase>()
                ?.getRecentPaths()
                ?.asSequence()
                ?.filter { isSapCommerceProject(it) }
                ?.map { location ->
                    RecentSapCommerceProject.of(location, localDisposable, onPropertyChange) { project ->
                        launchProjectLoaders(scope, project)
                    }
                }
                ?.toList()
                ?: emptyList()
        }.getOrElse {
            thisLogger().warn("Failed to enumerate recent SAP Commerce projects", it)
            emptyList()
        }
    }

    /**
     * Launches the two per-row I/O readers as children of [scope] and wires the resulting [Job] to
     * the row's [Disposable] so row disposal cancels only this row's work.
     *
     * - `scope.launch(Dispatchers.IO)` makes `projectJob` a structural child of `scope`: cancelling
     *   `scope` cancels every row's job automatically. The IO dispatcher is chosen here (rather
     *   than on each inner `launch`) so the coordinating coroutine itself doesn't occupy a
     *   Default-pool thread while waiting.
     * - `supervisorScope` ensures the settings reader and the git reader are isolated from each
     *   other: a throw in one does not cancel the other.
     * - The per-reader try/catch blocks explicitly rethrow [CancellationException] so cancellation
     *   propagates normally, while all other errors are logged and the UI is left in a sensible
     *   state (e.g., `settingsLoadedProperty = true` so the spinner clears).
     *
     * Ordering edge cases:
     * - If `scope` is already cancelled by the time we reach `scope.launch`, the returned `Job`
     *   is immediately cancelled — `projectJob.cancel()` below is then a harmless no-op.
     * - If the project's disposable has already been torn down (possible if its parent was
     *   disposed between registration and this callback), `Disposer.register` invokes our action
     *   synchronously, cancelling the job right away.
     */
    private fun launchProjectLoaders(scope: CoroutineScope, project: RecentSapCommerceProject) {
        val projectJob: Job = scope.launch(Dispatchers.IO) {
            supervisorScope {
                launch { loadSettings(project) }
                launch { loadGitBranch(project) }
            }
        }

        Disposer.register(project) { projectJob.cancel() }
    }

    private suspend fun loadSettings(project: RecentSapCommerceProject) {
        try {
            val settings = HybrisProjectSettingsReader.read(project.location)
            currentCoroutineContext().ensureActive()

            project.hybrisVersionProperty.asAtomic()?.set(settings.hybrisVersion)
            project.hostingEnvironmentProperty.asAtomic()?.set(settings.hostingEnvironment)
            project.settingsLoadedProperty.asAtomic()?.set(true)
        } catch (e: CancellationException) {
            // Structural cancellation — must rethrow, never swallow.
            throw e
        } catch (e: Throwable) {
            thisLogger().debug("Failed to read hybris settings for ${project.location}", e)
            // Flip the flag even on failure so the UI stops showing a spinner. Absent values stay
            // null, which the renderer handles as "n/a".
            project.settingsLoadedProperty.asAtomic()?.set(true)
        }
    }

    private suspend fun loadGitBranch(project: RecentSapCommerceProject) {
        try {
            val branch = GitHeadReader.read(project.location)
                ?.let { RecentSapCommerceProjectGitBranch.Named(it) }
                ?: RecentSapCommerceProjectGitBranch.NotAGitRepo
            currentCoroutineContext().ensureActive()

            project.gitBranchProperty.asAtomic()?.set(branch)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            thisLogger().debug("Failed to read git HEAD for ${project.location}", e)
        }
    }

    private fun isSapCommerceProject(location: String): Boolean = runCatching {
        Path.of(location)
            .resolve(Project.DIRECTORY_STORE_FOLDER)
            .resolve(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS)
            .fileExists
    }.getOrElse { false }

    /**
     * Safe downcast from the exposed [ObservableProperty] to the underlying [AtomicProperty].
     *
     * The data class exposes these as `ObservableProperty<X>` but constructs them as
     * `AtomicProperty<X>`, so the cast always succeeds in practice. Returning `null` on cast
     * failure is belt-and-suspenders defense should that construction contract ever change.
     */
    private fun <T> ObservableProperty<T>.asAtomic(): AtomicProperty<T>? = this.asSafely<AtomicProperty<T>>()

    companion object {
        @JvmStatic
        fun getInstance(): RecentSapCommerceProjectsManagerBase = service<RecentSapCommerceProjectsManagerBase>()
    }
}