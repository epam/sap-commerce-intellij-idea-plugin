/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.type.spring

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.spring.SpringManager
import com.intellij.spring.model.utils.SpringModelSearchers

object TSSpringHelper {

    fun resolveBeanDeclaration(element: PsiElement, beanId: String): XmlTag? {
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return null

        return if (PluginCommon.isPluginActive(PluginCommon.SPRING_PLUGIN_ID)) springResolveBean(module, beanId)
            ?.springBean
            ?.xmlTag
        else plainResolveBean(module, beanId)
    }

    fun resolveBeanClass(element: PsiElement, beanId: String): PsiClass? {
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return null
        val project = module.project

        return if (PluginCommon.isPluginActive(PluginCommon.SPRING_PLUGIN_ID)) springResolveBean(module, beanId)
            ?.beanClass
        else plainResolveBean(module, beanId)
            ?.getAttributeValue("class")
            ?.let { JavaPsiFacade.getInstance(project).findClass(it, GlobalSearchScope.allScope(project)) }
    }

    private fun springResolveBean(module: Module, beanId: String) = SpringManager.getInstance(module.project).getAllModels(module)
        .firstNotNullOfOrNull { SpringModelSearchers.findBean(it, beanId) }

    private fun plainResolveBean(module: Module, beanId: String): XmlTag? {
        val foundEls = mutableListOf<PsiElement>()

        val project = module.project
        PsiSearchHelper.getInstance(project).processElementsWithWord(
            { el, _ ->
                if (el.containingFile.name.contains("-spring") && el is XmlAttribute && el.name == "id") foundEls.add(el)
                true
            },
            GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.moduleScope(module), XmlFileType.INSTANCE),
            beanId,
            UsageSearchContext.ANY,
            true
        )

        return foundEls.firstOrNull()
            ?.let { it as? XmlAttribute }
            ?.parent
    }

}