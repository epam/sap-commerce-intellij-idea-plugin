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
package com.intellij.idea.plugin.hybris.diagram.businessProcess

import com.intellij.diagram.AbstractDiagramElementManager
import com.intellij.diagram.DiagramBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.*
import com.intellij.idea.plugin.hybris.system.businessProcess.model.Action
import com.intellij.idea.plugin.hybris.system.businessProcess.model.ParameterUse
import com.intellij.idea.plugin.hybris.system.businessProcess.model.Process
import com.intellij.idea.plugin.hybris.util.isNotHybrisProject
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes

class BpDiagramElementManager : AbstractDiagramElementManager<BpGraphNode>() {

    override fun findInDataContext(dataContext: DataContext): BpGraphNode? {
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return null
        val virtualFile = dataContext.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null

        if (project.isNotHybrisProject) return null

        return BpGraphFactory.buildNode(project, virtualFile)
    }

    override fun isAcceptableAsNode(o: Any?) = o is BpGraphNode
    override fun getElementTitle(t: BpGraphNode) = t.name
    override fun getNodeTooltip(t: BpGraphNode) = null
    override fun getNodeItems(parent: BpGraphNode?): Array<out Any> = parent?.properties ?: emptyArray()

    override fun getItemName(nodeElement: BpGraphNode?, nodeItem: Any?, builder: DiagramBuilder) = when (nodeItem) {
        is BpGraphField -> SimpleColoredText(nodeItem.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> null
    }

    override fun getItemType(nodeElement: BpGraphNode?, nodeItem: Any?, builder: DiagramBuilder?) = when (nodeItem) {
        is BpGraphFieldContextParameter -> SimpleColoredText(nodeItem.type, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        is BpGraphFieldParameter -> SimpleColoredText(nodeItem.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> null
    }

    override fun getItemIcon(nodeElement: BpGraphNode?, nodeItem: Any?, builder: DiagramBuilder?) = when (nodeItem) {
        is BpGraphFieldContextParameter -> when (nodeItem.use) {
            ParameterUse.REQUIRED -> HybrisIcons.BusinessProcess.Diagram.PARAMETER_REQUIRED
            else -> HybrisIcons.BusinessProcess.Diagram.PARAMETER_OPTIONAL
        }

        is BpGraphField -> when (nodeItem.name) {
            Action.BEAN -> HybrisIcons.BusinessProcess.Diagram.SPRING_BEAN
            Action.NODE -> HybrisIcons.BusinessProcess.Diagram.NODE
            Action.NODE_GROUP -> HybrisIcons.BusinessProcess.Diagram.NODE
            Action.CAN_JOIN_PREVIOUS_NODE -> HybrisIcons.BusinessProcess.Diagram.FIELD
            Process.DEFAULT_NODE_GROUP -> HybrisIcons.BusinessProcess.Diagram.NODE
            Process.PROCESS_CLASS -> HybrisIcons.BusinessProcess.Diagram.CLASS
            else -> HybrisIcons.BusinessProcess.Diagram.PROPERTY
        }

        else -> null
    }

    override fun getElementTitle(element: BpGraphNode?, builder: DiagramBuilder) = element?.name
}
