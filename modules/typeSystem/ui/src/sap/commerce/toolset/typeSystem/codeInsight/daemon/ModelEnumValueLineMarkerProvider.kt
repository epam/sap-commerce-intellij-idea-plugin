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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.codeInsight.daemon.HybrisClassLineMarkerProvider
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.util.TSUtils
import javax.swing.Icon

class ModelEnumValueLineMarkerProvider : HybrisClassLineMarkerProvider<PsiField>() {

    override fun getName() = i18n("hybris.editor.gutter.ts.model.enum.value.name")
    override fun getIcon(): Icon = HybrisIcons.TypeSystem.ENUM_VALUE
    override fun canProcess(psi: PsiClass) = TSUtils.isEnumFile(psi)
    override fun tryCast(psi: PsiElement) = psi as? PsiField

    override fun collectDeclarations(psi: PsiField) = TSMetaModelAccess.getInstance(psi.project).findMetaEnumByName(psi.containingClass!!.name)
        ?.values
        ?.get(psi.name)
        ?.retrieveDom()
        ?.xmlElement
        ?.let {
            NavigationGutterIconBuilder
                .create(icon)
                .setTargets(it)
                .setTooltipText(i18n("hybris.editor.gutter.ts.model.enum.value.tooltip.text"))
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(psi.nameIdentifier)
        }
        ?.let { listOf(it) }
        ?: emptyList()

}