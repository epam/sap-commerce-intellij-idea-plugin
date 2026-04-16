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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProjectGitBranch
import sap.commerce.toolset.welcomescreen.reader.GitHeadReader

/**
 * Application-level cache for git branch names read from `.git/HEAD`.
 *
 * State is exposed as a [StateFlow] of `location -> Branch` so consumers can
 * react to changes via coroutines instead of registering callback listeners.
 *
 * - [data] emits a new map snapshot whenever any entry is added or invalidated.
 * - [warmUp] schedules background reading for a path; concurrent calls for the
 *   same path are deduplicated.
 * - [invalidate] removes a cached entry (force re-read on next [warmUp]).
 *
 * Stores [RecentSapCommerceProjectGitBranch.NotAGitRepo] for projects without a `.git` directory so we
 * don't keep retrying them on every reload.
 *
 * Read API: [get] / [isLoaded] return synchronously from the latest snapshot —
 * UI renderers (which don't suspend) call these directly.
 */

@Service(Service.Level.APP)
class GitHeadCache(scope: CoroutineScope) : StateFlowCache<RecentSapCommerceProjectGitBranch>(scope) {

    override suspend fun load(key: String) = GitHeadReader.read(key)
        ?.let { RecentSapCommerceProjectGitBranch.Named(it) }
        ?: RecentSapCommerceProjectGitBranch.NotAGitRepo

    companion object {
        @JvmStatic
        fun getInstance(): GitHeadCache = ApplicationManager.getApplication().service()
    }
}