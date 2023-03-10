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

import com.intellij.diagram.DiagramVfsResolver
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager

class BpDiagramVfsResolver : DiagramVfsResolver<BpGraphNode?> {

    override fun getQualifiedName(t: BpGraphNode?) = t
        ?.virtualFile
        ?.url

    override fun resolveElementByFQN(s: String, project: Project) = BpGraphService.getInstance(project)
        .buildRootNode(project, VirtualFileManager.getInstance().findFileByUrl(s))
}
