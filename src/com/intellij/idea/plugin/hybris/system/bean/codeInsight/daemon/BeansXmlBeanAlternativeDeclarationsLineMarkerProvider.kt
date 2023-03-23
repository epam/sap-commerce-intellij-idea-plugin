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
package com.intellij.idea.plugin.hybris.system.bean.codeInsight.daemon

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.xml.DomManager
import javax.swing.Icon

class BeansXmlBeanAlternativeDeclarationsLineMarkerProvider : AbstractBeansXmlLineMarkerProvider<XmlAttributeValue>() {

    override fun getName() = message("hybris.editor.gutter.bs.beans.bean.alternativeDeclarations.name")
    override fun getIcon(): Icon = AllIcons.Actions.Forward
    override fun tryCast(psi: PsiElement) = psi as? XmlAttributeValue

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<RelatedItemLineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
            ?: return emptyList()

        val parentTag = PsiTreeUtil.getParentOfType(psi, XmlTag::class.java)
            ?: return emptyList()

        val project = parentTag.project
        val dom = DomManager.getDomManager(project).getDomElement(parentTag) as? Bean
            ?: return emptyList()

        if (psi != dom.clazz.xmlAttributeValue) return emptyList()

        val alternativeDoms = findAlternativeDoms(dom, project)

        if (alternativeDoms.isNotEmpty()) {
            val marker = NavigationGutterIconBuilder
                .create(icon) { _: Any? -> alternativeDoms }
                .setTargets(dom)
                .setPopupTitle(message("hybris.editor.gutter.bs.beans.bean.alternativeDeclarations.popup.title"))
                .setTooltipText(message("hybris.editor.gutter.bs.beans.bean.alternativeDeclarations.tooltip.text"))
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .createLineMarkerInfo(leaf)

            return listOf(marker)
        }
        return emptyList()
    }

    private fun findAlternativeDoms(sourceDom: Bean, project: Project) = BSMetaModelAccess.getInstance(project).findMetasForDom(sourceDom)
        .flatMap { it.retrieveAllDoms() }
        .filter { dom -> dom != sourceDom }
        .map { it.clazz }
        .sortedBy { it.module?.name }
        .mapNotNull { it.xmlAttributeValue }

}