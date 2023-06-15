/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.view

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.BasePsiNode
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesNode
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleGroupNode
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.facet.YFacet
import com.intellij.idea.plugin.hybris.kotlin.yExtensionName
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.Validate
import java.io.File

open class HybrisProjectView(val project: Project) : TreeStructureProvider, DumbAware {

    private val hybrisProjectSettingsComponent = HybrisProjectSettingsComponent.getInstance(project)
    private val hybrisProject = hybrisProjectSettingsComponent.state.hybrisProject
    private val hybrisApplicationSettings = HybrisApplicationSettingsComponent.getInstance().state
    private val commerceGroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.groupHybris)
        .firstOrNull()
    private val platformGroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.groupPlatform)
        .firstOrNull()
    private val ccv2GroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.groupCCv2)
        .firstOrNull()

    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        settings: ViewSettings
    ): Collection<AbstractTreeNode<*>> {
        if (!hybrisProject) return children

        replaceModuleGroupNodes(children)

        val newChildren = children
            .filter { isNodeVisible(parent, it) }
            .toMutableList()

        if (parent is JunkProjectViewNode) {
            return if (isCompactEmptyMiddleFoldersEnabled(settings)) compactEmptyMiddlePackages(parent, newChildren)
            else newChildren
        }

        if (parent is YProjectViewModuleGroupNode) modifyModuleGroupIcons(parent)
        if (parent is ExternalLibrariesNode) return modifyExternalLibrariesNodes(newChildren)

        val childrenWithProcessedJunkFiles = processJunkFiles(newChildren, settings)

        return if (isCompactEmptyMiddleFoldersEnabled(settings)) compactEmptyMiddlePackages(parent, childrenWithProcessedJunkFiles)
        else childrenWithProcessedJunkFiles
    }

    private fun replaceModuleGroupNodes(children: MutableCollection<AbstractTreeNode<*>>) {
        val nodes = children as? MutableList<AbstractTreeNode<*>>
            ?: return

        for (i in nodes.indices) {
            nodes[i]
                .let { it as? ProjectViewModuleGroupNode }
                ?.let { YProjectViewModuleGroupNode(it.project, it.value, it.settings) }
                ?.let { nodes.set(i, it) }

        }
    }

    private fun isNodeVisible(parent: AbstractTreeNode<*>, node: AbstractTreeNode<*>): Boolean {
        if (node !is PsiDirectoryNode) return true

        val vf = node.virtualFile
            ?: return true
        val module = ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(vf)
            ?: return true

        // hide `platform/ext` node
        if (HybrisConstants.PLATFORM_EXTENSIONS_DIRECTORY_NAME == vf.name
            && HybrisConstants.EXTENSION_NAME_PLATFORM == module.yExtensionName()
        ) return false

        return YFacet.getState(module)
            ?.let {
                if (it.subModuleType == null) true
                else parent !is ProjectViewModuleGroupNode
            }
            ?: true
    }

    private fun modifyModuleGroupIcons(parent: ProjectViewModuleGroupNode) {
        val rootGroup = parent.value
            ?.groupPath
            ?.firstOrNull()
            ?: return

        mapOf(
            platformGroupName to HybrisIcons.MODULE_PLATFORM_GROUP,
            commerceGroupName to HybrisIcons.MODULE_COMMERCE_GROUP,
            ccv2GroupName to HybrisIcons.MODULE_CCV2_GROUP,
        )
            .firstNotNullOfOrNull { if (rootGroup.equals(it.key, true)) it.value else null }
            ?.let { parent.icon = it }
    }

    private fun isCompactEmptyMiddleFoldersEnabled(settings: ViewSettings) = hybrisApplicationSettings.hideEmptyMiddleFolders
        && settings.isHideEmptyMiddlePackages

    private fun modifyExternalLibrariesNodes(
        children: Collection<AbstractTreeNode<*>>
    ): Collection<AbstractTreeNode<*>> {
        val treeNodes = mutableListOf<AbstractTreeNode<*>>()

        for (child in children) {
            if (child is PsiDirectoryNode) {
                val virtualFile = child.virtualFile ?: continue
                if (!HybrisConstants.CLASSES_DIRECTORY.equals(virtualFile.name, ignoreCase = true)) {
                    treeNodes.add(child)
                }
            } else {
                treeNodes.add(child)
            }
        }
        return treeNodes
    }

    private fun processJunkFiles(
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings?
    ): Collection<AbstractTreeNode<*>> {
        val junkFileNames = HybrisApplicationSettingsComponent.getInstance().state.junkDirectoryList
            .takeIf { it.isNotEmpty() }
            ?: return children

        val junkTreeNodes = mutableListOf<AbstractTreeNode<*>>()
        val treeNodes = mutableListOf<AbstractTreeNode<*>>()

        for (child in children) {
            if (child is BasePsiNode<*>) {
                val virtualFile = child.virtualFile
                    ?: continue

                if (isJunk(virtualFile, junkFileNames)) {
                    junkTreeNodes.add(child)
                } else {
                    treeNodes.add(child)
                }
            } else {
                treeNodes.add(child)
            }
        }
        if (junkTreeNodes.isNotEmpty()) {
            treeNodes.add(JunkProjectViewNode(project, junkTreeNodes, settings))
        }

        return treeNodes
    }

    private fun compactEmptyMiddlePackages(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>
    ): Collection<AbstractTreeNode<*>> {
        if (children.isEmpty()) return children

        if (parent is PsiDirectoryNode) {
            val parentVirtualFile = parent.virtualFile

            if (null != parentVirtualFile && isFileInRoots(parentVirtualFile)) {
                return children
            }
        }

        return children
            .map { recursivelyCompactEmptyMiddlePackages(it, it.children) }
            .toMutableList()
    }

    protected fun recursivelyCompactEmptyMiddlePackages(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>
    ): AbstractTreeNode<*> {
        if (children.isEmpty()) return parent
        if (parent is JunkProjectViewNode) return parent

        if (parent is PsiDirectoryNode) {
            val directory = children.firstOrNull()
                ?.let { it as? PsiDirectoryNode }
                ?: return parent

            val parentVirtualFile = parent.virtualFile
                ?: return parent

            if (isFileInRoots(parentVirtualFile) || isSrcOrClassesDirectory(parentVirtualFile))
                return parent
            val onlyChildVirtualFile = directory.virtualFile
                ?: return parent

            appendParentNameToOnlyChildName(parent, parentVirtualFile, directory, onlyChildVirtualFile)
            return recursivelyCompactEmptyMiddlePackages(directory, directory.children)
        }
        return parent
    }

    private fun appendParentNameToOnlyChildName(
        parentPsiDirectoryNode: PsiDirectoryNode,
        parentVirtualFile: VirtualFile,
        onlyChildPsiDirectoryNode: PsiDirectoryNode,
        onlyChildVirtualFile: VirtualFile
    ) {
        if (CollectionUtils.isEmpty(parentPsiDirectoryNode.presentation.coloredText)) {
            onlyChildPsiDirectoryNode.presentation.addText(
                PresentableNodeDescriptor
                    .ColoredFragment(parentVirtualFile.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            )
        } else {
            for (coloredFragment in parentPsiDirectoryNode.presentation.coloredText) {
                onlyChildPsiDirectoryNode.presentation.addText(coloredFragment)
            }
        }
        onlyChildPsiDirectoryNode.presentation.addText(
            PresentableNodeDescriptor.ColoredFragment(
                File.separator, SimpleTextAttributes.REGULAR_ATTRIBUTES
            )
        )
        onlyChildPsiDirectoryNode.presentation.addText(
            PresentableNodeDescriptor.ColoredFragment(
                onlyChildVirtualFile.name, SimpleTextAttributes.REGULAR_ATTRIBUTES
            )
        )
    }

    private fun isFileInRoots(file: VirtualFile): Boolean {
        val index = ProjectRootManager.getInstance(project).fileIndex
        return index.isInSource(file) || index.isInLibraryClasses(file)
    }

    private fun isSrcOrClassesDirectory(file: VirtualFile) = HybrisConstants.ADDON_SRC_DIRECTORY == file.name
        || HybrisConstants.CLASSES_DIRECTORY == file.name
        || HybrisConstants.TEST_CLASSES_DIRECTORY == file.name

    protected fun isJunk(virtualFile: VirtualFile, junkFileNames: List<String>): Boolean {
        Validate.notNull(virtualFile)
        Validate.notNull(junkFileNames)
        return junkFileNames.contains(virtualFile.name) || isIdeaModuleFile(virtualFile)
    }

    protected fun isIdeaModuleFile(virtualFile: VirtualFile): Boolean {
        Validate.notNull(virtualFile)
        return virtualFile.name.endsWith(HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION)
    }

}
