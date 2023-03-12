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
package com.intellij.idea.plugin.hybris.diagram.typeSystem

import com.intellij.diagram.*
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.module.*
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.TSDiagramColorManager
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.TSDiagramDataModel
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Pattern
import javax.swing.Icon

class TSDiagramProvider : BaseDiagramProvider<TSGraphNode>() {

    private val diagramExtras = TSDiagramExtras()

    @Pattern("[a-zA-Z0-9_-]*")
    override fun getID() = "HybrisTypeSystemDependencies"
    override fun getPresentableName() = message("hybris.diagram.ts.provider.name")
    override fun getActionIcon(isPopup: Boolean): Icon = HybrisIcons.TYPE_SYSTEM

    override fun createDataModel(
        project: Project,
        node: TSGraphNode?,
        virtualFile: VirtualFile?,
        model: DiagramPresentationModel
    ) = TSDiagramDataModel(project, this)

    override fun createNodeContentManager() = TSDiagramNodeContentManager()
    override fun createVisibilityManager() = TSDiagramVisibilityManager()
    override fun createScopeManager(project: Project) = TSDiagramScopeManager(project)
    override fun getColorManager() = TSDiagramColorManager()
    override fun getElementManager() = TSDiagramElementManager()
    override fun getVfsResolver() = TSDiagramVfsResolver()
    override fun getExtras() = diagramExtras

}