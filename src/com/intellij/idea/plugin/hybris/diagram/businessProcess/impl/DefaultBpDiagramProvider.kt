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
package com.intellij.idea.plugin.hybris.diagram.businessProcess.impl

import com.intellij.diagram.*
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.diagram.businessProcess.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Pattern

class DefaultBpDiagramProvider : BpDiagramProvider() {

    @Pattern("[a-zA-Z0-9_-]*")
    override fun getID() = "HybrisBusinessProcessDiagramProvider"
    override fun getPresentableName() = HybrisI18NBundleUtils.message("hybris.diagram.business.process.provider.name")

    override fun getElementManager(): DiagramElementManager<BpGraphNode> = ApplicationManager.getApplication().getService(BpDiagramElementManager::class.java)
    override fun getVfsResolver(): BpDiagramVfsResolver = ApplicationManager.getApplication().getService(BpDiagramVfsResolver::class.java)
    override fun getColorManager(): DiagramColorManager = ApplicationManager.getApplication().getService(BpDiagramColorManager::class.java)

    override fun createDataModel(
        project: Project,
        node: BpGraphNode?,
        virtualFile: VirtualFile?,
        diagramPresentationModel: DiagramPresentationModel
    ): DiagramDataModel<BpGraphNode?> {
        val bpDiagramDataModel = BpDiagramDataModel(project, node)
        bpDiagramDataModel.refreshDataModel()
        return bpDiagramDataModel
    }

}
