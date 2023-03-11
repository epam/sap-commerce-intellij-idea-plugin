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

import com.intellij.diagram.extras.DiagramExtras
import com.intellij.diagram.settings.DiagramConfigElement
import com.intellij.diagram.settings.DiagramConfigGroup
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.TSGraphNode

class TSDiagramExtras : DiagramExtras<TSGraphNode>() {

    private val diagramConfigGroups: Array<DiagramConfigGroup> by lazy {
        val categories = with(DiagramConfigGroup("Categories")) {
            TSDiagramNodeContentManager.CATEGORIES
                .map { DiagramConfigElement(it.name, true) }
                .forEach { addElement(it) }

            this
        }
        arrayOf(categories)
    }

    override fun getAdditionalDiagramSettings() = diagramConfigGroups
    override fun getToolbarActionsProvider() = TSDiagramToolbarActionsProvider.instance
}