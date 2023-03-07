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

import com.intellij.diagram.AbstractDiagramElementManager
import com.intellij.idea.plugin.hybris.actions.ActionUtils
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramElementManager
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphNode
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpGraphService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager

class BpDiagramElementManagerIml : AbstractDiagramElementManager<BpGraphNode>(), BpDiagramElementManager {

    override fun findInDataContext(dataContext: DataContext): BpGraphNode? {
        if (!ActionUtils.isHybrisContext(dataContext)) return null

        return ApplicationManager.getApplication().getService(BpGraphService::class.java).buildGraphFromXmlFile(
            CommonDataKeys.PROJECT.getData(dataContext),
            CommonDataKeys.VIRTUAL_FILE.getData(dataContext)
        )
    }

    override fun isAcceptableAsNode(o: Any?) = o is BpGraphNode
    override fun getElementTitle(t: BpGraphNode) = t.navigableElement.getId().stringValue
    override fun getNodeTooltip(t: BpGraphNode) = t.navigableElement.getId().stringValue
}
