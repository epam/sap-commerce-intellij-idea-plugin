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
package sap.commerce.toolset.businessProcess.diagram

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramColorManagerBase
import com.intellij.diagram.DiagramEdge
import sap.commerce.toolset.businessProcess.diagram.node.BpDiagramEdge
import sap.commerce.toolset.businessProcess.diagram.node.BpDiagramEdgeType

/**
 * TODO: Add user-defined project-based mapping for custom transition names
 */
class BpDiagramColorManager : DiagramColorManagerBase() {

    override fun getEdgeColorKey(builder: DiagramBuilder, edge: DiagramEdge<*>) = when (edge) {
        is BpDiagramEdge -> when (edge.type) {
            BpDiagramEdgeType.OK -> BpDiagramColors.EDGE_OK
            BpDiagramEdgeType.NOK -> BpDiagramColors.EDGE_NOK
            BpDiagramEdgeType.START -> BpDiagramColors.EDGE_START
            BpDiagramEdgeType.CANCEL -> BpDiagramColors.EDGE_CANCEL
            BpDiagramEdgeType.PARTIAL -> BpDiagramColors.EDGE_PARTIAL
            BpDiagramEdgeType.CYCLE -> BpDiagramColors.EDGE_CYCLE
            BpDiagramEdgeType.TIMEOUT -> BpDiagramColors.EDGE_TIMEOUT
            BpDiagramEdgeType.PARAMETERS -> BpDiagramColors.EDGE_PARAMETERS
            else -> BpDiagramColors.EDGE_DEFAULT
        }

        else -> BpDiagramColors.EDGE_DEFAULT
    }

}
