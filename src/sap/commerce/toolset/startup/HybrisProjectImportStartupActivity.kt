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
package sap.commerce.toolset.startup

import com.intellij.ide.util.RunOnceUtil
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.removeUserData
import com.intellij.util.application
import com.intellij.util.asSafely
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.console.HybrisConsoleService
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.configurator.PostImportBulkConfigurator
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.settings.WorkspaceSettings

class HybrisProjectImportStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isNotHybrisProject) {
            if (ProjectSettings.getInstance(project).importedByVersion == null) return
            else {
                WorkspaceSettings.getInstance(project).hybrisProject = true
                invokeLater {
                    Notifications
                        .error(
                            "Incompatible Plugin API changes",
                            """
                                It's highly recommended to re-import and re-configure the Project due drastic changes in the Plugin API.<br>
                                Unfortunately, some configurations and settings may be lost.
                            """.trimIndent()
                        )
                        .important(true)
                        .system(true)
                        .addAction("Restart") { _, _ ->
                            application.asSafely<ApplicationEx>()
                                ?.restart(true)
                        }
                        .notify(project)
                }
            }
        }

        RunOnceUtil.runOnceForProject(project, "afterHybrisProjectImport") {
            project.getUserData(ProjectConstants.KEY_FINALIZE_PROJECT_IMPORT)
                ?.let {
                    project.removeUserData(ProjectConstants.KEY_FINALIZE_PROJECT_IMPORT)

                    PostImportBulkConfigurator.getInstance(project).configure(it)
                }

            HybrisConsoleService.getInstance(project).activateToolWindow()
        }
    }

}