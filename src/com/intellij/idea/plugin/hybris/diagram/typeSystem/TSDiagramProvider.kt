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
import com.intellij.idea.plugin.hybris.diagram.module.*
import com.intellij.idea.plugin.hybris.diagram.typeSystem.impl.*
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.TSDiagramDataModel
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.TSGraphItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TSDiagramProvider : BaseDiagramProvider<TSGraphItem>() {

    override fun getID() = "HybrisTypeSystemDependencies"
    override fun getPresentableName() = message("hybris.diagram.ts.provider.name")

    override fun createDataModel(
        project: Project,
        node: TSGraphItem?,
        virtualFile: VirtualFile?,
        model: DiagramPresentationModel
    ) = with(TSDiagramDataModel(project, TSGraphItem(), this)) {
        refreshDataModel()
        this
    }

    override fun getColorManager() = TSDiagramColorManagerImpl()
    override fun createNodeContentManager() = TSDiagramNodeContentManager.instance
//    override fun createVisibilityManager() = TSDiagramVisibilityManager.instance
    override fun getElementManager() = TSDiagramElementManager.instance
    override fun getVfsResolver() = TSDiagramVfsResolver.instance
}