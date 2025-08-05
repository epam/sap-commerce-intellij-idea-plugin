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

package com.intellij.idea.plugin.hybris.project.view

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesNode
import com.intellij.ide.projectView.impl.nodes.ProjectViewDirectoryHelper
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.module.impl.LoadedModuleDescriptionImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager

// TODO: remove this class and migrate to new Workspace Model API
class HybrisProjectViewProjectNode(project: Project, viewSettings: ViewSettings) : ProjectViewProjectNode(project, viewSettings) {

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val project = myProject
        if (project == null || project.isDisposed || project.isDefault) {
            return mutableListOf()
        }

        val modules = ProjectViewDirectoryHelper.getInstance(project).getTopLevelRoots()
            .mapNotNull { ModuleUtilCore.findModuleForFile(it, project) }
            .map { LoadedModuleDescriptionImpl(it) }

        val nodes = modulesAndGroups(modules).toMutableList()

        val baseDirPath = project.basePath
            ?.let { LocalFileSystem.getInstance().findFileByPath(it) }
            ?.let { baseDir ->
                val psiManager = PsiManager.getInstance(project)
                val files = baseDir.children
                var projectFileIndex: ProjectFileIndex? = null
                for (file in files) {
                    if (!file.isDirectory) {
                        if (projectFileIndex == null) {
                            projectFileIndex = ProjectFileIndex.getInstance(getProject())
                        }
                        if (projectFileIndex.getModuleForFile(file, false) == null) {
                            val psiFile = psiManager.findFile(file)
                            if (psiFile != null) {
                                nodes.add(PsiFileNode(getProject(), psiFile, settings))
                            }
                        }
                    }
                }
            }

        if (settings.isShowLibraryContents) {
            nodes.add(ExternalLibrariesNode(project, settings))
        }
        return nodes
    }
}