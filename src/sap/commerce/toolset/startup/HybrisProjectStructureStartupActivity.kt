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

import com.google.gson.Gson
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.text.VersionComparatorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.ProjectState
import sap.commerce.toolset.project.configurator.ProjectStartupConfigurator
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.ui.ProjectAskForRefreshDialog
import sap.commerce.toolset.project.ui.ProjectAskForReimportDialog
import sap.commerce.toolset.settings.WorkspaceSettings

class HybrisProjectStructureStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isDisposed) return
        val isHybrisProject = project.isHybrisProject
        if (!isHybrisProject) return
        val importedByVersion = WorkspaceSettings.getInstance(project).importedByVersion
        logVersion(project, importedByVersion)

        val continueOpening = when (val projectState = getProjectState(importedByVersion)) {
            is ProjectState.Normal -> true

            is ProjectState.Refresh -> withContext(Dispatchers.EDT) {
                !ProjectAskForRefreshDialog(project, projectState).showAndGet()
            }

            is ProjectState.Reimport -> withContext(Dispatchers.EDT) {
                !ProjectAskForReimportDialog(project, projectState).showAndGet()
            }
        }

        if (continueOpening) {
            continueOpening(project)
        }

//        if (isOutdatedHybrisProject(importedByVersion)) {
//            Notifications.create(
//                NotificationType.INFORMATION,
//                i18n("hybris.notification.project.open.outdated.title"),
//                i18n(
//                    "hybris.notification.project.open.outdated.text",
//                    importedByVersion ?: "old"
//                )
//            )
//                .important(true)
//                .addAction(i18n("hybris.notification.project.open.outdated.action")) { _, _ -> project.triggerAction("sap.commerce.toolset.yRefresh") }
//                .notify(project)
//        }
//
//        logVersion(project, importedByVersion)
//        continueOpening(project)
    }

    private fun getProjectState(importedByVersion: String?): ProjectState {
        val lastImportVersion = importedByVersion ?: return ProjectState.Reimport()
        val currentVersion = Plugin.HYBRIS.pluginDescriptor
            ?.version
            ?: return ProjectState.Reimport()

        val prs = this.javaClass.getResourceAsStream("/prs.json").use { stream ->
            Gson().fromJson(stream?.bufferedReader(), ProjectState.PRData::class.java)
        }
            .pullRequests
            .filter { VersionComparatorUtil.compare(it.milestone, lastImportVersion) >= 0 && VersionComparatorUtil.compare(it.milestone, currentVersion) <= 0 }
            .flatMap { pr -> pr.labels.map { label -> label to pr } }
            .groupBy({ it.first }, { it.second })

        return prs[ProjectImportConstants.PR_LABEL_PROJECT_REIMPORT]?.let { ProjectState.Reimport(it) }
            ?: prs[ProjectImportConstants.PR_LABEL_PROJECT_REFRESH]?.let { ProjectState.Refresh(it) }
            ?: ProjectState.Normal
    }

    private fun logVersion(project: Project, importedByVersion: String?) {
        val settings = ProjectSettings.getInstance(project)
        val hybrisVersion = settings.hybrisVersion
        val plugin = Plugin.HYBRIS.pluginDescriptor ?: return
        val pluginVersion = plugin.version
        thisLogger().info("Opening hybris version $hybrisVersion which was imported by $importedByVersion. Current plugin is $pluginVersion")
    }

    private suspend fun continueOpening(project: Project) {
        if (project.isDisposed) return

        ProjectStartupConfigurator.EP.extensionList.forEach { it.configure(project) }
    }
}
