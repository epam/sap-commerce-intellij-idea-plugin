/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.diagram.typeSystem

import com.intellij.diagram.AbstractDiagramElementManager
import com.intellij.diagram.DiagramBuilder
import com.intellij.idea.plugin.hybris.actions.ActionUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes

class TSDiagramElementManager : AbstractDiagramElementManager<TSGraphNode>() {

    override fun findInDataContext(dataContext: DataContext) = if (ActionUtils.isHybrisContext(dataContext))
        TSGraphNodeRoot()
    else null

    override fun isAcceptableAsNode(item: Any?) = item is TSGraphNode
    override fun getElementTitle(node: TSGraphNode?) = node?.name
    override fun getNodeTooltip(node: TSGraphNode?) = node?.tooltip
    override fun getNodeItems(node: TSGraphNode?) = node?.fields?.toTypedArray()
        ?: emptyArray()

    override fun getPresentableElementTitle(element: TSGraphNode?, builder: DiagramBuilder) = element
        ?.let {
            with(SimpleColoredText()) {
                if (it.collapsed) {
                    this.append("collapsed", SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
                    this.append("  ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
                this.append(element.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)

                this
            }
        }
        ?: super.getPresentableElementTitle(element, builder)

    override fun getItemName(nodeElement: TSGraphNode?, nodeItem: Any?, builder: DiagramBuilder) = when (nodeItem) {
        is TSGraphField -> SimpleColoredText(nodeItem.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> null
    }

    override fun getItemType(nodeElement: TSGraphNode?, nodeItem: Any?, builder: DiagramBuilder?) = when (nodeItem) {
        is TSGraphFieldDeployment -> SimpleColoredText(nodeItem.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        is TSGraphFieldProperty -> SimpleColoredText(nodeItem.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        is TSGraphFieldTyped -> SimpleColoredText(nodeItem.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        is TSGraphFieldIndex -> SimpleColoredText(nodeItem.type, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        is TSGraphFieldAttribute -> SimpleColoredText(nodeItem.type, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        is TSGraphFieldRelationElement -> SimpleColoredText(nodeItem.type, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        is TSGraphFieldRelationEnd -> SimpleColoredText(nodeItem.type, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        else -> null
    }

    override fun getItemIcon(nodeElement: TSGraphNode?, nodeItem: Any?, builder: DiagramBuilder?) = when (nodeItem) {
        is TSGraphFieldEnumValue -> HybrisIcons.TypeSystem.ENUM_VALUE
        is TSGraphFieldProperty -> HybrisIcons.TypeSystem.Diagram.PROPERTY
        is TSGraphFieldTyped -> HybrisIcons.TypeSystem.Diagram.PROPERTY
        is TSGraphFieldDeployment -> HybrisIcons.TypeSystem.Diagram.DEPLOYMENT
        is TSGraphFieldAttribute -> HybrisIcons.TypeSystem.ATTRIBUTE
        is TSGraphFieldCustomProperty -> HybrisIcons.TypeSystem.CUSTOM_PROPERTY
        is TSGraphFieldRelationEnd -> if (nodeItem.meta.end == TSMetaRelation.RelationEnd.SOURCE) HybrisIcons.TypeSystem.RELATION_SOURCE
        else HybrisIcons.TypeSystem.RELATION_TARGET

        is TSGraphFieldRelationElement -> if (nodeItem.meta.end == TSMetaRelation.RelationEnd.SOURCE) HybrisIcons.TypeSystem.RELATION_SOURCE
        else HybrisIcons.TypeSystem.RELATION_TARGET

        is TSGraphFieldIndex -> {
            if (nodeItem.meta.isRemove) HybrisIcons.TypeSystem.INDEX_REMOVE
            else if (nodeItem.meta.isReplace) HybrisIcons.TypeSystem.INDEX_REPLACE
            else if (nodeItem.meta.isUnique) HybrisIcons.TypeSystem.INDEX_UNIQUE
            else HybrisIcons.TypeSystem.INDEX
        }

        else -> null
    }

}
