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

package sap.commerce.toolset.project.configurator

import com.intellij.openapi.application.readAction
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.roots.VcsRootDetector
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.backend.workspace.WorkspaceModel
import sap.commerce.toolset.project.context.ProjectImportContext

class VersionControlSystemConfigurator : ProjectPostImportAsyncConfigurator {

    override val name: String
        get() = "Version Control System"

    override suspend fun postImport(
        importContext: ProjectImportContext,
        workspaceModel: WorkspaceModel
    ) {
        val project = importContext.project
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        val rootDetector = VcsRootDetector.getInstance(project)

        val roots = importContext.detectedVcs
            .mapNotNull { readAction { VfsUtil.findFile(it, true) } }
            .flatMap { rootDetector.detect(it) }

        val directoryMappings = buildSet {
            addAll(rootDetector.detect())
            addAll(roots)
        }
            .mapNotNull { vcsRoot ->
                val vcs = vcsRoot.vcs ?: return@mapNotNull null

                VcsDirectoryMapping(vcsRoot.path.path, vcs.name)
            }

        vcsManager.setDirectoryMappings(directoryMappings)
    }
}