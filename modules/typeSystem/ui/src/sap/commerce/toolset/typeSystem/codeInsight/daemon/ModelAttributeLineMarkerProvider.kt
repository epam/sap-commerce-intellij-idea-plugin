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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.xml.XmlElement
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.codeInsight.daemon.HybrisClassLineMarkerProvider
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.meta.model.TSGlobalMetaItem
import sap.commerce.toolset.typeSystem.meta.model.TSMetaRelation.RelationEnd
import sap.commerce.toolset.typeSystem.util.TSUtils
import javax.swing.Icon

abstract class ModelAttributeLineMarkerProvider<T : PsiElement> : HybrisClassLineMarkerProvider<T>() {

    override fun getIcon(): Icon = HybrisIcons.TypeSystem.FILE
    override fun canProcess(psi: PsiClass) = TSUtils.isItemModelFile(psi)

    override fun collectDeclarations(psi: T) = TSMetaModelAccess.getInstance(psi.project).findMetaItemByName(TSUtils.cleanItemModelSearchName((psi.parent as PsiClass).name))
        ?.let { collect(it, psi) }
        ?: emptyList()

    protected abstract fun collect(meta: TSGlobalMetaItem, psi: T): Collection<LineMarkerInfo<PsiElement>>

    protected open fun getPsiElementItemLineMarkerInfo(
        meta: TSGlobalMetaItem, name: String, nameIdentifier: PsiIdentifier
    ) = with(getAttributeElements(meta, name)) {
        if (this == null) {
            val groupedRelElements = getRelations(meta, name)
            return@with getRelationMarkers(groupedRelElements, RelationEnd.SOURCE, HybrisIcons.TypeSystem.RELATION_SOURCE, nameIdentifier)
                ?: getRelationMarkers(groupedRelElements, RelationEnd.TARGET, HybrisIcons.TypeSystem.RELATION_TARGET, nameIdentifier)
        } else {
            return@with createTargetsWithGutterIcon(
                nameIdentifier,
                this,
                HybrisIcons.TypeSystem.ATTRIBUTE,
                i18n("hybris.editor.gutter.ts.model.item.attribute.popup.title"),
                i18n("hybris.editor.gutter.ts.model.item.attribute.tooltip.text")
            )
        }
    }

    open fun getAttributeElements(meta: TSGlobalMetaItem, name: String) = meta.allAttributes[name]
        ?.declarations
        ?.mapNotNull { it.retrieveDom() }
        ?.map { it.qualifier }
        ?.mapNotNull { it.xmlAttributeValue }

    open fun getRelations(meta: TSGlobalMetaItem, name: String) = meta.allRelationEnds
        .filter { it.qualifier == name }
        .filter { it.isNavigable }
        .filter { it.retrieveDom()?.qualifier?.xmlAttributeValue != null }
        .groupBy({ it.end }, { it.retrieveDom()!!.qualifier.xmlAttributeValue!! })

    open fun getRelationMarkers(
        groupedRelElements: Map<RelationEnd, List<XmlElement>>,
        target: RelationEnd,
        icon: Icon,
        nameIdentifier: PsiIdentifier
    ) = groupedRelElements[target]
        ?.let {
            createTargetsWithGutterIcon(
                nameIdentifier,
                it,
                icon,
                i18n("hybris.editor.gutter.ts.model.item.relationEnd.popup.title"),
                i18n("hybris.editor.gutter.ts.model.item.relationEnd.tooltip.text"),
            )
        }

    private fun createTargetsWithGutterIcon(
        nameIdentifier: PsiIdentifier,
        targets: List<XmlElement>,
        icon: Icon,
        popupTitle: String,
        tooltipText: String,
    ) = NavigationGutterIconBuilder
        .create(icon)
        .setTargets(targets)
        .setPopupTitle(popupTitle)
        .setTooltipText(tooltipText)
        .setAlignment(GutterIconRenderer.Alignment.LEFT)
        .createLineMarkerInfo(nameIdentifier)

}