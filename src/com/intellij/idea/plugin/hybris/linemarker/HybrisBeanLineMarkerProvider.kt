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
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor.getDescriptorType
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptorType
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope.getScopeRestrictedByFileTypes
import com.intellij.psi.search.GlobalSearchScope.moduleWithDependentsScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class HybrisBeanLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(element: PsiElement,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element is PsiClass) {
            val project = element.project

            val searchHelper = PsiSearchHelper.getInstance(project)
            val module = ModuleUtil.findModuleForPsiElement(element) ?: return

            if (getDescriptorType(module) != HybrisModuleDescriptorType.PLATFORM) {
                return
            }
            val foundEls = mutableListOf<PsiElement>()

            val searchScope = getScopeRestrictedByFileTypes(moduleWithDependentsScope(module), XmlFileType.INSTANCE)
            val qualifiedName = element.qualifiedName
            if (qualifiedName != null) {
                searchHelper.processElementsWithWord({ el, _ ->
                    if (el.containingFile.name.contains("-beans") && el is XmlAttributeValue
                            && (el.parent as XmlAttribute).name == "class") {
                        foundEls.add(el)
                    }
                    true
                }, searchScope, qualifiedName, UsageSearchContext.ANY, true)
            }

            if (foundEls.isNotEmpty()) {
                createTargetsWithGutterIcon(result, element, foundEls)
            }
        }
    }

    private fun createTargetsWithGutterIcon(
            result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>,
            psiClass: PsiClass,
            list: Collection<PsiElement>
    ) {
        val builder = NavigationGutterIconBuilder.create(HybrisIcons.BEAN).setTargets(list)

        builder.setEmptyPopupText(HybrisI18NBundleUtils.message(
                "hybris.gutter.navigate.no.matching.beans"
        ))

        builder.setPopupTitle(HybrisI18NBundleUtils.message(
                "hybris.gutter.bean.class.navigate.choose.class.title"
        ))
        builder.setTooltipText(HybrisI18NBundleUtils.message(
                "hybris.gutter.item.class.tooltip.navigate.declaration"
        ))
        result.add(builder.createLineMarkerInfo(psiClass.nameIdentifier!!))
    }
}