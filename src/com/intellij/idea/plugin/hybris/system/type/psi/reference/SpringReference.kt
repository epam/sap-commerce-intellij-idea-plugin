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

package com.intellij.idea.plugin.hybris.system.type.psi.reference

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.spring.SpringManager
import com.intellij.spring.contexts.model.SpringModel
import com.intellij.spring.model.utils.SpringModelSearchers

class SpringReference(
    element: PsiElement,
    val name: String,
    private val project: Project = element.project
) : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {

    override fun getRangeInElement() = TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return ResolveResult.EMPTY_ARRAY

        val springModels = SpringManager.getInstance(project).getAllModels(module)
        val pointer = findBean(springModels, name) ?: return ResolveResult.EMPTY_ARRAY

        pointer.beanClass ?: return ResolveResult.EMPTY_ARRAY

        return PsiElementResolveResult.createResults(pointer.beanClass)
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<PsiReference> = PsiReference.EMPTY_ARRAY

    private fun findBean(springModels: Set<SpringModel>, name: String) = springModels.firstNotNullOfOrNull { SpringModelSearchers.findBean(it, name) }
}