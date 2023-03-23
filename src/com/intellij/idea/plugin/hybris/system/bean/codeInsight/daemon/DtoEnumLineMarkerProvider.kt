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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.codeInsight.daemon.AbstractHybrisClassLineMarkerProvider
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.utils.ModelsUtils
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import javax.swing.Icon

class DtoEnumLineMarkerProvider : AbstractHybrisClassLineMarkerProvider<PsiClass>() {

    override fun getName() = message("hybris.editor.gutter.bs.dto.enum.name")
    override fun getIcon(): Icon = HybrisIcons.BS_ENUM
    override fun canProcess(psi: PsiClass) = ModelsUtils.isEnumFile(psi)
    override fun tryCast(psi: PsiElement) = (psi as? PsiClass)
        ?.takeIf { it.qualifiedName != null }
        ?.takeIf { it.nameIdentifier != null }

    override fun collectDeclarations(psi: PsiClass) = BSMetaModelAccess.getInstance(psi.project).findMetaEnumByName(psi.qualifiedName!!)
        ?.retrieveAllDoms()
        ?.mapNotNull { it.clazz.xmlAttributeValue }
        ?.takeIf { it.isNotEmpty() }
        ?.let {
            NavigationGutterIconBuilder
                .create(icon)
                .setTargets(it)
                .setPopupTitle(message("hybris.editor.gutter.bs.dto.enum.popup.title"))
                .setTooltipText(message("hybris.editor.gutter.bs.dto.enum.tooltip.text"))
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .createLineMarkerInfo(psi.nameIdentifier!!)
        }
        ?.let { listOf(it) }
        ?: emptyList()

}