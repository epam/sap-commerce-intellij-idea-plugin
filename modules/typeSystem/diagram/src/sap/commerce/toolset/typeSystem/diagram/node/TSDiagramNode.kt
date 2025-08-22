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

package sap.commerce.toolset.typeSystem.diagram.node

import com.intellij.diagram.DiagramNodeBase
import com.intellij.diagram.DiagramProvider
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.typeSystem.diagram.node.graph.TSGraphNode
import sap.commerce.toolset.typeSystem.diagram.node.graph.TSGraphNodeClassifier
import sap.commerce.toolset.typeSystem.meta.model.*
import java.io.Serial
import javax.swing.Icon

class TSDiagramNode(val graphNode: TSGraphNode, provider: DiagramProvider<TSGraphNode>) : DiagramNodeBase<TSGraphNode>(provider) {

    override fun getIdentifyingElement() = graphNode
    override fun getTooltip() = graphNode.name
    override fun getIcon(): Icon? = identifyingElement
        .asSafely<TSGraphNodeClassifier>()
        ?.let {
            when (it.meta) {
                is TSGlobalMetaEnum -> HybrisIcons.TypeSystem.Types.ENUM
                is TSGlobalMetaItem -> HybrisIcons.TypeSystem.Types.ITEM
                is TSGlobalMetaCollection -> HybrisIcons.TypeSystem.Types.COLLECTION
                is TSGlobalMetaMap -> HybrisIcons.TypeSystem.Types.MAP
                is TSGlobalMetaRelation -> HybrisIcons.TypeSystem.Types.RELATION
                else -> HybrisIcons.TypeSystem.FILE
            }
        }

    companion object {
        @Serial
        private val serialVersionUID: Long = -8508256123440006334L
    }
}