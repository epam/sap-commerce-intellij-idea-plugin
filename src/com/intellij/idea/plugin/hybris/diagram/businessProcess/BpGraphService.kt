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
package com.intellij.idea.plugin.hybris.diagram.businessProcess

import com.intellij.idea.plugin.hybris.diagram.businessProcess.impl.BpDiagramFileEdge
import com.intellij.idea.plugin.hybris.diagram.businessProcess.impl.BpDiagramFileNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.impl.BpRootGraphNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface BpGraphService {
    fun buildRootNode(project: Project?, virtualFile: VirtualFile?): BpGraphNode?

    fun buildNodes(rootGraphNode: BpRootGraphNode): Map<String, BpGraphNode>

    fun buildEdge(name: String, source: BpDiagramFileNode, target: BpDiagramFileNode): BpDiagramFileEdge

    companion object {
        fun getInstance(): BpGraphService = ApplicationManager.getApplication().getService(BpGraphService::class.java)
    }
}
