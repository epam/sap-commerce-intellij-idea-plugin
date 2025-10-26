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

package sap.commerce.toolset.project.view

import com.intellij.ide.projectView.impl.nodes.ProjectViewDirectoryHelper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import sap.commerce.toolset.ifHybrisProject

@Service(Service.Level.PROJECT)
class HybrisProjectViewDirectoryHelper(private val project: Project) {

   fun getTopLevelRoots(): MutableList<VirtualFile> {
        val topLevelContentRoots = ProjectViewDirectoryHelper.getInstance(project).getTopLevelRoots()

        project.ifHybrisProject {
            val prm = ProjectRootManager.getInstance(project)

            for (root in prm.contentRoots) {
                val parent = root.parent
                if (!isFileUnderContentRoot(parent)) {
                    topLevelContentRoots.add(root)
                }
            }
        }

        return topLevelContentRoots
    }

    private fun isFileUnderContentRoot(file: VirtualFile?): Boolean {
        if (file == null) return false
        if (!file.isValid) return false

        val contentRoot = ProjectFileIndex.getInstance(project).getContentRootForFile(file, false)
            ?: return false

        return !contentRoot.name.endsWith("core-customize")
    }

    companion object {
        fun getInstance(project: Project): HybrisProjectViewDirectoryHelper = project.service()
    }
}