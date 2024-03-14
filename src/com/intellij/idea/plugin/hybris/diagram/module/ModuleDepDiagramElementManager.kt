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
package com.intellij.idea.plugin.hybris.diagram.module

import com.intellij.diagram.AbstractDiagramElementManager
import com.intellij.diagram.DiagramBuilder
import com.intellij.idea.plugin.hybris.actions.ActionUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.module.node.graph.*
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes

class ModuleDepDiagramElementManager : AbstractDiagramElementManager<ModuleDepGraphNode>() {

    override fun findInDataContext(dataContext: DataContext) = if (ActionUtils.isHybrisContext(dataContext))
        ModuleDepGraphNodeRoot()
    else null

    override fun isAcceptableAsNode(element: Any?) = element is ModuleDepGraphNode
    override fun getElementTitle(node: ModuleDepGraphNode) = node.name
    override fun getNodeItems(parent: ModuleDepGraphNode?): Array<out Any> = parent?.properties ?: emptyArray()

    override fun getItemName(nodeElement: ModuleDepGraphNode?, nodeItem: Any?, builder: DiagramBuilder) = when (nodeItem) {
        is ModuleDepGraphField -> SimpleColoredText(nodeItem.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> if (nodeElement != null)
            SimpleColoredText(nodeElement.name, DEFAULT_TITLE_ATTR)
        else null
    }

    override fun getItemType(nodeElement: ModuleDepGraphNode?, nodeItem: Any?, builder: DiagramBuilder?) = when (nodeItem) {
        is ModuleDepGraphFieldSubModuleType -> SimpleColoredText(nodeItem.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> null
    }

    override fun getItemIcon(nodeElement: ModuleDepGraphNode?, nodeItem: Any?, builder: DiagramBuilder?) = when (nodeItem) {
        is ModuleDepGraphFieldDescription -> HybrisIcons.MODULE_DEP_DIAGRAM_DESCRIPTION
        is ModuleDepGraphField -> when (nodeItem.name) {
            "Maven Enabled" -> HybrisIcons.MODULE_DEP_DIAGRAM_MAVEN_ENABLED
            "Jalo Logic Free" -> HybrisIcons.MODULE_DEP_DIAGRAM_JALO_LOGIC_FREE
            "Deprecated" -> HybrisIcons.MODULE_DEP_DIAGRAM_DEPRECATED
            else -> HybrisIcons.MODULE_DEP_DIAGRAM_PROPERTY
        }

        else -> null
    }

    override fun getNodeTooltip(element: ModuleDepGraphNode) = element.name
}
