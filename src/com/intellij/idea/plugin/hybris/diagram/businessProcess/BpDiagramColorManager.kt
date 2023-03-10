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
package com.intellij.idea.plugin.hybris.diagram.businessProcess

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramColorManagerBase
import com.intellij.diagram.DiagramEdge
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_CANCEL
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_CYCLE
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_DEFAULT
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_NOK
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_OK
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_PARTIAL
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_START
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColors.EDGE_TIMEOUT
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.BpDiagramEdge
import com.intellij.idea.plugin.hybris.diagram.businessProcess.node.BpDiagramEdgeType

/**
 * TODO: Add user-defined project-based mapping for custom transition names
 */
class BpDiagramColorManager : DiagramColorManagerBase() {

    override fun getEdgeColorKey(builder: DiagramBuilder, edge: DiagramEdge<*>) = when (edge) {
        is BpDiagramEdge -> when (edge.type) {
            BpDiagramEdgeType.OK -> EDGE_OK
            BpDiagramEdgeType.NOK -> EDGE_NOK
            BpDiagramEdgeType.START -> EDGE_START
            BpDiagramEdgeType.CANCEL -> EDGE_CANCEL
            BpDiagramEdgeType.PARTIAL -> EDGE_PARTIAL
            BpDiagramEdgeType.CYCLE -> EDGE_CYCLE
            BpDiagramEdgeType.TIMEOUT -> EDGE_TIMEOUT
            else -> EDGE_DEFAULT
        }

        else -> EDGE_DEFAULT
    }

}
