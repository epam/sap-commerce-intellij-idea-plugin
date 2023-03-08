/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.diagram.businessProcess.impl

import com.intellij.diagram.presentation.DiagramLineType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphService
import com.intellij.idea.plugin.hybris.system.businessProcess.model.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils

class DefaultBpGraphService : BpGraphService {

    private val badEdges = arrayOf("NOK", "ERROR", "FAIL", "ON ERROR")

    override fun buildRootNode(project: Project?, virtualFile: VirtualFile?): BpGraphNode? {
        if (project == null || virtualFile == null) return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? XmlFile ?: return null
        val fileElement = DomManager.getDomManager(project).getFileElement(psiFile, Process::class.java)

        if (fileElement == null || !fileElement.isValid || !fileElement.rootElement.isValid) return null

        val process = fileElement.rootElement

        return BpRootGraphNode(
            process.name.stringValue ?: virtualFile.nameWithoutExtension,
            process,
            virtualFile,
            process
        )
    }

    override fun buildNodes(rootGraphNode: BpRootGraphNode): Map<String, BpGraphNode> {
        rootGraphNode.nodeName = rootGraphNode.process.name.stringValue
            ?: rootGraphNode.virtualFile.nameWithoutExtension
        rootGraphNode.transitions.clear()

        val nodes = rootGraphNode.process.nodes
            .filter { it.getId().isValid }

        val nodesMap = nodes
            .filter { it.getId().stringValue != null }
            .associate {
                val nodeName = it.getId().stringValue!!
                nodeName to DefaultBpGraphNode(nodeName, it, rootGraphNode.virtualFile, rootGraphNode.process)
            }
        populateNodesTransitions(nodesMap, nodes)

        rootGraphNode.navigableElement.start.stringValue
            ?.let { nodesMap[it] }
            ?.let { rootGraphNode.transitions["Start"] = it }
        rootGraphNode.navigableElement.onError.stringValue
            ?.let { nodesMap[it] }
            ?.let { rootGraphNode.transitions["On Error"] = it }

        return nodesMap
    }

    override fun buildEdge(name: String, source: BpDiagramFileNode, target: BpDiagramFileNode) = if (source == target) {
        BpDiagramFileCycleEdge(source, target, BpDiagramRelationship(name, DiagramLineType.DASHED))
    } else if ("Start".equals(name, true)) {
        BpDiagramFileStartEdge(source, target, BpDiagramRelationship(name))
    } else if ("Cancel".equals(name, true)) {
        BpDiagramFileCancelEdge(source, target, BpDiagramRelationship(name))
    } else if ("Partial".equals(name, true)) {
        BpDiagramFilePartialEdge(source, target, BpDiagramRelationship(name))
    } else if (name.isBlank() || "OK".equals(name, ignoreCase = true)) {
        BpDiagramFileOKEdge(source, target, BpDiagramRelationship(name))
    } else if (StringUtils.startsWith(name, message("hybris.business.process.timeout"))) {
        BpDiagramFileTimeoutEdge(source, target, BpDiagramRelationship(name))
    } else if (StringUtils.startsWith(name, message("hybris.business.process.timeout"))) {
        BpDiagramFileTimeoutEdge(source, target, BpDiagramRelationship(name))
    } else if (badEdges.contains(name.uppercase())) {
        BpDiagramFileNOKEdge(source, target, BpDiagramRelationship(name))
    } else {
        BpDiagramFileDefaultEdge(source, target, BpDiagramRelationship(name))
    }

    private fun populateNodesTransitions(
        nodesMap: Map<String, BpGraphNode>,
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

        is Action -> navigableElement.transitions
            .associate { (it.name.stringValue ?: "") to it.to.stringValue }

        is Split -> navigableElement.targetNodes
            .associate { (it.name.stringValue ?: "") to it.name.stringValue }

        is ScriptAction -> navigableElement.transitions
            .associate { (it.name.stringValue ?: "") to it.to.stringValue }

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
                transitions["${message("hybris.business.process.timeout")} ${navigableElement.timeout.delay}"] = navigableElement.timeout.then.stringValue
            }
            transitions
        }

        else -> emptyMap()
    }
}
