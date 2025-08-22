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
package sap.commerce.toolset.businessProcess.diagram.node

import com.intellij.diagram.DiagramNodeBase
import com.intellij.diagram.DiagramProvider
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.businessProcess.diagram.node.graph.BpGraphNode
import sap.commerce.toolset.businessProcess.diagram.node.graph.BpGraphNodeContextParameters
import sap.commerce.toolset.businessProcess.diagram.node.graph.BpGraphNodeNavigable
import sap.commerce.toolset.businessProcess.model.*
import java.io.Serial

class BpDiagramNode(val graphNode: BpGraphNode, provider: DiagramProvider<BpGraphNode?>) : DiagramNodeBase<BpGraphNode>(provider) {

    override fun getIdentifyingElement() = graphNode
    override fun getTooltip() = graphNode.name

    override fun getIcon() = when (graphNode) {
        is BpGraphNodeContextParameters -> HybrisIcons.BusinessProcess.Diagram.PARAMETERS
        is BpGraphNodeNavigable -> when (graphNode.navigableElement) {
            is Process -> HybrisIcons.BusinessProcess.FILE
            is ScriptAction -> HybrisIcons.BusinessProcess.Diagram.SCRIPT
            is Action -> HybrisIcons.BusinessProcess.Diagram.ACTION
            is Split -> HybrisIcons.BusinessProcess.Diagram.SPLIT
            is Wait -> HybrisIcons.BusinessProcess.Diagram.WAIT
            is Join -> HybrisIcons.BusinessProcess.Diagram.JOIN
            is End -> HybrisIcons.BusinessProcess.Diagram.END
            is Notify -> HybrisIcons.BusinessProcess.Diagram.NOTIFY
            else -> null
        }

        else -> null
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 1719193590375939088L
    }
}
