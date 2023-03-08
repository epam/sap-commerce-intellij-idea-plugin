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

import com.intellij.diagram.AbstractDiagramElementManager
import com.intellij.diagram.DiagramBuilder
import com.intellij.idea.plugin.hybris.actions.ActionUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramElementManager
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphService
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.graph.BpGraphParameterNodeField
import com.intellij.idea.plugin.hybris.system.businessProcess.model.Action
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes

class BpDiagramElementManagerIml : AbstractDiagramElementManager<BpGraphNode>(), BpDiagramElementManager {

    override fun findInDataContext(dataContext: DataContext): BpGraphNode? {
        if (!ActionUtils.isHybrisContext(dataContext)) return null

        return BpGraphService.getInstance().buildRootNode(
            CommonDataKeys.PROJECT.getData(dataContext),
            CommonDataKeys.VIRTUAL_FILE.getData(dataContext)
        )
    }

    override fun isAcceptableAsNode(o: Any?) = o is BpGraphNode
    override fun getElementTitle(t: BpGraphNode) = t.name
    override fun getNodeTooltip(t: BpGraphNode) = t.name

    override fun getNodeItems(parent: BpGraphNode?): Array<out Any> = parent?.properties ?: emptyArray()

    override fun getItemName(nodeElement: BpGraphNode?, nodeItem: Any?, builder: DiagramBuilder) = when (nodeItem) {
        is BpGraphParameterNodeField -> SimpleColoredText(nodeItem.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> null
    }

    override fun getItemType(element: Any?) = when (element) {
        is BpGraphParameterNodeField -> SimpleColoredText(element.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> null
    }

    override fun getItemIcon(nodeElement: BpGraphNode?, nodeItem: Any?, builder: DiagramBuilder?) = when (nodeItem) {
        is BpGraphParameterNodeField -> when (nodeItem.name) {
            Action.BEAN -> HybrisIcons.BS_DIAGRAM_SPRING_BEAN
            Action.NODE -> HybrisIcons.BS_DIAGRAM_NODE
            Action.NODE_GROUP -> HybrisIcons.BS_DIAGRAM_NODE
            Action.CAN_JOIN_PREVIOUS_NODE -> HybrisIcons.BS_DIAGRAM_FIELD
            else -> HybrisIcons.BS_DIAGRAM_PROPERTY
        }

        else -> null
    }

    override fun getElementTitle(element: BpGraphNode?, builder: DiagramBuilder) = element?.name
}
