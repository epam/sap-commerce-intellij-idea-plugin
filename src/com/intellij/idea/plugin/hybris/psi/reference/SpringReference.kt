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

package com.intellij.idea.plugin.hybris.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.spring.SpringManager
import com.intellij.spring.model.utils.SpringModelSearchers
import org.apache.commons.lang3.StringUtils

class SpringReference(element: PsiElement) : PsiReferenceBase<PsiElement>(element), PsiPolyVariantReference, HighlightedReference {

    override fun getRangeInElement() = TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = myElement.project
        val name = myElement.text.replace("[\"\']".toRegex(), StringUtils.EMPTY)

        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return ResolveResult.EMPTY_ARRAY

        return SpringManager.getInstance(project).getAllModels(module)
            .mapNotNull { SpringModelSearchers.findBean(it, name) }
            .map { it.beanClass }
            .toCollection(mutableSetOf())
            .let { PsiElementResolveResult.createResults(it) }
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<PsiReference> = EMPTY_ARRAY
}