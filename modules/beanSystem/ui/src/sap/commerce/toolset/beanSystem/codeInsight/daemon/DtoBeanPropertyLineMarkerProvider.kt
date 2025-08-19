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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.isBeanFile
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.codeInsight.daemon.HybrisClassLineMarkerProvider
import javax.swing.Icon

class DtoBeanPropertyLineMarkerProvider : HybrisClassLineMarkerProvider<PsiField>() {

    override fun getName() = i18n("hybris.editor.gutter.bs.dto.bean.property.name")
    override fun getIcon(): Icon = HybrisIcons.BeanSystem.PROPERTY
    override fun canProcess(psi: PsiClass) = isBeanFile(psi)
    override fun tryCast(psi: PsiElement) = psi as? PsiField

    override fun collectDeclarations(psi: PsiField): Collection<LineMarkerInfo<PsiElement>> {
        val metas = BSMetaModelAccess.getInstance(psi.project).findMetaBeansByName(psi.containingClass?.qualifiedName)

        if (metas.isEmpty() || metas.size > 1) return emptyList()

        val xmlElement = metas.first().properties[psi.name]
            ?.retrieveDom()
            ?.name
            ?.xmlElement
            ?: return emptyList()

        return listOf(NavigationGutterIconBuilder
            .create(icon)
            .setTarget(xmlElement)
            .setTooltipText(i18n("hybris.editor.gutter.bs.dto.bean.property.tooltip.text"))
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .createLineMarkerInfo(psi.nameIdentifier)
        )
    }
}