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

package com.intellij.idea.plugin.hybris.diagram.typeSystem

import com.intellij.diagram.DiagramColors
import com.intellij.openapi.editor.colors.ColorKey

object TSDiagramColors {

    val EDGE_EXTENDS = ColorKey.createColorKeyWithFallback("TS_DIAGRAM_EDGE_EXTENDS", DiagramColors.DEFAULT_EDGE)
    val EDGE_PART_OF = ColorKey.createColorKeyWithFallback("TS_DIAGRAM_EDGE_PART_OF", DiagramColors.DEFAULT_EDGE)
    val EDGE_DEPENDENCY = ColorKey.createColorKeyWithFallback("TS_DIAGRAM_EDGE_DEPENDENCY", DiagramColors.DEFAULT_EDGE)
    val EDGE_TRANSITIVE_DEPENDENCY = ColorKey.createColorKeyWithFallback("TS_DIAGRAM_EDGE_TRANSITIVE_DEPENDENCY", DiagramColors.DEFAULT_EDGE)
    val NODE_HEADER_CUSTOM = ColorKey.createColorKeyWithFallback("TS_DIAGRAM_NODE_HEADER_CUSTOM", DiagramColors.NODE_HEADER)

}