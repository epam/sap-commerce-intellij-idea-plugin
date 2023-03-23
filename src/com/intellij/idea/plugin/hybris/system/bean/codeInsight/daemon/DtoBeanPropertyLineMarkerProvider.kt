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

package com.intellij.idea.plugin.hybris.system.bean.codeInsight.daemon

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.codeInsight.daemon.AbstractHybrisClassLineMarkerProvider
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.BeansUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import javax.swing.Icon

class DtoBeanPropertyLineMarkerProvider : AbstractHybrisClassLineMarkerProvider<PsiField>() {

    override fun getName() = message("hybris.editor.gutter.bs.dto.bean.property.name")
    override fun getIcon(): Icon = HybrisIcons.BS_PROPERTY
    override fun canProcess(psi: PsiClass) = !BeansUtils.isEnumFile(psi)
    override fun tryCast(psi: PsiElement) = psi as? PsiField

    override fun collectDeclarations(psi: PsiField): Collection<RelatedItemLineMarkerInfo<PsiElement>> {
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
            .setTooltipText(message("hybris.editor.gutter.bs.dto.bean.property.tooltip.text"))
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .createLineMarkerInfo(psi.nameIdentifier)
        )
    }
}