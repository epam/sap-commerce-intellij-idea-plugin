/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor.getDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.idea.plugin.hybris.system.bean.BeansUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiField

class HybrisBSPropertyLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is PsiField || element.containingClass == null) return
        val module = ModuleUtil.findModuleForPsiElement(element) ?: return
        val psiClass = element.containingClass!!
        if (getDescriptorType(module) != HybrisModuleDescriptorType.PLATFORM) return
        if (BeansUtils.isEnumFile(psiClass)) return
        if (psiClass.qualifiedName == null) return

        val metas = BSMetaModelAccess.getInstance(element.project).findMetaBeansByName(psiClass.qualifiedName!!)

        if (metas.isEmpty() || metas.size > 1) return

        val xmlElement = metas.first().properties[element.name]?.retrieveDom()?.xmlElement ?: return

        result.add(createTargetsWithGutterIcon(element, xmlElement))
    }

    private fun createTargetsWithGutterIcon(
        dom: PsiField,
        psi: PsiElement
    ) = NavigationGutterIconBuilder.create(HybrisIcons.PROPERTY)
        .setTarget(psi)
        .setTooltipText(message("hybris.gutter.bs.bean.property.title"))
        .createLineMarkerInfo(dom.nameIdentifier)

}