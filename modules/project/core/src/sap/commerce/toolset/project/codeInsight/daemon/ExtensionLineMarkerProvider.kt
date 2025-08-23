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

package sap.commerce.toolset.project.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.modules
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.codeInsight.daemon.HybrisLineMarkerProvider
import sap.commerce.toolset.extensioninfo.EiModelAccess
import sap.commerce.toolset.project.settings.ProjectSettings
import sap.commerce.toolset.project.yExtensionName
import javax.swing.Icon

abstract class ExtensionLineMarkerProvider : HybrisLineMarkerProvider<XmlAttributeValue>() {

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
        val descriptor = ProjectSettings.getInstance(psi.project).availableExtensions[psi.value]
            ?: return emptyList()
        val extensionInfoName = psi.project.modules
            .find { it.yExtensionName() == psi.value }
            ?.let { EiModelAccess.getExtensionInfo(it) }
            ?.xmlTag
            ?: return emptyList()

        val marker = NavigationGutterIconBuilder
            .create(descriptor.type.icon)
            .setTargets(extensionInfoName)
            .setPopupTitle(getPopupTitle())
            .setTooltipText(getTooltipText())
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .createLineMarkerInfo(leaf)

        return listOf(marker)
    }
}