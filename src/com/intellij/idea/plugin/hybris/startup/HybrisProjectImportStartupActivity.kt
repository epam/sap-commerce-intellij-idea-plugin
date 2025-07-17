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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.ide.util.RunOnceUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.project.configurators.PostImportConfigurator
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.idea.plugin.hybris.util.isNotHybrisProject
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.removeUserData

class HybrisProjectImportStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isNotHybrisProject) return

        RunOnceUtil.runOnceForProject(project, "afterHybrisProjectImport") {
            project.getUserData(HybrisConstants.KEY_FINALIZE_PROJECT_IMPORT)
                ?.let {
                    project.removeUserData(HybrisConstants.KEY_FINALIZE_PROJECT_IMPORT)

                    PostImportConfigurator.getInstance(project).configure(
                        it.first,
                        it.second,
                        it.third
                    )
                }

            project.service<HybrisConsoleService>().activateToolWindow()
        }

        CommonIdeaService.getInstance().fixRemoteConnectionSettings(project)
    }

}