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
import kotlinx.coroutines.flow.StateFlow
import sap.commerce.toolset.project.welcomescreen.presentation.RecentSapCommerceProjectSettings
import sap.commerce.toolset.project.welcomescreen.reader.HybrisProjectSettingsReader

/**
 * Application-level cache for parsed `.idea/hybrisProjectSettings.xml`.
 *
 * State is exposed as a [StateFlow] of `location -> Settings` so consumers can
 * react to changes via coroutines instead of registering callback listeners.
 *
 * - [settings] emits a new map snapshot whenever any entry is added or invalidated.
 * - [warmUp] schedules background parsing for a path; concurrent calls for the
 *   same path are deduplicated.
 * - [invalidate] removes a cached entry (force re-read on next [warmUp]).
 *
 * Read API: [get] / [isLoaded] return synchronously from the latest snapshot —
 * UI renderers (which don't suspend) call these directly.
 */
@Service(Service.Level.APP)
class HybrisProjectSettingsCache(scope: CoroutineScope) : StateFlowCache<RecentSapCommerceProjectSettings>(scope) {

    override suspend fun load(key: String) = HybrisProjectSettingsReader.read(key)

    companion object {
        @JvmStatic
        fun getInstance(): HybrisProjectSettingsCache = ApplicationManager.getApplication().service()
    }
}