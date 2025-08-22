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
package sap.commerce.toolset.project.diagram

import com.intellij.diagram.DiagramVfsResolver
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import sap.commerce.toolset.project.diagram.node.graph.ModuleDepGraphFactory
import sap.commerce.toolset.project.diagram.node.graph.ModuleDepGraphNode
import sap.commerce.toolset.project.diagram.node.graph.ModuleDepGraphNodeModule

class ModuleDepDiagramVfsResolver : DiagramVfsResolver<ModuleDepGraphNode> {

    override fun getQualifiedName(item: ModuleDepGraphNode?) = item
        ?.takeIf { it is ModuleDepGraphNodeModule }
        ?.name

    override fun resolveElementByFQN(fqn: String, project: Project) = fqn
        .takeIf { fqn == "null" }
        ?.let { ModuleManager.getInstance(project).findModuleByName(fqn) }
        ?.let { ModuleDepGraphFactory.buildNode(it) }

}
