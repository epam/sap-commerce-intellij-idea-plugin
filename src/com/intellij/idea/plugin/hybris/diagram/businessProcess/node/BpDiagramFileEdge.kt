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
package com.intellij.idea.plugin.hybris.diagram.businessProcess.node

import com.intellij.diagram.DiagramEdgeBase
import com.intellij.diagram.DiagramNode
import com.intellij.diagram.DiagramRelationshipInfo
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNode
import java.io.Serial

abstract class BpDiagramFileEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo
) : DiagramEdgeBase<BpGraphNode>(source, target, relationship) {

    companion object {
        @Serial
        private const val serialVersionUID: Long = 2559027965802259164L
    }
}

class BpDiagramFileCycleEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1109695684478969817L
    }
}

class BpDiagramFileStartEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 5732011492071389878L
    }
}

class BpDiagramFileOKEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -261196942015337711L
    }
}

class BpDiagramFileNOKEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 6605498804231635111L
    }
}

class BpDiagramFileCancelEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -5502578267095995858L
    }
}

class BpDiagramFilePartialEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2843747361304072195L
    }
}

class BpDiagramFileTimeoutEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -1236008898547054199L
    }
}

class BpDiagramFileDefaultEdge(
    source: DiagramNode<BpGraphNode>,
    target: DiagramNode<BpGraphNode>,
    relationship: DiagramRelationshipInfo,
) : BpDiagramFileEdge(source, target, relationship) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -8473761360059983132L
    }
}
