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

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramEdge
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.CANCEL
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.CYCLE
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.DEFAULT
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.NOK
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.OK
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.PARTIAL
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.START
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpColors.TIMEOUT
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramColorManager

/**
 * TODO: Add user-defined project-based mapping for custom transition names
 */
class BpDiagramColorManagerIml : BpDiagramColorManager() {

    override fun getEdgeColorKey(builder: DiagramBuilder, edge: DiagramEdge<*>) = when (edge) {
        is BpDiagramFileOKEdge -> OK
        is BpDiagramFileNOKEdge -> NOK
        is BpDiagramFileStartEdge -> START
        is BpDiagramFileCancelEdge -> CANCEL
        is BpDiagramFilePartialEdge -> PARTIAL
        is BpDiagramFileCycleEdge -> CYCLE
        is BpDiagramFileTimeoutEdge -> TIMEOUT
        else -> DEFAULT
    }

}
