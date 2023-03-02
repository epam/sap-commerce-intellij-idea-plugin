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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor.getDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.idea.plugin.hybris.system.bean.BeansUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement

class BSTypeLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is PsiClass) return
        val name = element.qualifiedName ?: return
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return
        if (getDescriptorType(module) != HybrisModuleDescriptorType.PLATFORM) return
        if (!BeansUtils.isGeneratedFile(element)) return

        val elements = BSMetaModelAccess.getInstance(element.project).findMetasByName(name)
            .flatMap { it.declarations }
            .mapNotNull { it.retrieveDom() }
            .mapNotNull { it.xmlElement }

        if (elements.isEmpty()) return

        result.add(createTargetsWithGutterIcon(element, elements))
    }

    private fun createTargetsWithGutterIcon(
        psiClass: PsiClass,
        list: Collection<PsiElement>
    ) = NavigationGutterIconBuilder.create(HybrisIcons.BEAN)
        .setTargets(list)
        .setEmptyPopupText(message("hybris.editor.gutter.navigate.no.matching.beans"))
        .setPopupTitle(message("hybris.editor.gutter.bean.type.navigate.choose.class.title"))
        .setTooltipText(message("hybris.editor.gutter.item.class.tooltip.navigate.declaration"))
        .setAlignment(GutterIconRenderer.Alignment.LEFT)
        .createLineMarkerInfo(psiClass.nameIdentifier!!)

}