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
package com.intellij.idea.plugin.hybris.impex.project.view

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectViewPane
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import org.jetbrains.annotations.Unmodifiable

class LocalizedImpExNode(
    name: String,
    project: Project,
    children: Collection<AbstractTreeNode<*>>,
    viewSettings: ViewSettings,
    private val presentableName: String = "$name.${ImpexFileType.defaultExtension}",
) : ProjectViewNode<Collection<AbstractTreeNode<*>>>(project, children, viewSettings) {

    override fun contains(file: VirtualFile) = ProjectViewPane.canBeSelectedInProjectView(myProject, file)

    override fun getChildren(): @Unmodifiable Collection<AbstractTreeNode<*>?> = this.value

    override fun update(presentation: PresentationData) = with(presentation) {
        setIcon(HybrisIcons.ImpEx.BUNDLE)
        addText(ColoredFragment(presentableName, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES))
        addText(ColoredFragment(" bundle", SimpleTextAttributes.GRAY_ATTRIBUTES))
        addText(ColoredFragment(" ${children.size} files", SimpleTextAttributes.GRAY_ATTRIBUTES))
    }

    // PsiFileNode = 20
    override fun getWeight() = 20
}
