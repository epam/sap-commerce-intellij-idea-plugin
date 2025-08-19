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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.childrenOfType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem

class ModelItemAttributeFieldLineMarkerProvider : ModelAttributeLineMarkerProvider<PsiField>() {

    override fun getName() = i18n("hybris.editor.gutter.ts.model.item.attribute.field.name")
    override fun tryCast(psi: PsiElement) = (psi as? PsiField)
        ?.takeIf { it.name != HybrisConstants.TYPECODE_FIELD_NAME }
        ?.takeIf { (it.type as? PsiClassReferenceType)?.name == String::class.java.simpleName }

    override fun collect(meta: TSGlobalMetaItem, psi: PsiField) = psi.childrenOfType<PsiLiteralExpression>()
        .firstNotNullOfOrNull {
            val name = it.value.toString()
            val nameIdentifier = psi.nameIdentifier

            getPsiElementItemLineMarkerInfo(meta, name, nameIdentifier)
                ?: getPsiElementRelationLineMarkerInfo(name, nameIdentifier)
                ?: getPsiElementOrderingAttributeRelationLineMarkerInfo(meta, name, nameIdentifier)
        }
        ?.let { listOf(it) }
        ?: emptyList()

    private fun getPsiElementOrderingAttributeRelationLineMarkerInfo(
        meta: TSGlobalMetaItem,
        name: String,
        nameIdentifier: PsiIdentifier
    ) = meta.allOrderingAttributes[name]
        ?.retrieveDom()
        ?.qualifier
        ?.xmlAttributeValue
        ?.let {
            NavigationGutterIconBuilder
                .create(HybrisIcons.TypeSystem.ORDERING_ATTRIBUTE)
                .setTargets(it)
                .setTooltipText(i18n("hybris.editor.gutter.ts.model.item.orderingAttribute.tooltip.text"))
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(nameIdentifier)
        }

    private fun getPsiElementRelationLineMarkerInfo(
        name: String,
        nameIdentifier: PsiIdentifier
    ) = TSMetaModelAccess.getInstance(nameIdentifier.project).findMetaRelationByName(name)
        ?.retrieveDom()
        ?.code
        ?.xmlAttributeValue
        ?.let {
            NavigationGutterIconBuilder
                .create(HybrisIcons.TypeSystem.Types.RELATION)
                .setTargets(it)
                .setTooltipText(i18n("hybris.editor.gutter.ts.model.item.attribute.field.relation.tooltip.text"))
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(nameIdentifier)
        }

}