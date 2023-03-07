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

class DefaultBpGraphService : BpGraphService {

    override fun buildGraphFromXmlFile(project: Project?, virtualFile: VirtualFile?): BpGraphNode? {
        if (project == null || virtualFile == null) return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? XmlFile ?: return null
        val fileElement = DomManager.getDomManager(project).getFileElement(psiFile, Process::class.java)

        if (fileElement == null || !fileElement.isValid || !fileElement.rootElement.isValid) return null

        val process = fileElement.rootElement

        if (!process.start.isValid) return null

        val nodes = process.nodes
            .filter { it.getId().isValid }

        if (CollectionUtils.isEmpty(nodes)) return null

        val nodesMap = buildNodesMap(virtualFile, process, nodes)
        populateNodesTransitions(nodesMap, nodes)
        return nodesMap[process.start.stringValue]
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

    private fun getTransitionIdsForAction(navigableElement: NavigableElement): Map<String, String?> {
        val transitionsIds: MutableMap<String, String?> = HashMap()

        when (navigableElement) {
            is Join -> {
                transitionsIds[""] = navigableElement.then.stringValue
            }

            is Notify -> {
                transitionsIds[""] = navigableElement.then.stringValue
            }

            is Action -> {
                for (transition in navigableElement.transitions) {
                    transitionsIds[transition.name.stringValue!!] = transition.to.stringValue
                }
            }

            is Split -> {
                for (targetNode in navigableElement.targetNodes) {
                    transitionsIds[""] = targetNode.name.stringValue
                }
            }

            is Wait -> {
                transitionsIds[""] = navigableElement.then.stringValue
                if (navigableElement.case.isValid && CollectionUtils.isNotEmpty(navigableElement.case.choices)) {
                    for (choice in navigableElement.case.choices) {
                        transitionsIds[choice.getId().stringValue!!] = choice.then.stringValue
                    }
                }
                if (navigableElement.timeout.isValid) {
                    transitionsIds["${message("hybris.business.process.timeout")} ${navigableElement.timeout.delay}"] = navigableElement.timeout.then.stringValue
                }
            }

            is ScriptAction -> {
                for (transition in navigableElement.transitions) {
                    transitionsIds[""] = transition.to.stringValue
                }
            }

        }

        return transitionsIds
    }

    private fun buildNodesMap(
        virtualFile: VirtualFile,
        process: Process,
        nodes: List<NavigableElement>
    ): Map<String, BpGraphNode> {
        val nodesMap: MutableMap<String, BpGraphNode> = HashMap()

        for (action in nodes) {
            nodesMap[action.getId().stringValue!!] = DefaultBpGraphNode(action, nodesMap, virtualFile, process)
        }
        return nodesMap
    }
}
