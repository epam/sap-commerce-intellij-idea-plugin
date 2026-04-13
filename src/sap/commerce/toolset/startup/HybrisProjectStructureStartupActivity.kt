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
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.text.VersionComparatorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.path
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.ProjectState
import sap.commerce.toolset.project.configurator.ProjectStartupConfigurator
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.ui.ProjectAskForForcedReimportDialog
import sap.commerce.toolset.project.ui.ProjectAskForRefreshDialog
import sap.commerce.toolset.project.ui.ProjectAskForReimportDialog
import sap.commerce.toolset.settings.WorkspaceSettings

class HybrisProjectStructureStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (project.isDisposed) return
        val isHybrisProject = project.isHybrisProject
        if (!isHybrisProject) return
        val currentVersion = Plugin.HYBRIS.pluginDescriptor?.version ?: return
        val workspaceSettings = WorkspaceSettings.getInstance(project)
        val importedByVersion = workspaceSettings.importedByVersion
        logVersion(project, importedByVersion)

        if (workspaceSettings.doNotAskForProjectImport[currentVersion] ?: false) {
            continueOpening(project)
            return
        }

        val continueOpening = when (val projectState = getProjectState(project, importedByVersion, currentVersion)) {
            is ProjectState.Normal -> true

            is ProjectState.Refresh -> withContext(Dispatchers.EDT) {
                !ProjectAskForRefreshDialog(project, projectState).showAndGet()
            }

            is ProjectState.Reimport -> withContext(Dispatchers.EDT) {
                !ProjectAskForReimportDialog(project, projectState).showAndGet()
            }

            is ProjectState.ForceReimport -> withContext(Dispatchers.EDT) {
                ProjectAskForForcedReimportDialog(project, projectState).showAndGet()
                false
            }

            else -> {
                thisLogger().warn("Project Import State cannot be identified due missing 'resource/prs.json', which has to be generated via Gradle 'fetchPRs' task.")
                true
            }
        }

        if (continueOpening) {
            continueOpening(project)
        }
    }

    private fun getProjectState(project: Project, importedByVersion: String?, currentVersion: String): ProjectState? {
        val projectDirectory = project.path
            ?.let { path -> VfsUtil.findFile(path, true) }
            ?: return null
        val lastImportVersion = importedByVersion
            ?: return ProjectState.ForceReimport(projectDirectory)
        val resourceAsStream = this.javaClass.getResourceAsStream("/prs.json")
            ?: return null
        val prs = resourceAsStream.use { stream ->
            Gson().fromJson(stream.bufferedReader(), ProjectState.PRData::class.java)
        }.pullRequests

        val filteredPRs = prs.filter { VersionComparatorUtil.compare(it.milestone, lastImportVersion) >= 0 && VersionComparatorUtil.compare(it.milestone, currentVersion) < 0 }

        val groupedPRs = filteredPRs
            .flatMap { pr -> pr.labels.map { label -> label to pr } }
            .groupBy({ it.first }, { it.second })

        return groupedPRs[ProjectImportConstants.PR_LABEL_PROJECT_REIMPORT]
            ?.let {
                ProjectState.Reimport(
                    projectDirectory = projectDirectory,
                    importedByVersion = importedByVersion,
                    currentVersion = currentVersion,
                    reimportRequests = it,
                    refreshRequests = groupedPRs[ProjectImportConstants.PR_LABEL_PROJECT_REFRESH] ?: emptyList()
                )
            }
            ?: groupedPRs[ProjectImportConstants.PR_LABEL_PROJECT_REFRESH]?.let {
                ProjectState.Refresh(
                    importedByVersion = importedByVersion,
                    currentVersion = currentVersion,
                    refreshRequests = it
                )
            }
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
