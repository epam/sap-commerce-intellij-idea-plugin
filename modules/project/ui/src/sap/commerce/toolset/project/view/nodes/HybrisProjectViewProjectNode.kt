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

package sap.commerce.toolset.project.view.nodes

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesNode
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.module.impl.LoadedModuleDescriptionImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.directory
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.view.HybrisProjectViewDirectoryHelper
import javax.swing.Icon

// TODO: remove this class and migrate to new Workspace Model API
class HybrisProjectViewProjectNode(project: Project, viewSettings: ViewSettings) : ProjectViewProjectNode(project, viewSettings) {

    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> {
        val project = myProject
        if (project == null || project.isDisposed || project.isDefault) {
            return mutableListOf()
        }

        val modules = HybrisProjectViewDirectoryHelper.getInstance(project).getTopLevelRoots()
            .mapNotNull { ModuleUtilCore.findModuleForFile(it, project) }
            .map { LoadedModuleDescriptionImpl(it) }

        val nodes = modulesAndGroups(modules).toMutableList()

        project.directory
            ?.let { LocalFileSystem.getInstance().findFileByPath(it) }
            ?.let { baseDir ->
                val psiManager = PsiManager.getInstance(project)
                val virtualFiles = baseDir.children
                var projectFileIndex: ProjectFileIndex? = null
                val aiDirectories = mutableListOf<PsiDirectoryNode>()
                for (vf in virtualFiles) {
                    if (!vf.isDirectory) {
                        if (projectFileIndex == null) {
                            projectFileIndex = ProjectFileIndex.getInstance(getProject())
                        }
                        if (projectFileIndex.getModuleForFile(vf, false) == null) {
                            val psiFile = psiManager.findFile(vf)
                            if (psiFile != null) {
                                nodes.add(PsiFileNode(getProject(), psiFile, settings))
                            }
                        }
                    } else {
                        when (vf.name) {
                            ProjectConstants.Directory.GITHUB -> psiManager.findDirectory(vf)
                                ?.let { PsiDirectoryNode(getProject(), it, settings) }
                                ?.let { nodes.add(it) }

                            HybrisConstants.Ai.CLAUDE -> aiAgentNode(psiManager, vf, HybrisIcons.AI.AGENT_CLAUDE)
                                ?.let { aiDirectories.add(it) }

                            HybrisConstants.Ai.GEMINI -> aiAgentNode(psiManager, vf, HybrisIcons.AI.AGENT_GEMINI)
                                ?.let { aiDirectories.add(it) }

                            HybrisConstants.Ai.JUNIE -> aiAgentNode(psiManager, vf, HybrisIcons.AI.AGENT_JUNIE)
                                ?.let { aiDirectories.add(it) }

                            HybrisConstants.Ai.CURSOR -> aiAgentNode(psiManager, vf, HybrisIcons.AI.AGENT_CURSOR)
                                ?.let { aiDirectories.add(it) }

                            HybrisConstants.Ai.AGENTS -> aiAgentNode(psiManager, vf, HybrisIcons.AI.AGENTS)
                                ?.let { aiDirectories.add(it) }

                            HybrisConstants.Ai.ASSISTANT -> aiAgentNode(psiManager, vf, HybrisIcons.AI.ASSISTANT)
                                ?.let { aiDirectories.add(it) }
                        }
                    }
                }

                if (aiDirectories.isNotEmpty()) {
                    nodes.add(AiProjectViewNode(getProject(), aiDirectories, settings))
                }
            }

        if (settings.isShowLibraryContents) {
            nodes.add(ExternalLibrariesNode(project, settings))
        }
        return nodes
    }

    private fun aiAgentNode(psiManager: PsiManager, vf: VirtualFile, icon: Icon) = psiManager.findDirectory(vf)
        ?.let { AiAgentPsiDirectoryNode(project, it, settings, icon) }
}