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

package com.intellij.idea.plugin.hybris.diagram.typeSystem.node

import com.intellij.diagram.DiagramNodeBase
import com.intellij.diagram.DiagramProvider
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNode
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNodeClassifier
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import javax.swing.Icon

class TSDiagramNode(val graphNode: TSGraphNode, provider: DiagramProvider<TSGraphNode>) : DiagramNodeBase<TSGraphNode>(provider) {

    override fun getIdentifyingElement() = graphNode
    override fun getTooltip() = graphNode.name
    override fun getIcon(): Icon? = identifyingElement
        .takeIf { it is TSGraphNodeClassifier }
        ?.let { it as TSGraphNodeClassifier }
        ?.let {
            when (it.meta) {
                is TSGlobalMetaEnum -> HybrisIcons.TS_ENUM
                is TSGlobalMetaItem -> HybrisIcons.TS_ITEM
                is TSGlobalMetaCollection -> HybrisIcons.TS_COLLECTION
                is TSGlobalMetaMap -> HybrisIcons.TS_MAP
                is TSGlobalMetaRelation -> HybrisIcons.TS_RELATION
                else -> HybrisIcons.TYPE_SYSTEM
            }
        }

    companion object {
        private const val serialVersionUID: Long = -8508256123440006334L
    }
}