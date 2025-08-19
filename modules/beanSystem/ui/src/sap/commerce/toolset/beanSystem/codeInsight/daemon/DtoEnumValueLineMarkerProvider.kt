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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiEnumConstant
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.isEnumFile
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.codeInsight.daemon.HybrisClassLineMarkerProvider
import javax.swing.Icon

class DtoEnumValueLineMarkerProvider : HybrisClassLineMarkerProvider<PsiEnumConstant>() {

    override fun getName() = i18n("hybris.editor.gutter.bs.dto.enum.value.name")
    override fun getIcon(): Icon = HybrisIcons.BeanSystem.ENUM_VALUE
    override fun canProcess(psi: PsiClass) = isEnumFile(psi)
        && psi.qualifiedName != null

    override fun tryCast(psi: PsiElement) = psi as? PsiEnumConstant

    override fun collectDeclarations(psi: PsiEnumConstant) = BSMetaModelAccess.getInstance(psi.project).findMetaEnumByName(psi.containingClass?.qualifiedName)
        ?.values
        ?.get(psi.name)
        ?.retrieveDom()
        ?.xmlElement
        ?.let {
            NavigationGutterIconBuilder
                .create(icon)
                .setTarget(it)
                .setTooltipText(i18n("hybris.editor.gutter.bs.dto.enum.value.tooltip.text"))
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(psi.nameIdentifier)
        }
        ?.let { listOf(it) }
        ?: emptyList()
}