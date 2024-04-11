/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

open class JavaClassReference : PsiReferenceBase<PsiElement>, HighlightedReference {

    protected val className: String

    constructor(element: PsiElement, className: String) : super(element) {
        this.className = className
    }

    constructor(element: PsiElement, textRange: TextRange, className: String) : super(element, textRange) {
        this.className = className
    }

    protected open fun evalClassName() = className

    override fun calculateDefaultRangeInElement(): TextRange =
        if (element.textLength == 0) super.calculateDefaultRangeInElement()
        else TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun getVariants() = JavaPsiFacade.getInstance(element.project)
        .findClass(evalClassName(), GlobalSearchScope.allScope(element.project))
        ?.let { psiClass ->
            val fields = psiClass.allFields

            return@let if (psiClass.isRecord) fields.toList()
            else fields
                .filter {
                    val targetClass = it.containingClass ?: return@filter false
                    JavaPsiHelper.hasGetter(targetClass, it) && JavaPsiHelper.hasSetter(targetClass, it)
                }
        }
        ?.map {
            JavaLookupElementBuilder.forField(it, it.name, it.containingClass)
                .withTypeText(it.type.presentableText, true)
        }
        ?.toTypedArray()
        ?: emptyArray()

    override fun resolve(): PsiElement? {
        val project = element.project
        return JavaPsiFacade.getInstance(project)
            .findClass(evalClassName(), GlobalSearchScope.allScope(project))
            ?.let { psiClass ->
                val field = psiClass.findFieldByName(value, true)
                return@let if (psiClass.isRecord) field
                else field
                    ?.takeIf {
                        val targetClass = it.containingClass ?: return@takeIf false
                        JavaPsiHelper.hasGetter(targetClass, it) && JavaPsiHelper.hasSetter(targetClass, it)
                    }
            }
    }

}