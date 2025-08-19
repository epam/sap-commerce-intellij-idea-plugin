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

package sap.commerce.toolset.spring.codeInsight.daemon

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.codeInsight.daemon.AbstractItemsXmlLineMarkerProvider
import sap.commerce.toolset.typeSystem.model.ItemType
import javax.swing.Icon

class ItemsXmlInterceptorLineMarkerProvider : AbstractItemsXmlLineMarkerProvider<XmlAttributeValue>() {

    override fun canProcess(elements: MutableList<out PsiElement>): Boolean = super.canProcess(elements)
        && Plugin.SPRING.isActive()

    override fun getName() = i18n("hybris.editor.gutter.ts.items.item.interceptors.name")
    override fun getIcon(): Icon = HybrisIcons.TypeSystem.INTERCEPTOR

    override fun tryCast(psi: PsiElement) = (psi as? XmlAttributeValue)
        ?.takeIf {
            val attribute = psi.parent as? XmlAttribute ?: return@takeIf false
            return@takeIf attribute.name == ItemType.CODE
                && DomManager.getDomManager(psi.project).getDomElement(attribute.parent) is ItemType
        }

    override fun collectDeclarations(psi: XmlAttributeValue) = psi.childrenOfType<XmlToken>()
        .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
        ?.let { leaf ->
            TSInterceptorSpringBuilderFactory.createGutterBuilder(psi.project, psi.value)
                ?.createSpringGroupLineMarkerInfo(leaf)
                ?.let { listOf(it) }
        }
        ?: emptyList()

}