/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.diagram.module.execution.lineMarker

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.ExtensionInfo
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.xml.DomManager

class ShowModuleDepDiagramRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        val xmlFile = element.containingFile as? XmlFile ?: return null
        if (element !is XmlToken) return null
        if (element.parent !is XmlTag) return null
        val prevSibling = element.prevSibling as? XmlToken ?: return null
        if (prevSibling.tokenType != XmlTokenType.XML_START_TAG_START) return null

        if (element.text != HybrisConstants.ROOT_TAG_EXTENSION_INFO_XML) return null
        if (DomManager.getDomManager(xmlFile.project).getFileElement(xmlFile, ExtensionInfo::class.java) == null) return null

        val action = ActionManager.getInstance().getAction("ShowModuleDependencyDiagram") ?: return null
        return Info(HybrisIcons.Actions.SHOW_DIAGRAM, arrayOf(action)) { action.templateText }
    }
}
