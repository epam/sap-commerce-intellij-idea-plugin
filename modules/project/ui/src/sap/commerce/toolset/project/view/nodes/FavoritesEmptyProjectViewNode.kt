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

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n

class FavoritesEmptyProjectViewNode(
    project: Project,
    viewSettings: ViewSettings?
) : ProjectViewNode<String>(project, "favorites.empty", viewSettings) {

    override fun getChildren(): Collection<AbstractTreeNode<*>> = emptyList()

    override fun contains(file: VirtualFile) = false

    public override fun update(presentation: PresentationData) {
        with(presentation) {
            setIcon(HybrisIcons.ProjectView.FAVORITES_EMPTY)
            addText(i18n("hybris.project.view.favorites.empty.name"), SimpleTextAttributes.GRAYED_ATTRIBUTES)
            addText("  ${i18n("hybris.project.view.favorites.empty.hint")}", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
            tooltip = i18n("hybris.project.view.favorites.empty.tooltip")
        }
    }
}
