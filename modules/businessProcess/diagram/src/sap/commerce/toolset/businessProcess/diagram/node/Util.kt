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

import org.apache.commons.collections.CollectionUtils
import sap.commerce.toolset.businessProcess.diagram.node.graph.*
import sap.commerce.toolset.businessProcess.model.*
import sap.commerce.toolset.i18n

internal fun BpGraphNodeRoot.buildNodes(): Map<String, BpGraphNode> {
    this.name = this.process.name.stringValue
        ?: this.virtualFileName
    this.transitions.clear()

    val nodes = this.process.nodes
        .filter { it.getId().isValid }

    val nodesMap = nodes
        .filter { it.getId().stringValue != null }
        .associate {
            val nodeName = it.getId().stringValue!!
            nodeName to BpGraphFactory.buildNode(nodeName, it, this)
        }
    populateNodesTransitions(nodesMap, nodes)

    this.navigableElement.start.stringValue
        ?.let { nodesMap[it] }
        ?.let { this.transitions["Start"] = it }
    this.navigableElement.onError.stringValue
        ?.let { nodesMap[it] }
        ?.let { this.transitions["On Error"] = it }

    val contextParametersNode = this.process.contextParameters
        .filter { it.name.stringValue != null && it.type.stringValue != null }
        .map {
            BpGraphFieldContextParameter(it.name.stringValue!!, it.type.stringValue!!, it.use.value ?: ParameterUse.OPTIONAL)
        }
        .takeIf { it.isNotEmpty() }
        ?.let { BpGraphFactory.buildNode("Context Parameters", this, it.toTypedArray()) }

    return if (contextParametersNode == null) nodesMap
    else {
        val mutableNodes: MutableMap<String, BpGraphNode> = nodesMap.toMutableMap()
        mutableNodes["Context Parameters"] = contextParametersNode

        mutableNodes
    }
}

private fun populateNodesTransitions(
    nodesMap: Map<String, BpGraphNodeNavigable>,
    nodes: List<NavigableElement>
) {
    nodes.forEach {
        nodesMap[it.getId().stringValue]
            ?.let { actionGraphNode ->
                getTransitionIdsForAction(it).forEach { (key, value) ->
                    nodesMap[value]
                        ?.let { node ->
                            actionGraphNode.transitions[key] = node
                        }
                }
            }
    }
}

private fun getTransitionIdsForAction(navigableElement: NavigableElement): Map<String, String?> = when (navigableElement) {
    is Join -> mapOf("" to navigableElement.then.stringValue)

    is Notify -> mapOf("" to navigableElement.then.stringValue)

    is ScriptAction -> navigableElement.transitions
        .associate { (it.name.stringValue ?: "") to it.to.stringValue }

    is Action -> navigableElement.transitions
        .associate { (it.name.stringValue ?: "") to it.to.stringValue }

    is Split -> navigableElement.targetNodes
        .associate { (it.name.stringValue ?: "") to it.name.stringValue }

    is Wait -> {
        val transitions = mutableMapOf(
            "" to navigableElement.then.stringValue
        )

        if (navigableElement.case.isValid && CollectionUtils.isNotEmpty(navigableElement.case.choices)) {
            navigableElement.case.choices
                .filter { it.getId().stringValue != null }
                .forEach { transitions[it.getId().stringValue!!] = it.then.stringValue }
        }
        if (navigableElement.timeout.isValid) {
            transitions["${i18n("hybris.diagram.bp.provider.edge.timeout")} ${navigableElement.timeout.delay}"] = navigableElement.timeout.then.stringValue
        }
        transitions
    }

    else -> emptyMap()
}
