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

package sap.commerce.toolset.welcomescreen.presentation

import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject.Companion.of
import sap.commerce.toolset.welcomescreen.reader.GitHeadReader
import sap.commerce.toolset.welcomescreen.reader.HybrisProjectSettingsReader
import java.nio.file.Path
import javax.swing.Icon

/**
 * Presentation model for a SAP Commerce project row on the welcome tab.
 *
 * Slow-to-read attributes (hybris version, hosting environment, git branch) are
 * exposed as [ObservableProperty] instances and populated asynchronously via a
 * coroutine kicked off by [of]. There is no separate cache: each project owns
 * its own properties and initializes them once on creation. The UI subscribes
 * via [ObservableProperty.afterChange] and receives updates as values arrive.
 *
 * While a property is still loading, its value is `null`. For the renderer's
 * convenience there is also a dedicated [settingsLoadedProperty] that flips to
 * `true` exactly when `.idea/hybrisProjectSettings.xml` finishes parsing — this
 * drives the "spinner vs. version badge" switch in the cell.
 */
data class RecentSapCommerceProject(
    val location: String,
    val displayName: String,
    val projectName: String,
    val projectIcon: Icon,
    val hybrisVersionProperty: ObservableProperty<String?>,
    val hostingEnvironmentProperty: ObservableProperty<HostingEnvironment?>,
    val gitBranchProperty: ObservableProperty<RecentSapCommerceProjectGitBranch?>,
    val settingsLoadedProperty: ObservableProperty<Boolean>,
) : Disposable {
    val path: Path get() = Path.of(location)

    val locationRelativeToUserHome: String
        get() = FileUtil.getLocationRelativeToUserHome(location)

    /** `true` once `.idea/hybrisProjectSettings.xml` has been read (regardless of contents). */
    val isSettingsLoaded: Boolean
        get() = settingsLoadedProperty.get()

    /** Parsed hybris version, or `null` while loading or when absent from settings. */
    val hybrisVersion: String?
        get() = hybrisVersionProperty.get()

    /** Hosting environment, or `null` while loading or when absent from settings. */
    val hostingEnvironment: HostingEnvironment?
        get() = hostingEnvironmentProperty.get()

    /** Current branch name if the project is a git repo, `null` otherwise (or while still loading). */
    val gitBranch: String?
        get() = gitBranchProperty.get().asSafely<RecentSapCommerceProjectGitBranch.Named>()?.name

    override fun dispose() {
    }

    companion object {
        /**
         * Builds a [RecentSapCommerceProject] and schedules asynchronous population of its
         * observable properties on [scope]. The coroutine's work is throwaway once the
         * instance is discarded, so callers don't need to cancel anything explicitly —
         * [scope]'s own lifecycle is sufficient.
         */
        fun of(location: String, scope: CoroutineScope, parentDisposable: Disposable, onPropertyChange: () -> Unit): RecentSapCommerceProject {
            val manager = RecentProjectsManagerBase.getInstanceEx()
            val projectName = manager.getProjectName(location)

            val hybrisVersionProperty = AtomicProperty<String?>(null)
            val hostingEnvironmentProperty = AtomicProperty<HostingEnvironment?>(null)
            val gitBranchProperty = AtomicProperty<RecentSapCommerceProjectGitBranch?>(null)
            val settingsLoadedProperty = AtomicProperty(false)

            val project = RecentSapCommerceProject(
                location = location,
                displayName = manager.getDisplayName(location) ?: projectName,
                projectName = projectName,
                projectIcon = manager.getProjectIcon(location, true),
                hybrisVersionProperty = hybrisVersionProperty,
                hostingEnvironmentProperty = hostingEnvironmentProperty,
                gitBranchProperty = gitBranchProperty,
                settingsLoadedProperty = settingsLoadedProperty,
            ).apply {
                Disposer.register(parentDisposable, this)
                hybrisVersionProperty.afterChange(this) { onPropertyChange() }
                hostingEnvironmentProperty.afterChange(this) { onPropertyChange() }
                gitBranchProperty.afterChange(this) { onPropertyChange() }
                settingsLoadedProperty.afterChange(this) { onPropertyChange() }
            }

            scope.launch {
                val settings = runCatching { HybrisProjectSettingsReader.read(location) }.getOrNull()
                hybrisVersionProperty.set(settings?.hybrisVersion)
                hostingEnvironmentProperty.set(settings?.hostingEnvironment)
                settingsLoadedProperty.set(true)
            }

            scope.launch {
                val branch = runCatching { GitHeadReader.read(location) }.getOrNull()
                gitBranchProperty.set(
                    branch
                        ?.let { RecentSapCommerceProjectGitBranch.Named(it) }
                        ?: RecentSapCommerceProjectGitBranch.NotAGitRepo
                )
            }

            return project
        }
    }
}