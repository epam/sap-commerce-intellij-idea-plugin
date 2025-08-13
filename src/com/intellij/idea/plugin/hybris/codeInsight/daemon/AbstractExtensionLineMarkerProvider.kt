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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorType
import com.intellij.idea.plugin.hybris.settings.ProjectSettings
import com.intellij.idea.plugin.hybris.system.extensioninfo.EiSModelAccess
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.modules
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import javax.swing.Icon

abstract class AbstractExtensionLineMarkerProvider : AbstractHybrisLineMarkerProvider<XmlAttributeValue>() {

    override fun getIcon(): Icon = HybrisIcons.Y.LOGO_BLUE
    override fun tryCast(psi: PsiElement) = psi as? XmlAttributeValue
    abstract fun getParentTagName(): String
    abstract fun getTooltipText(): String
    abstract fun getPopupTitle(): String

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<LineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
            ?: return emptyList()
        if (PsiTreeUtil.getParentOfType(psi, XmlTag::class.java)?.localName != getParentTagName()) return emptyList()
        val descriptor = ProjectSettings.getInstance(psi.project).getAvailableExtensions()[psi.value]
            ?: return emptyList()
        val extensionInfoName = psi.project.modules
            .find { it.yExtensionName() == psi.value }
            ?.let { EiSModelAccess.getExtensionInfo(it) }
            ?.xmlTag
            ?: return emptyList()

        val marker = NavigationGutterIconBuilder
            .create(
                when (descriptor.type) {
                    ModuleDescriptorType.CCV2 -> HybrisIcons.Extension.CLOUD
                    ModuleDescriptorType.CUSTOM -> HybrisIcons.Extension.CUSTOM
                    ModuleDescriptorType.EXT -> HybrisIcons.Extension.EXT
                    ModuleDescriptorType.OOTB -> HybrisIcons.Extension.OOTB
                    ModuleDescriptorType.PLATFORM -> HybrisIcons.Extension.PLATFORM
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