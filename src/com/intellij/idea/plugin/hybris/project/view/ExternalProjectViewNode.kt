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

import com.intellij.ide.projectView.*
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes

class ExternalProjectViewNode(
    project: Project,
    children: List<AbstractTreeNode<*>>,
    viewSettings: ViewSettings?
) : ProjectViewNode<List<AbstractTreeNode<*>>>(project, children, viewSettings) {

    override fun getChildren(): Collection<AbstractTreeNode<*>> = this.value

    override fun contains(file: VirtualFile) = this.value
        .filterIsInstance<ProjectViewNode<*>>()
        .mapNotNull { it.virtualFile }
        .any { it == file && file.path.startsWith(it.path) }

    public override fun update(presentation: PresentationData) {
        with(presentation) {
            setIcon(HybrisIcons.MODULE_EXTERNAL_GROUP)
            val groupNameExternalModules = ApplicationSettingsComponent.getInstance().state.groupNameExternalModules
                .takeIf { it.isNotBlank() }
                ?: message("hybris.project.view.external.module.directory.name")
            addText(groupNameExternalModules, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
    }

    override fun getSortOrder(settings: NodeSortSettings) = NodeSortOrder.MODULE_ROOT
}