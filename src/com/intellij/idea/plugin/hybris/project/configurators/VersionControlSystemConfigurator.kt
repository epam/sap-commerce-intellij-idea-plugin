/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.roots.VcsRootDetector
import com.intellij.openapi.vfs.VfsUtil

@Service
class VersionControlSystemConfigurator {

    fun configure(
        indicator: ProgressIndicator,
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        project: Project
    ) {
        indicator.text = HybrisI18NBundleUtils.message("hybris.project.import.vcs")

        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        val rootDetector = project.getService(VcsRootDetector::class.java)
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
