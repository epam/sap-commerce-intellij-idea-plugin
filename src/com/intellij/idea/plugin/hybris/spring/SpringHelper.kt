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

package com.intellij.idea.plugin.hybris.spring

import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.idea.plugin.hybris.system.spring.SimpleSpringService
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.spring.SpringManager
import com.intellij.spring.java.SpringJavaClassInfo
import com.intellij.spring.model.SpringBeanPointer
import com.intellij.spring.model.utils.SpringModelSearchers
import com.intellij.spring.model.xml.beans.SpringBean

object SpringHelper {

    fun resolveBeanDeclaration(element: PsiElement, beanId: String): XmlTag? {
        return Plugin.SPRING.ifActive {
            ModuleUtilCore.findModuleForPsiElement(element)
                ?.let { springResolveBean(it, beanId) }
                ?.springBean
                ?.xmlTag
        } ?: plainResolveBean(element.project, beanId)
    }

    fun resolveBeanClass(element: PsiElement, beanId: String) = Plugin.SPRING.ifActive {
        ModuleUtilCore.findModuleForPsiElement(element)
            ?.let { springResolveBean(it, beanId) }
            ?.beanClass
    }
        ?: plainResolveBean(element.project, beanId)
            ?.getAttributeValue("class")
            ?.let {
                JavaPsiFacade.getInstance(element.project).findClass(it, GlobalSearchScope.allScope(element.project))
            }

    fun resolveInterceptorBeansLazy(
        clazz: PsiClass,
        name: String? = null
    ): NotNullLazyValue<MutableCollection<out SpringBeanPointer<*>>> = NotNullLazyValue.lazy {
        SpringJavaClassInfo.getSpringJavaClassInfo(clazz).resolve().mappedDomBeans
            .asSequence()
            .map { it.springBean }
            .filterIsInstance<SpringBean>()
            .filter { bean ->
                bean.properties.find { property ->
                    property.propertyName == "typeCode" && (name?.equals(property.valueAsString, true) ?: true)
                } != null
            }
            .mapNotNull { bean -> bean.properties.find { property -> property.propertyName == "interceptor" } }
            .mapNotNull { interceptor -> interceptor.refValue }
            .sortedWith(SpringBeanPointer.DISPLAY_COMPARATOR)
            .toMutableList()
    }

    private fun springResolveBean(module: Module, beanId: String) = SpringManager.getInstance(module.project).getAllModels(module)
        .firstNotNullOfOrNull { SpringModelSearchers.findBean(it, beanId) }

    private fun plainResolveBean(project: Project, beanId: String) = SimpleSpringService.getService(project)
        ?.findBean(beanId)

}