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

package sap.commerce.toolset.spring

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.spring.SpringManager
import com.intellij.spring.model.SpringBeanPointer
import com.intellij.spring.model.utils.SpringModelSearchers
import sap.commerce.toolset.Plugin

class AdvancedSpringService : SpringService {

    override fun resolveBeanDeclaration(element: PsiElement, beanId: String) = Plugin.SPRING.ifActive {
        guessModule(element)
            ?.let { springResolveBean(it, beanId) }
            ?.springBean
            ?.xmlTag
            ?: fallbackToAllModules(element, beanId)
                ?.springBean
                ?.xmlTag
    }

    override fun resolveBeanClass(element: PsiElement, beanId: String) = Plugin.SPRING.ifActive {
        guessModule(element)
            ?.let { springResolveBean(it, beanId) }
            ?.beanClass
            ?: fallbackToAllModules(element, beanId)
                ?.beanClass
    }

    private fun guessModule(element: PsiElement) = ModuleUtilCore.findModuleForPsiElement(element)

    private fun springResolveBean(module: Module, beanId: String) = SpringManager.getInstance(module.project)
        .getAllModels(module)
        .firstNotNullOfOrNull { SpringModelSearchers.findBean(it, beanId) }

    private fun fallbackToAllModules(element: PsiElement, beanId: String): SpringBeanPointer<*>? {
        val springManager = SpringManager.getInstance(element.project)
        return ModuleManager.getInstance(element.project).sortedModules.reversed().asSequence()
            .flatMap { springManager.getAllModels(it).asSequence() }
            .firstNotNullOfOrNull { SpringModelSearchers.findBean(it, beanId) }
    }
}