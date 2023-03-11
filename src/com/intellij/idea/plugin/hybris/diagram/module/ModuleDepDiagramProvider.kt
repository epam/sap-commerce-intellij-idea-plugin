/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.diagram.module

import com.intellij.diagram.*
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.diagram.module.*
import com.intellij.idea.plugin.hybris.diagram.module.node.ModuleDepDiagramDataModel
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Pattern
import javax.swing.Icon

class ModuleDepDiagramProvider : BaseDiagramProvider<ModuleDepGraphNode>() {
    @Pattern("[a-zA-Z0-9_-]*")
    override fun getID() = "HybrisModuleDependencies"
    override fun getPresentableName() = HybrisI18NBundleUtils.message("hybris.diagram.module.dependencies.provider.name")
    override fun getActionIcon(isPopup: Boolean): Icon = AllIcons.Actions.DiagramDiff

    override fun createVisibilityManager() = ModuleDepDiagramVisibilityManager()
    override fun createNodeContentManager() = ModuleDepDiagramNodeContentManager()
    override fun getElementManager() = ModuleDepDiagramElementManager()
    override fun getVfsResolver() = ModuleDepDiagramVfsResolver()
    override fun getColorManager() = ModuleDepDiagramColorManager()

    override fun createDataModel(
        project: Project,
        item: ModuleDepGraphNode?,
        file: VirtualFile?,
        model: DiagramPresentationModel
    ) = ModuleDepDiagramDataModel(project, this)
}
