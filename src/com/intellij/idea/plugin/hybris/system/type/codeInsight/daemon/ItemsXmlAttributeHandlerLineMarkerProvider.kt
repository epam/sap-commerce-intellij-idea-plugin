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
package com.intellij.idea.plugin.hybris.system.type.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.model.Persistence
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.spring.SpringManager
import com.intellij.spring.model.utils.SpringModelSearchers
import com.intellij.util.xml.DomManager
import javax.swing.Icon

class ItemsXmlAttributeHandlerLineMarkerProvider : AbstractItemsXmlLineMarkerProvider<XmlAttributeValue>() {

    override fun getName() = message("hybris.editor.gutter.ts.items.item.attributeHandler.name")
    override fun getIcon(): Icon = HybrisIcons.SPRING_BEAN
    override fun tryCast(psi: PsiElement) = (psi as? XmlAttributeValue)
        ?.takeIf {
            val attribute = psi.parent as? XmlAttribute ?: return@takeIf false
            return@takeIf attribute.name == Persistence.ATTRIBUTE_HANDLER
                && DomManager.getDomManager(psi.project).getDomElement(attribute.parent) is Persistence
        }

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<LineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN } ?: return emptyList()
        val module = ModuleUtilCore.findModuleForPsiElement(psi) ?: return emptyList()

        val springBeans = SpringManager.getInstance(psi.project).getAllModels(module)
            .mapNotNull { SpringModelSearchers.findBean(it, psi.value) }
            .map { it.springBean }
            .map { it.xmlTag }

        if (springBeans.isEmpty()) return emptyList()

        val marker = NavigationGutterIconBuilder
            .create(icon)
            .setTargets(springBeans)
            .setTooltipText(message("hybris.editor.gutter.ts.items.item.attributeHandler.tooltip.text"))
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .createLineMarkerInfo(leaf)
        return listOf(marker)
    }

}