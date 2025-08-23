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

package sap.commerce.toolset.project.execution.lineMarker

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.modules
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.*
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.extensioninfo.model.Extension
import sap.commerce.toolset.extensioninfo.model.ExtensionInfo
import sap.commerce.toolset.localextensions.model.Extensions
import sap.commerce.toolset.localextensions.model.Hybrisconfig
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.yExtensionName

class RefreshProjectRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        if (element !is XmlToken || element.tokenType != XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) return null
        val xmlAttributeValue = PsiTreeUtil.getParentOfType(element, XmlAttributeValue::class.java) ?: return null
        val xmlFile = element.containingFile as? XmlFile ?: return null
        if (xmlAttributeValue.value == HybrisConstants.EXTENSION_NAME_PLATFORM) return null
        val descriptor = ProjectSettings.getInstance(xmlFile.project).availableExtensions[xmlAttributeValue.value]
            ?: return null
        if (descriptor.type != ModuleDescriptorType.OOTB && descriptor.type != ModuleDescriptorType.CUSTOM) return null
        val parentTagName = PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag::class.java)?.localName
            ?: return null

        val domManager = DomManager.getDomManager(xmlFile.project)
        val module = xmlFile.project.modules
            .find { it.yExtensionName() == xmlAttributeValue.value }

        if (module != null) return null

        if ((parentTagName == Extension.REQUIRES_EXTENSION && domManager.getFileElement(xmlFile, ExtensionInfo::class.java) != null)
            || (parentTagName == Extensions.EXTENSION && domManager.getFileElement(xmlFile, Hybrisconfig::class.java) != null)
        ) {

            val action = ActionManager.getInstance().getAction("sap.commerce.toolset.yRefresh") ?: return null
            return Info(HybrisIcons.Actions.FORCE_REFRESH, arrayOf(action)) { action.templateText }
        }

        return null
    }
}