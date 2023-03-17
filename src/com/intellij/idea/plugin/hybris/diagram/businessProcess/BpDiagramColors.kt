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

import com.intellij.diagram.DiagramColors
import com.intellij.openapi.editor.colors.ColorKey.createColorKeyWithFallback

object BpDiagramColors {

    val EDGE_START = createColorKeyWithFallback("BP_DIAGRAM_EDGE_START", DiagramColors.DEFAULT_EDGE)
    val EDGE_PARTIAL = createColorKeyWithFallback("BP_DIAGRAM_EDGE_PARTIAL", DiagramColors.DEFAULT_EDGE)
    val EDGE_OK = createColorKeyWithFallback("BP_DIAGRAM_EDGE_OK", DiagramColors.DEFAULT_EDGE)
    val EDGE_NOK = createColorKeyWithFallback("BP_DIAGRAM_EDGE_NOK", DiagramColors.DEFAULT_EDGE)
    val EDGE_TIMEOUT = createColorKeyWithFallback("BP_DIAGRAM_EDGE_TIMEOUT", DiagramColors.DEFAULT_EDGE)
    val EDGE_CYCLE = createColorKeyWithFallback("BP_DIAGRAM_EDGE_CYCLE", DiagramColors.DEFAULT_EDGE)
    val EDGE_CANCEL = createColorKeyWithFallback("BP_DIAGRAM_EDGE_CANCEL", DiagramColors.DEFAULT_EDGE)
    val EDGE_DEFAULT = createColorKeyWithFallback("BP_DIAGRAM_EDGE_DEFAULT", DiagramColors.DEFAULT_EDGE)
    val EDGE_PARAMETERS = createColorKeyWithFallback("BP_DIAGRAM_EDGE_PARAMETERS", DiagramColors.DEFAULT_EDGE)
}