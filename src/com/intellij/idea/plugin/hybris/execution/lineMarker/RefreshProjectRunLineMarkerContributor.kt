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
package com.intellij.idea.plugin.hybris.execution.lineMarker

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.Extension
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.ExtensionInfo
import com.intellij.idea.plugin.hybris.system.localextensions.model.Extensions
import com.intellij.idea.plugin.hybris.system.localextensions.model.Hybrisconfig
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.modules
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.*
import com.intellij.util.xml.DomManager

class RefreshProjectRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        if (element !is XmlToken || element.tokenType != XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) return null
        val xmlAttributeValue = PsiTreeUtil.getParentOfType(element, XmlAttributeValue::class.java) ?: return null
        val xmlFile = element.containingFile as? XmlFile ?: return null
        if (xmlAttributeValue.value == HybrisConstants.EXTENSION_NAME_PLATFORM) return null
        val descriptor = HybrisProjectSettingsComponent.getInstance(xmlFile.project).getAvailableExtensions()[xmlAttributeValue.value]
            ?: return null
        if (descriptor.type != HybrisModuleDescriptorType.OOTB && descriptor.type != HybrisModuleDescriptorType.CUSTOM) return null
        val parentTagName = PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag::class.java)?.localName
            ?: return null

        val domManager = DomManager.getDomManager(xmlFile.project)
        val module = xmlFile.project.modules
            .find { it.name == xmlAttributeValue.value }

        if (module != null) return null

        if ((parentTagName == Extension.REQUIRES_EXTENSION && domManager.getFileElement(xmlFile, ExtensionInfo::class.java) != null)
            || (parentTagName == Extensions.EXTENSION && domManager.getFileElement(xmlFile, Hybrisconfig::class.java) != null)) {

            val action = ActionManager.getInstance().getAction("File.yRefresh") ?: return null
            return Info(AllIcons.Actions.ForceRefresh, arrayOf(action)) { action.templateText }
        }

        return null
    }
}
