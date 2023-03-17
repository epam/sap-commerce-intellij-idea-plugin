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

import com.intellij.diagram.DiagramNodeBase
import com.intellij.diagram.DiagramProvider
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNodeContextParameters
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNodeNavigable
import com.intellij.idea.plugin.hybris.system.businessProcess.model.*
import java.io.Serial

class BpDiagramNode(val graphNode: BpGraphNode, provider: DiagramProvider<BpGraphNode?>) : DiagramNodeBase<BpGraphNode>(provider) {

    override fun getIdentifyingElement() = graphNode
    override fun getTooltip() = graphNode.name

    override fun getIcon() = when (graphNode) {
        is BpGraphNodeContextParameters -> HybrisIcons.BP_DIAGRAM_PARAMETERS
        is BpGraphNodeNavigable -> when (graphNode.navigableElement) {
            is Process -> HybrisIcons.BUSINESS_PROCESS
            is ScriptAction -> HybrisIcons.BP_DIAGRAM_SCRIPT
            is Action -> HybrisIcons.BP_DIAGRAM_ACTION
            is Split -> HybrisIcons.BP_DIAGRAM_SPLIT
            is Wait -> HybrisIcons.BP_DIAGRAM_WAIT
            is Join -> HybrisIcons.BP_DIAGRAM_JOIN
            is End -> HybrisIcons.BP_DIAGRAM_END
            is Notify -> HybrisIcons.BP_DIAGRAM_NOTIFY
            else -> null
        }

        else -> null
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 1719193590375939088L
    }
}
