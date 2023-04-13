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
package com.intellij.idea.plugin.hybris.diagram.typeSystem.execution.lineMarker

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.xml.DomManager

class ShowTSDiagramRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        val xmlFile = element.containingFile as? XmlFile ?: return null
        if (element !is XmlToken) return null
        if (element.parent !is XmlTag) return null
        val prevSibling = element.prevSibling as? XmlToken ?: return null
        if (prevSibling.tokenType != XmlTokenType.XML_START_TAG_START) return null

        if (element.text != HybrisConstants.ROOT_TAG_ITEMS_XML) return null
        if (DomManager.getDomManager(xmlFile.project).getFileElement(xmlFile, Items::class.java) == null) return null

        val action = ActionManager.getInstance().getAction("ShowTypeSystemDiagram") ?: return null
        return Info(AllIcons.FileTypes.Diagram, arrayOf(action)) { action.templateText }
    }
}
