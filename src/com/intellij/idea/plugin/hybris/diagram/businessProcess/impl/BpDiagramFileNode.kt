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

import com.intellij.diagram.DiagramNodeBase
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramProvider
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphNode
import com.intellij.idea.plugin.hybris.system.businessProcess.model.*
import com.intellij.openapi.application.ApplicationManager
import java.io.Serial

class BpDiagramFileNode(private val diagramNode: BpGraphNode) : DiagramNodeBase<BpGraphNode>(ApplicationManager.getApplication().getService(BpDiagramProvider::class.java)) {

    override fun getIdentifyingElement() = diagramNode
    override fun getTooltip() = identifyingElement.nodeName

    override fun getIcon() = when (diagramNode.navigableElement) {
        is Process -> HybrisIcons.BUSINESS_PROCESS
        is Action -> HybrisIcons.BP_DIAGRAM_ACTION
        is Split -> HybrisIcons.BP_DIAGRAM_SPLIT
        is Wait -> HybrisIcons.BP_DIAGRAM_WAIT
        is Join -> HybrisIcons.BP_DIAGRAM_JOIN
        is End -> HybrisIcons.BP_DIAGRAM_END
        is ScriptAction -> HybrisIcons.BP_DIAGRAM_SCRIPT
        is Notify -> HybrisIcons.BP_DIAGRAM_NOTIFY
        else -> null
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 1719193590375939088L
    }
}
