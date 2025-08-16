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
package sap.commerce.toolset.system.type.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import sap.commerce.toolset.spring.SpringHelper
import sap.commerce.toolset.system.type.model.Attribute
import sap.commerce.toolset.system.type.model.Persistence
import sap.commerce.toolset.system.type.model.PersistenceType
import sap.commerce.toolset.system.type.psi.TSPsiHelper
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.xml.XmlTag
import sap.commerce.toolset.HybrisI18NBundleUtils.message
import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

class ItemsXmlAttributeHandlerLineMarkerProvider : AbstractItemsXmlLineMarkerProvider<XmlTag>() {

    override fun getName() = message("hybris.editor.gutter.ts.items.item.attributeHandler.name")
    override fun getIcon(): Icon = HybrisIcons.SPRING_BEAN
    override fun tryCast(psi: PsiElement) = (psi as? XmlTag)
        ?.takeIf { it.localName == Attribute.PERSISTENCE }
        ?.takeIf { it.getAttributeValue(Persistence.TYPE) == PersistenceType.DYNAMIC.value }

    override fun collectDeclarations(psi: XmlTag): Collection<LineMarkerInfo<PsiElement>> {
        val attributeHandlerId = TSPsiHelper.resolveAttributeHandlerId(psi) ?: return emptyList()
        val springBeanDeclaration = SpringHelper.resolveBeanDeclaration(psi, attributeHandlerId) ?: return emptyList()

        val marker = NavigationGutterIconBuilder
            .create(icon)
            .setTargets(springBeanDeclaration)
            .setTooltipText(message("hybris.editor.gutter.ts.items.item.attributeHandler.tooltip.text"))
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .createLineMarkerInfo(psi.firstLeaf())
        return listOf(marker)
    }

}