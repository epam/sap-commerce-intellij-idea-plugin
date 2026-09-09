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
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
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
            .map { HybrisLoadedModuleDescription(it) }

        val nodes = modulesAndGroups(modules).toMutableList()

        project.directory
            ?.let { LocalFileSystem.getInstance().findFileByPath(it) }
            ?.let { baseDir ->
                val psiManager = PsiManager.getInstance(project)
                val virtualFiles = baseDir.children
                var projectFileIndex: ProjectFileIndex? = null
                val aiNodes = mutableListOf<BasePsiNode<out PsiElement>>()
                for (vf in virtualFiles) {
                    if (!vf.isDirectory) {
                        if (projectFileIndex == null) {
                            projectFileIndex = ProjectFileIndex.getInstance(getProject())
                        }
                        if (projectFileIndex.getModuleForFile(vf, false) == null) {
                            val psiFile = psiManager.findFile(vf)
                            if (psiFile != null) {
                                when (vf.name) {
                                    HybrisConstants.Ai.MD.AGENTS -> aiFileNode(psiManager, vf)
                                        ?.let { aiNodes.add(it) }

                                    HybrisConstants.Ai.MD.GEMINI -> aiFileNode(psiManager, vf)
                                        ?.let { aiNodes.add(it) }

                                    HybrisConstants.Ai.MD.CLAUDE -> aiFileNode(psiManager, vf)
                                        ?.let { aiNodes.add(it) }

                                    HybrisConstants.Ai.MD.CLAUDE_LOCAL -> aiFileNode(psiManager, vf)
                                        ?.let { aiNodes.add(it) }

                                    else -> nodes.add(PsiFileNode(getProject(), psiFile, settings))
                                }
                            }
                        }
                    } else {
                        when (vf.name) {
                            ProjectConstants.Directory.GITHUB -> psiManager.findDirectory(vf)
                                ?.let { PsiDirectoryNode(getProject(), it, settings) }
                                ?.let { nodes.add(it) }

                            HybrisConstants.Ai.CLAUDE -> aiDirectoryNode(psiManager, vf, HybrisIcons.AI.AGENT_CLAUDE)
                                ?.let { aiNodes.add(it) }

                            HybrisConstants.Ai.GEMINI -> aiDirectoryNode(psiManager, vf, HybrisIcons.AI.AGENT_GEMINI)
                                ?.let { aiNodes.add(it) }

                            HybrisConstants.Ai.JUNIE -> aiDirectoryNode(psiManager, vf, HybrisIcons.AI.AGENT_JUNIE)
                                ?.let { aiNodes.add(it) }

                            HybrisConstants.Ai.CURSOR -> aiDirectoryNode(psiManager, vf, HybrisIcons.AI.AGENT_CURSOR)
                                ?.let { aiNodes.add(it) }

                            HybrisConstants.Ai.AGENTS -> aiDirectoryNode(psiManager, vf, HybrisIcons.AI.AGENTS)
                                ?.let { aiNodes.add(it) }

                            HybrisConstants.Ai.CODEMIE -> aiDirectoryNode(psiManager, vf, HybrisIcons.AI.CODEMIE)
                                ?.let { aiNodes.add(it) }

                            HybrisConstants.Ai.ASSISTANT -> aiDirectoryNode(psiManager, vf, HybrisIcons.AI.ASSISTANT)
                                ?.let { aiNodes.add(it) }
                        }
                    }
                }

                if (aiNodes.isNotEmpty()) {
                    nodes.add(AiProjectViewNode(getProject(), aiNodes, settings))
                }
            }

        if (settings.isShowLibraryContents) {
            nodes.add(ExternalLibrariesNode(project, settings))
        }
        return nodes
    }

    private fun aiDirectoryNode(psiManager: PsiManager, vf: VirtualFile, icon: Icon) = psiManager.findDirectory(vf)
        ?.let { AiAgentPsiDirectoryNode(project, it, settings, icon) }

    private fun aiFileNode(psiManager: PsiManager, vf: VirtualFile) = psiManager.findFile(vf)
        ?.let { PsiFileNode(project, it, null) }
}