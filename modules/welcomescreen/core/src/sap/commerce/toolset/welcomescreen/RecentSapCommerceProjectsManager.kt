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

package sap.commerce.toolset.welcomescreen

import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.RecentProjectsManager.RecentProjectsChange
import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.util.fileExists
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProjectSettings
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProjectVcsDetails
import sap.commerce.toolset.welcomescreen.reader.SapCommerceProjectSettingsReader
import sap.commerce.toolset.welcomescreen.reader.SapCommerceProjectVcsDetailsReader
import java.nio.file.Path

@Service
class RecentSapCommerceProjectsManager(private val coroutineScope: CoroutineScope) {

    private val stateLock = Any()
    private var lazyEvaluationJob: Job? = null

    init {
        application.messageBus.connect(coroutineScope).subscribe(
            topic = RecentProjectsManager.RECENT_PROJECTS_CHANGE_TOPIC,
            handler = object : RecentProjectsChange {
                override fun change() = loadRecentProjects()
            })
    }

    fun loadRecentProjects() {
        invokeLater {
            application.messageBus.syncPublisher(TOPIC).loading()
        }

        synchronized(stateLock) {
            val recentProjects = RecentProjectsManager.getInstance()
                .asSafely<RecentProjectsManagerBase>()
                ?.getRecentPaths()
                ?.asSequence()
                ?.filter { isSapCommerceProject(it) }
                ?.map { location -> RecentSapCommerceProject.of(location) }
                ?.toList()
                ?: emptyList()

            invokeLater { application.messageBus.syncPublisher(TOPIC).loaded(recentProjects) }

            lazyEvaluationJob?.cancel()
            lazyEvaluationJob = coroutineScope.launch {
                for (recentProject in recentProjects) {
                    supervisorScope {
                        launch { loadSettings(recentProject) }
                        launch { loadVcsDetails(recentProject) }
                    }
                }
            }
        }
    }

    private suspend fun loadSettings(recentProject: RecentSapCommerceProject) = lazyLoad(
        recentProject = recentProject,
        onError = {
            thisLogger().debug("Failed to read hybris settings for ${recentProject.location}", it)
            recentProject.settingsProperty.set(RecentSapCommerceProjectSettings.NotLoaded)
        }) {
        checkCanceled()

        val settings = SapCommerceProjectSettingsReader.getInstance().read(recentProject)
        recentProject.settingsProperty.set(settings)
    }

    private suspend fun loadVcsDetails(recentProject: RecentSapCommerceProject) = lazyLoad(
        recentProject = recentProject,
        onError = {
            thisLogger().debug("Failed to read git HEAD for ${recentProject.location}", it)
            recentProject.vcsDetailsProperty.set(RecentSapCommerceProjectVcsDetails.NotAGitRepo)
        }) {
        checkCanceled()

        val vcsDetails = SapCommerceProjectVcsDetailsReader.getInstance().read(recentProject)
        recentProject.vcsDetailsProperty.set(vcsDetails)
    }

    private suspend fun lazyLoad(
        recentProject: RecentSapCommerceProject,
        onError: (Throwable) -> Unit,
        loadLazily: suspend () -> Unit
    ) = try {
        loadLazily()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        onError(e)
    } finally {
        invokeLater { application.messageBus.syncPublisher(TOPIC).changed(recentProject) }
    }

    private fun isSapCommerceProject(location: String): Boolean = runCatching {
        Path.of(location)
            .resolve(Project.DIRECTORY_STORE_FOLDER)
            .resolve(HybrisConstants.STORAGE_HYBRIS_PROJECT_SETTINGS)
            .fileExists
    }.getOrElse { false }

    interface RecentSapCommerceProjectsListener {
        @RequiresEdt
        fun loading() = Unit

        @RequiresEdt
        fun loaded(recentProjects: List<RecentSapCommerceProject>) = Unit

        @RequiresEdt
        fun changed(recentProject: RecentSapCommerceProject) = Unit
    }

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(
            RecentSapCommerceProjectsListener::class.java,
            Topic.BroadcastDirection.NONE
        )

        @JvmStatic
        fun getInstance(): RecentSapCommerceProjectsManager = service<RecentSapCommerceProjectsManager>()
    }
}