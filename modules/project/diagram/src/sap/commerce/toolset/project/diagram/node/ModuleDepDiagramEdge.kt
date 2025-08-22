/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.project.diagram.node

import com.intellij.diagram.DiagramEdgeBase
import com.intellij.diagram.DiagramRelationshipInfo
import sap.commerce.toolset.project.diagram.node.graph.ModuleDepGraphNode
import java.io.Serial


// TODO: improve static fields usage
class ModuleDepDiagramEdge(
    source: ModuleDepDiagramNode,
    target: ModuleDepDiagramNode,
    relationship: DiagramRelationshipInfo
) : DiagramEdgeBase<ModuleDepGraphNode>(source, target, relationship) {

    @JvmField
    var circleNumber = -1

    @JvmField
    var numberOfCircles = 0

    fun isCircular() = circleNumber > -1

    companion object {
        @Serial
        private val serialVersionUID: Long = -7068334246818700799L
    }
}
