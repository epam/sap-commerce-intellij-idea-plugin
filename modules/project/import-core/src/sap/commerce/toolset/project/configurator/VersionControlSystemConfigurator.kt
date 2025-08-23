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

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.roots.VcsRootDetector
import com.intellij.openapi.vfs.VfsUtil
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor

class VersionControlSystemConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Version Control System"

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val project = hybrisProjectDescriptor.project ?: return
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        val rootDetector = VcsRootDetector.getInstance(project)
        val detectedRoots = HashSet(rootDetector.detect())

        val roots = hybrisProjectDescriptor.detectedVcs
            .mapNotNull { VfsUtil.findFileByIoFile(it, true) }
            .flatMap { rootDetector.detect(it) }
        detectedRoots.addAll(roots)

        vcsManager.directoryMappings = detectedRoots
            .filter { it.vcs != null }
            .map { VcsDirectoryMapping(it.path.path, it.vcs!!.name) }
    }
}