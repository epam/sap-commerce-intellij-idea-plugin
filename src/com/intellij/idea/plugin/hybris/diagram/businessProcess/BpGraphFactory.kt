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

import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphDefaultNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphParameterNodeField
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphRootNode
import com.intellij.idea.plugin.hybris.system.businessProcess.model.*
import com.intellij.util.xml.DomElement

object BpGraphFactory {

    fun buildNode(nodeName: String, element: Action, rootGraphNode: BpGraphRootNode): BpGraphNode {
        val properties = mutableListOf(
            BpGraphParameterNodeField(Action.CAN_JOIN_PREVIOUS_NODE, (element.canJoinPreviousNode.stringValue
                ?: "false"))
        )
        element.node.stringValue
            ?.let { properties.add(BpGraphParameterNodeField(Action.NODE, it)) }
        element.nodeGroup.stringValue
            ?.let { properties.add(BpGraphParameterNodeField(Action.NODE_GROUP, it)) }
        element.bean.stringValue
            ?.let { properties.add(BpGraphParameterNodeField(Action.BEAN, it)) }
        element.parameters
            .forEach {
                properties.add(BpGraphParameterNodeField(it.name.stringValue ?: "", it.value.stringValue ?: ""))
            }

        return BpGraphDefaultNode(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties.toTypedArray())
    }

    fun buildNode(nodeName: String, element: End, rootGraphNode: BpGraphRootNode): BpGraphNode {
        val properties = (element.state.stringValue
            ?.let { arrayOf(BpGraphParameterNodeField(End.STATE, it)) }
            ?: emptyArray())
        return BpGraphDefaultNode(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties)
    }

    fun buildNode(nodeName: String, element: Wait, rootGraphNode: BpGraphRootNode): BpGraphNode {
        val properties = mutableListOf(
            BpGraphParameterNodeField(Wait.PREPEND_PROCESS_CODE, (element.prependProcessCode.stringValue
                ?: "true"))
        )
        return BpGraphDefaultNode(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties.toTypedArray())
    }

    fun buildNode(nodeName: String, element: Notify, rootGraphNode: BpGraphRootNode): BpGraphNode {
        val properties = element.userGroups
            .filter { it.name.stringValue?.isNotEmpty() ?: false }
            .map { BpGraphParameterNodeField(it.name.stringValue!!, "") }
            .toTypedArray()
        return BpGraphDefaultNode(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties)
    }

    fun buildNode(nodeName: String, element: ScriptAction, rootGraphNode: BpGraphRootNode): BpGraphNode {
        val properties = element.script.type.stringValue
            ?.let { arrayOf(BpGraphParameterNodeField(ScriptAction.SCRIPT, it)) }
            ?: emptyArray()
        return BpGraphDefaultNode(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties)
    }

    fun buildNode(nodeName: String, element: DomElement, rootGraphNode: BpGraphRootNode) = BpGraphDefaultNode(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process)


}