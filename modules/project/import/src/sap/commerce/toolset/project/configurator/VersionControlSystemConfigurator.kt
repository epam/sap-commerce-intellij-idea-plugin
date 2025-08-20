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
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.roots.VcsRootDetector
import com.intellij.openapi.vfs.VfsUtil
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptor

class VersionControlSystemConfigurator : ProjectImportConfigurator {

    override fun configure(
        project: Project,
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        moduleDescriptors: Map<String, ModuleDescriptor>,
        rootProjectModifiableModel: ModifiableModuleModel,
        modifiableModelsProvider: IdeModifiableModelsProvider,
        cache: ConfiguratorCache
    ) {
        indicator.text = i18n("hybris.project.import.vcs")

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