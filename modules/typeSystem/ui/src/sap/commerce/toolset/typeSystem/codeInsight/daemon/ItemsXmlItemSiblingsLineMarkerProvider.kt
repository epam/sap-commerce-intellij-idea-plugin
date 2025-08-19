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
package sap.commerce.toolset.typeSystem.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType
import sap.commerce.toolset.typeSystem.model.ItemType
import javax.swing.Icon

class ItemsXmlItemSiblingsLineMarkerProvider : AbstractItemsXmlLineMarkerProvider<XmlAttributeValue>() {

    override fun getName() = i18n("hybris.editor.gutter.ts.items.item.siblings.name")
    override fun getIcon(): Icon = HybrisIcons.TypeSystem.SIBLING
    override fun tryCast(psi: PsiElement) = (psi as? XmlAttributeValue)
        ?.takeIf {
            val attribute = psi.parent as? XmlAttribute ?: return@takeIf false
            return@takeIf attribute.name == ItemType.CODE
                && DomManager.getDomManager(psi.project).getDomElement(attribute.parent) is ItemType
        }

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<LineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
            ?: return emptyList()

        val meta = TSMetaModelAccess.getInstance(psi.project).findMetaItemByName(psi.value)
            ?: return emptyList()

        return TSMetaModelAccess.getInstance(psi.project).getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
            .filter { it.extendedMetaItemName == meta.name }
            .mapNotNull { it.retrieveDom() }
            .map { it.code }
            .mapNotNull { it.xmlAttributeValue }
            .takeIf { it.isNotEmpty() }
            ?.let { targets ->
                NavigationGutterIconBuilder
                    .create(icon)
                    .setTargets(targets)
                    .setPopupTitle(i18n("hybris.editor.gutter.ts.items.item.siblings.popup.title"))
                    .setTooltipText(i18n("hybris.editor.gutter.ts.items.item.siblings.tooltip.text", psi.value))
                    .setAlignment(GutterIconRenderer.Alignment.CENTER)
                    .createLineMarkerInfo(leaf)
            }
            ?.let { listOf(it) }
            ?: emptyList()
    }

}