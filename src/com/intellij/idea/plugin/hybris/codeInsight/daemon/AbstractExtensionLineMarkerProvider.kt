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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.extensioninfo.model.ExtensionInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.*
import com.intellij.util.xml.DomManager
import javax.swing.Icon

abstract class AbstractExtensionLineMarkerProvider : AbstractHybrisLineMarkerProvider<XmlAttributeValue>() {

    override fun getIcon(): Icon = HybrisIcons.HYBRIS
    override fun tryCast(psi: PsiElement) = psi as? XmlAttributeValue
    abstract fun getParentTagName(): String
    abstract fun getTooltipText(): String
    abstract fun getPopupTitle(): String

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<LineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
            ?: return emptyList()
        if (PsiTreeUtil.getParentOfType(psi, XmlTag::class.java)?.localName != getParentTagName()) return emptyList()
        val descriptor = HybrisProjectSettingsComponent.getInstance(psi.project).getAvailableExtensions()[psi.value]
            ?: return emptyList()
        val module = psi.project.modules
            .find { it.name == psi.value }
            ?: return emptyList()
        val extensionInfoName = ModuleRootManager.getInstance(module).contentRoots
            .firstNotNullOfOrNull { it.findFile(HybrisConstants.EXTENSION_INFO_XML) }
            ?.let { PsiManager.getInstance(psi.project).findFile(it) }
            ?.let { it as? XmlFile }
            ?.let { DomManager.getDomManager(psi.project).getFileElement(it, ExtensionInfo::class.java) }
            ?.rootElement
            ?.extension
            ?.name
            ?.xmlAttributeValue
            ?: return emptyList()

        val marker = NavigationGutterIconBuilder
            .create(
                when (descriptor.type) {
                    HybrisModuleDescriptorType.CCV2 -> HybrisIcons.EXTENSION_CLOUD
                    HybrisModuleDescriptorType.CUSTOM -> HybrisIcons.EXTENSION_CUSTOM
                    HybrisModuleDescriptorType.EXT -> HybrisIcons.EXTENSION_EXT
                    HybrisModuleDescriptorType.OOTB -> HybrisIcons.EXTENSION_OOTB
                    HybrisModuleDescriptorType.PLATFORM -> HybrisIcons.EXTENSION_PLATFORM
                    else -> icon
                }
            )
            .setTargets(extensionInfoName)
            .setPopupTitle(getPopupTitle())
            .setTooltipText(getTooltipText())
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .createLineMarkerInfo(leaf)

        return listOf(marker)
    }
}