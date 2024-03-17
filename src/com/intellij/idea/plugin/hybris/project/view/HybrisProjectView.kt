/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.facet.YFacet
import com.intellij.idea.plugin.hybris.project.services.HybrisProjectService
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import java.io.File

open class HybrisProjectView(val project: Project) : TreeStructureProvider, DumbAware {

    private val hybrisProjectSettingsComponent = HybrisProjectSettingsComponent.getInstance(project)
    private val hybrisProject = hybrisProjectSettingsComponent.state.hybrisProject
    private val hybrisApplicationSettings = HybrisApplicationSettingsComponent.getInstance().state
    private val commerceGroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.groupHybris)
        ?.firstOrNull()
    private val platformGroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.groupPlatform)
        ?.firstOrNull()
    private val ccv2GroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.groupCCv2)
        ?.firstOrNull()
    private val customGroupName = HybrisApplicationSettingsComponent.toIdeaGroup(hybrisApplicationSettings.groupCustom)
        ?.firstOrNull()
    private val groupToIcon = mapOf(
        customGroupName to HybrisIcons.MODULE_CUSTOM_GROUP,
        platformGroupName to HybrisIcons.MODULE_PLATFORM_GROUP,
        commerceGroupName to HybrisIcons.MODULE_COMMERCE_GROUP,
        ccv2GroupName to HybrisIcons.MODULE_CCV2_GROUP,
    )
    private val hideModuleLibraries = setOf(
        "- Backoffice Classes",
        "- Web Classes",
        "- Common Web Classes",
        "- Addon's Target Classes",
        "- HAC Web Classes",
        "- lib",
        "- Backoffice lib",
        "- compiler output",
        "- resources",
        "- server",
    )

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

        if (parent is JunkProjectViewNode || parent is ExternalProjectViewNode) {
            return if (isCompactEmptyMiddleFoldersEnabled(settings)) compactEmptyMiddlePackages(parent, newChildren)
            else newChildren
        }

        if (parent is ExternalLibrariesNode) return modifyExternalLibrariesNodes(newChildren)
        val externalModules = processExternalModules(parent, newChildren, settings)
        val childrenWithProcessedJunkFiles = processJunkFiles(externalModules, settings)

        return if (isCompactEmptyMiddleFoldersEnabled(settings)) compactEmptyMiddlePackages(parent, childrenWithProcessedJunkFiles)
        else childrenWithProcessedJunkFiles
    }

    private fun processExternalModules(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings?
    ): Collection<AbstractTreeNode<*>> {
        if (!hybrisApplicationSettings.groupExternalModules || isExternalModuleParent(parent)) return children

        val otherNodes = mutableListOf<AbstractTreeNode<*>>()
        val treeNodes = mutableListOf<AbstractTreeNode<*>>()

        val projectService = ApplicationManager.getApplication().getService(HybrisProjectService::class.java)
        val projectRootManager = ProjectRootManager.getInstance(project)
        for (child in children) {
            if (child is PsiDirectoryNode) {
                val virtualFile = child.virtualFile
                    ?: continue

                val file = VfsUtil.virtualToIoFile(virtualFile)
                val yFacet = projectRootManager.fileIndex.getModuleForFile(virtualFile)
                    ?.let { YFacet.get(it) }

                if (yFacet == null && (projectService.isGradleModule(file)
                        || projectService.isEclipseModule(file)
                        || projectService.isGradleKtsModule(file)
                        || projectService.isMavenModule(file))
                ) {
                    otherNodes.add(child)
                } else {
                    treeNodes.add(child)
                }
            } else {
                treeNodes.add(child)
            }
        }
        if (otherNodes.isNotEmpty()) {
            treeNodes.add(ExternalProjectViewNode(project, otherNodes, settings))
        }
        return treeNodes
    }

    private fun isExternalModuleParent(child: AbstractTreeNode<*>): Boolean {
        return if (child is ExternalProjectViewNode) true
        else if (child.parent != null) isExternalModuleParent(child.parent)
        else false
    }

    private fun replaceModuleGroupNodes(children: MutableCollection<AbstractTreeNode<*>>) {
        val nodes = children as? MutableList<AbstractTreeNode<*>>
            ?: return

        for (i in nodes.indices) {
            nodes[i]
                .let { it as? ProjectViewModuleGroupNode }
                ?.let { node ->
                    val yNode = YProjectViewModuleGroupNode(node.project, node.value, node.settings)
                    yNode.value
                        ?.groupPath
                        ?.firstOrNull()
                        ?.let { groupName ->
                            groupToIcon
                                .firstNotNullOfOrNull { if (groupName.equals(it.key, true)) it.value else null }
                        }
                        ?.let { yNode.icon = it }

                    yNode
                }
                ?.let { nodes.set(i, it) }

        }
    }

    private fun isNodeVisible(parent: AbstractTreeNode<*>, node: AbstractTreeNode<*>): Boolean {
        if (node !is PsiDirectoryNode) return true

        val vf = node.virtualFile
            ?: return true

        // hide `core-customize/hybris` node
        if (HybrisConstants.HYBRIS_DIRECTORY == vf.name
            && HybrisConstants.CCV2_CORE_CUSTOMIZE_NAME == parent.name
        ) return false

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

    private fun isCompactEmptyMiddleFoldersEnabled(settings: ViewSettings) = hybrisApplicationSettings.hideEmptyMiddleFolders
        && settings.isHideEmptyMiddlePackages

    private fun modifyExternalLibrariesNodes(
        children: Collection<AbstractTreeNode<*>>
    ): Collection<AbstractTreeNode<*>> {
        val treeNodes = mutableListOf<AbstractTreeNode<*>>()

        for (child in children) {
            when (child) {
                is PsiDirectoryNode -> {
                    val virtualFile = child.virtualFile ?: continue
                    if (!HybrisConstants.CLASSES_DIRECTORY.equals(virtualFile.name, ignoreCase = true) &&
                        !HybrisConstants.RESOURCES_DIRECTORY.equals(virtualFile.name, ignoreCase = true)
                    ) {
                        treeNodes.add(child)
                    }
                }

                is NamedLibraryElementNode -> {
                    val libraryName = child.value.name
                    if (hideModuleLibraries.none { libraryName.endsWith(it) }) {
                        treeNodes.add(child)
                    }
                }

                else -> treeNodes.add(child)
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

    private fun recursivelyCompactEmptyMiddlePackages(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>
    ): AbstractTreeNode<*> {
        if (children.isEmpty()) return parent
        if (parent is JunkProjectViewNode) return parent
        if (parent is ExternalProjectViewNode) return parent
        if (parent !is PsiDirectoryNode) return parent

        val onlyChild = children
            .takeIf { it.size == 1 }
            ?.firstOrNull()
            ?.let { it as? PsiDirectoryNode }
            ?: return parent

        val parentVirtualFile = parent.virtualFile
            ?: return parent

        if (isFileInRoots(parentVirtualFile) || isSrcOrClassesDirectory(parentVirtualFile)) return parent

        val onlyChildVirtualFile = onlyChild.virtualFile
            ?: return parent

        appendParentNameToOnlyChildName(parent, parentVirtualFile, onlyChild, onlyChildVirtualFile)
        return recursivelyCompactEmptyMiddlePackages(onlyChild, onlyChild.children)
    }

    private fun appendParentNameToOnlyChildName(
        parentPsiDirectoryNode: PsiDirectoryNode,
        parentVirtualFile: VirtualFile,
        onlyChildPsiDirectoryNode: PsiDirectoryNode,
        onlyChildVirtualFile: VirtualFile
    ) {
        if (parentPsiDirectoryNode.presentation.coloredText.isEmpty()) {
            onlyChildPsiDirectoryNode.presentation.addText(parentVirtualFile.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        } else {
            parentPsiDirectoryNode.presentation.coloredText
                .forEach { onlyChildPsiDirectoryNode.presentation.addText(it) }
        }
        onlyChildPsiDirectoryNode.presentation.addText(File.separator, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        onlyChildPsiDirectoryNode.presentation.addText(onlyChildVirtualFile.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    private fun isFileInRoots(file: VirtualFile): Boolean {
        val index = ProjectRootManager.getInstance(project).fileIndex
        return index.isInSource(file) || index.isInLibraryClasses(file)
    }

    private fun isSrcOrClassesDirectory(file: VirtualFile) = HybrisConstants.ADDON_SRC_DIRECTORY == file.name
        || HybrisConstants.CLASSES_DIRECTORY == file.name
        || HybrisConstants.TEST_CLASSES_DIRECTORY == file.name

    private fun isJunk(virtualFile: VirtualFile, junkFileNames: List<String>) = junkFileNames.contains(virtualFile.name)
        || isIdeaModuleFile(virtualFile)

    private fun isIdeaModuleFile(virtualFile: VirtualFile) = virtualFile
        .name
        .endsWith(HybrisConstants.NEW_IDEA_MODULE_FILE_EXTENSION)

}
