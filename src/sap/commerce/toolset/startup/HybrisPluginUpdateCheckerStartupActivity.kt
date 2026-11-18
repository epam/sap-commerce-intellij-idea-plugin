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
package sap.commerce.toolset.startup

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HybrisPluginUpdateCheckerStartupActivity : ProjectActivity {

    // cancellable, unlike `runInEdt`, so it does not run while the plugin is being unloaded
    override suspend fun execute(project: Project) = withContext(Dispatchers.EDT) {
        if (project.isDisposed) return@withContext

        UpdateSettings.getInstance().state.isCheckNeeded = true
        HybrisStandalonePluginUpdateChecker.getInstance(project).pluginUsed()
    }
}