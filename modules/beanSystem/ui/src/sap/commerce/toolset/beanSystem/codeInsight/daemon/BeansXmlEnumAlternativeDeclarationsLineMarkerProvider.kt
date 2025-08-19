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
package sap.commerce.toolset.beanSystem.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.model.Enum
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.yExtensionName
import javax.swing.Icon

class BeansXmlEnumAlternativeDeclarationsLineMarkerProvider : BeansXmlLineMarkerProvider<XmlAttributeValue>() {

    override fun getName() = i18n("hybris.editor.gutter.bs.beans.enum.alternativeDeclarations.name")
    override fun getIcon(): Icon = HybrisIcons.BeanSystem.ALTERNATIVE_DECLARATION
    override fun tryCast(psi: PsiElement) = psi as? XmlAttributeValue

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<LineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
            ?: return emptyList()

        val parentTag = PsiTreeUtil.getParentOfType(psi, XmlTag::class.java)
            ?: return emptyList()

        val project = parentTag.project
        val dom = DomManager.getDomManager(project).getDomElement(parentTag) as? Enum
            ?: return emptyList()

        if (psi != dom.clazz.xmlAttributeValue) return emptyList()

        return BSMetaModelAccess.getInstance(project).findMetaForDom(dom)
            ?.retrieveAllDoms()
            ?.filter { it != dom }
            ?.map { it.clazz }
            ?.sortedBy { it.module?.yExtensionName() }
            ?.mapNotNull { it.xmlAttributeValue }
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                NavigationGutterIconBuilder
                    .create(icon)
                    .setTargets(it)
                    .setPopupTitle(i18n("hybris.editor.gutter.bs.beans.enum.alternativeDeclarations.popup.title"))
                    .setTooltipText(i18n("hybris.editor.gutter.bs.beans.enum.alternativeDeclarations.tooltip.text"))
                    .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                    .createLineMarkerInfo(leaf)
            }
            ?.let { listOf(it) }
            ?: emptyList()
    }
}