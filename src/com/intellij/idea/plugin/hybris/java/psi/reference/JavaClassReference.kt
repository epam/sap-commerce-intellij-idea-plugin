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

package com.intellij.idea.plugin.hybris.java.psi.reference

import com.intellij.codeInsight.completion.JavaLookupElementBuilder
import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.java.psi.JavaPsiHelper
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope

class JavaClassReference : PsiReferenceBase<PsiElement>, HighlightedReference {

    private val className: String

    constructor(element: PsiElement, className: String) : super(element) {
        this.className = className
    }

    constructor(element: PsiElement, textRange: TextRange, className: String) : super(element, textRange) {
        this.className = className
    }

    override fun calculateDefaultRangeInElement(): TextRange =
        if (element.textLength == 0) super.calculateDefaultRangeInElement()
        else TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun getVariants() = JavaPsiFacade.getInstance(element.project)
        .findClass(className, GlobalSearchScope.allScope(element.project))
        ?.let { psiClass ->
            val fields = psiClass.fields

            return@let if (psiClass.isRecord) fields.toList()
            else fields
                .filter { JavaPsiHelper.hasGetter(psiClass, it) && JavaPsiHelper.hasSetter(psiClass, it) }
        }
        ?.map { JavaLookupElementBuilder.forField(it) }
        ?.toTypedArray()
        ?: emptyArray()

    override fun resolve(): PsiElement? {
        val project = element.project
        return JavaPsiFacade.getInstance(project)
            .findClass(className, GlobalSearchScope.allScope(project))
            ?.let { psiClass ->
                val field = psiClass.findFieldByName(value, false)
                return@let if (psiClass.isRecord) field
                else field
                    ?.takeIf { JavaPsiHelper.hasGetter(psiClass, it) && JavaPsiHelper.hasSetter(psiClass, it) }
            }
    }

}