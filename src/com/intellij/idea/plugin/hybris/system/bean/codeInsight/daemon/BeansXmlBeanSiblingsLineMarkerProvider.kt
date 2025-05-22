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
package com.intellij.idea.plugin.hybris.system.bean.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.xml.DomManager
import javax.swing.Icon

class BeansXmlBeanSiblingsLineMarkerProvider : AbstractBeansXmlLineMarkerProvider<XmlAttributeValue>() {

    override fun getName() = message("hybris.editor.gutter.bs.beans.bean.siblings.name")
    override fun getIcon(): Icon = HybrisIcons.BeanSystem.SIBLING
    override fun tryCast(psi: PsiElement) = psi as? XmlAttributeValue

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<LineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
            ?: return emptyList()

        val parentTag = PsiTreeUtil.getParentOfType(psi, XmlTag::class.java)
            ?: return emptyList()

        val project = parentTag.project
        val dom = DomManager.getDomManager(project).getDomElement(parentTag) as? Bean
            ?: return emptyList()

        if (psi != dom.clazz.xmlAttributeValue) return emptyList()

        val wsBeans = BSMetaModelAccess.getInstance(project).getAll<BSGlobalMetaBean>(BSMetaType.META_WS_BEAN)
        val beans = BSMetaModelAccess.getInstance(project).getAll<BSGlobalMetaBean>(BSMetaType.META_BEAN)
        val events = BSMetaModelAccess.getInstance(project).getAll<BSGlobalMetaBean>(BSMetaType.META_EVENT)

        return (wsBeans + beans + events)
            // TODO: improve, it is slow
            .mapNotNull { it.retrieveDom() }
            // TODO: improve, it is slow
            .filter { it.extends.stringValue == psi.value }
            .mapNotNull { it.clazz.xmlAttributeValue }
            .takeIf { it.isNotEmpty() }
            ?.let {
                NavigationGutterIconBuilder
                    .create(icon)
                    .setTargets(it)
                    .setPopupTitle(message("hybris.editor.gutter.bs.beans.bean.siblings.popup.title"))
                    .setTooltipText(message("hybris.editor.gutter.bs.beans.bean.siblings.tooltip.text"))
                    .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                    .createLineMarkerInfo(leaf)
            }
            ?.let { listOf(it) }
            ?: emptyList()
    }
}