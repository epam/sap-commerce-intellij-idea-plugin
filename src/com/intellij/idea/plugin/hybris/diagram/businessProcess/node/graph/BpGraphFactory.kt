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

package com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph

import com.intellij.diagram.presentation.DiagramLineType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.BpDiagramEdge
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.BpDiagramEdgeType
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.BpDiagramNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.BpDiagramRelationship
import com.intellij.idea.plugin.hybris.system.businessProcess.model.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import org.apache.commons.lang3.StringUtils

object BpGraphFactory {

    //TODO: mby read all possible "bad" edges from the hybris4intellij.properties file
    private val badEdges = arrayOf("NOK", "ERROR", "FAIL", "ON ERROR")

    fun buildNode(project: Project, virtualFile: VirtualFile): BpGraphNode? {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? XmlFile ?: return null
        val fileElement = DomManager.getDomManager(project).getFileElement(psiFile, Process::class.java)

        if (fileElement == null || !fileElement.isValid || !fileElement.rootElement.isValid) return null

        val process = fileElement.rootElement

        return BpGraphNodeRoot(
            process.name.stringValue ?: virtualFile.nameWithoutExtension,
            process,
            virtualFile,
            process
        )
    }

    fun buildNode(nodeName: String, element: NavigableElement, rootGraphNode: BpGraphNodeRoot) = when (element) {
        is ScriptAction -> build(nodeName, element, rootGraphNode)
        is Action -> build(nodeName, element, rootGraphNode)
        is End -> build(nodeName, element, rootGraphNode)
        is Wait -> build(nodeName, element, rootGraphNode)
        is Notify -> build(nodeName, element, rootGraphNode)
        else -> build(nodeName, element, rootGraphNode)
    }

    fun buildEdge(transitionName: String, source: BpDiagramNode, target: BpDiagramNode) = if (source == target) {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target), BpDiagramEdgeType.CYCLE)
    } else if ("Start".equals(transitionName, true)) {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target), BpDiagramEdgeType.START)
    } else if ("Cancel".equals(transitionName, true)) {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target), BpDiagramEdgeType.CANCEL)
    } else if ("Partial".equals(transitionName, true)) {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target), BpDiagramEdgeType.PARTIAL)
    } else if (transitionName.isBlank() || "OK".equals(transitionName, ignoreCase = true)) {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target), BpDiagramEdgeType.OK)
    } else if (StringUtils.startsWith(transitionName, HybrisI18NBundleUtils.message("hybris.business.process.timeout"))) {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target), BpDiagramEdgeType.TIMEOUT)
    } else if (badEdges.contains(transitionName.uppercase())) {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target), BpDiagramEdgeType.NOK)
    } else {
        BpDiagramEdge(source, target, buildRelationship(transitionName, source, target))
    }

    private fun buildRelationship(transitionName: String, source: BpDiagramNode, target: BpDiagramNode): BpDiagramRelationship {
        val sourceNodeImportant = source.graphNode.navigableElement is End || source.graphNode.navigableElement is Process
        val targetNodeImportant = target.graphNode.navigableElement is End || target.graphNode.navigableElement is Process
        var width = 1

        if (sourceNodeImportant) width++
        if (targetNodeImportant) width++

        val lineType = if (source == target) DiagramLineType.DASHED
        else DiagramLineType.SOLID

        return BpDiagramRelationship(
            label = transitionName,
            lineType = lineType,
            width = width
        )
    }

    private fun build(nodeName: String, element: Action, rootGraphNode: BpGraphNodeRoot): BpGraphNode {
        val properties = mutableListOf(
            BpGraphFieldParameter(Action.CAN_JOIN_PREVIOUS_NODE, (element.canJoinPreviousNode.stringValue
                ?: "false"))
        )
        element.node.stringValue
            ?.let { properties.add(BpGraphFieldParameter(Action.NODE, it)) }
        element.nodeGroup.stringValue
            ?.let { properties.add(BpGraphFieldParameter(Action.NODE_GROUP, it)) }
        element.bean.stringValue
            ?.let { properties.add(BpGraphFieldParameter(Action.BEAN, it)) }
        element.parameters
            .forEach {
                properties.add(BpGraphFieldParameter(it.name.stringValue ?: "", it.value.stringValue ?: ""))
            }

        return BpGraphNodeDefault(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties.toTypedArray())
    }

    private fun build(nodeName: String, element: End, rootGraphNode: BpGraphNodeRoot): BpGraphNode {
        val properties = (element.state.stringValue
            ?.let { arrayOf(BpGraphFieldParameter(End.STATE, it)) }
            ?: emptyArray())
        return BpGraphNodeDefault(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties)
    }

    private fun build(nodeName: String, element: Wait, rootGraphNode: BpGraphNodeRoot): BpGraphNode {
        val properties = mutableListOf(
            BpGraphFieldParameter(Wait.PREPEND_PROCESS_CODE, (element.prependProcessCode.stringValue
                ?: "true"))
        )
        return BpGraphNodeDefault(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties.toTypedArray())
    }

    private fun build(nodeName: String, element: Notify, rootGraphNode: BpGraphNodeRoot): BpGraphNode {
        val properties = element.userGroups
            .filter { it.name.stringValue?.isNotEmpty() ?: false }
            .map { BpGraphFieldParameter(it.name.stringValue!!, "") }
            .toTypedArray()
        return BpGraphNodeDefault(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties)
    }

    private fun build(nodeName: String, element: ScriptAction, rootGraphNode: BpGraphNodeRoot): BpGraphNode {
        val properties = element.script.type.stringValue
            ?.let { arrayOf(BpGraphFieldParameter(ScriptAction.SCRIPT, it)) }
            ?: emptyArray()
        return BpGraphNodeDefault(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process, properties)
    }

    private fun build(nodeName: String, element: DomElement, rootGraphNode: BpGraphNodeRoot) = BpGraphNodeDefault(nodeName, element, rootGraphNode.virtualFile, rootGraphNode.process)

}