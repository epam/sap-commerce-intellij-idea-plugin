/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.spring

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.spring.SpringManager
import com.intellij.spring.model.SpringBeanPointer
import com.intellij.spring.model.utils.SpringModelSearchers
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.yExtensionDescriptor

class AdvancedSpringService : SpringService {

    override fun resolveBeanDeclaration(element: PsiElement, beanId: String, fallback: SpringFallbackScope) = Plugin.SPRING.ifActive {
        guessModule(element)
            ?.let { springResolveBean(it, beanId) }
            ?.springBean
            ?.xmlTag
            ?: fallback(element, beanId, fallback)
                ?.springBean
                ?.xmlTag
    }

    override fun resolveBeanClass(element: PsiElement, beanId: String, fallback: SpringFallbackScope) = Plugin.SPRING.ifActive {
        guessModule(element)
            ?.let { springResolveBean(it, beanId) }
            ?.beanClass
            ?: fallback(element, beanId, fallback)
                ?.beanClass
    }

    private fun guessModule(element: PsiElement) = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        CachedValueProvider.Result.createSingleDependency(
            ModuleUtilCore.findModuleForPsiElement(element),
            element,
        )
    }

    private fun springResolveBean(module: Module, beanId: String) = SpringManager.getInstance(module.project)
        .getAllModels(module)
        .firstNotNullOfOrNull { SpringModelSearchers.findBean(it, beanId) }

    private fun fallback(element: PsiElement, beanId: String, fallback: SpringFallbackScope): SpringBeanPointer<*>? {
        if (fallback == SpringFallbackScope.NONE) return null

        val springManager = SpringManager.getInstance(element.project)
        return ModuleManager.getInstance(element.project).sortedModules.reversed().asSequence()
            .filter {
                if (fallback == SpringFallbackScope.ALL_MODULES) true
                else it.yExtensionDescriptor?.type == ModuleDescriptorType.CUSTOM
            }
            .flatMap { springManager.getAllModels(it).asSequence() }
            .firstNotNullOfOrNull { SpringModelSearchers.findBean(it, beanId) }
    }
}