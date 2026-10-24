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

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.SimpleTextAttributes
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.settings.FavoriteFoldersSettings

class FavoritesProjectViewNode(
    project: Project,
    viewSettings: ViewSettings?
) : ProjectViewNode<FavoriteFoldersSettings>(project, FavoriteFoldersSettings.getInstance(project), viewSettings) {

    // Favorites nested into another favorite are not listed twice: they stay at their place within the parent folder
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        val psiManager = PsiManager.getInstance(myProject)
        val fileSystem = LocalFileSystem.getInstance()

        return value.topLevelFolders
            .mapNotNull { fileSystem.findFileByPath(it) }
            .filter { it.isDirectory }
            .mapNotNull { psiManager.findDirectory(it) }
            .map { PsiDirectoryNode(myProject, it, settings) }
            .ifEmpty { listOf(FavoritesEmptyProjectViewNode(myProject, settings)) }
    }

    override fun contains(file: VirtualFile) = value.folders.any { FileUtil.isAncestor(it, file.path, false) }

    public override fun update(presentation: PresentationData) {
        with(presentation) {
            setIcon(HybrisIcons.ProjectView.FAVORITES)
            addText(i18n("hybris.project.view.favorites.directory.name"), SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
    }

    override fun getSortOrder(settings: NodeSortSettings) = NodeSortOrder.PROJECT_ROOT

    override fun getSortKey(): Comparable<*> = 0
    override fun getTypeSortKey(): Comparable<*> = 0
}
