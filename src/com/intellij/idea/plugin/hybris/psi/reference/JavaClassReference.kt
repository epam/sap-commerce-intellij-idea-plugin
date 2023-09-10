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

package com.intellij.idea.plugin.hybris.psi.reference

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope

class JavaClassReference(element: PsiElement, private val className: String) : PsiReferenceBase<PsiElement>(element), HighlightedReference {

    override fun calculateDefaultRangeInElement(): TextRange =
        if (element.textLength == 0) super.calculateDefaultRangeInElement()
        else TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)

    override fun resolve(): PsiElement? {
        val project = element.project
        return JavaPsiFacade.getInstance(project)
            .findClass(className, GlobalSearchScope.allScope(project))
            ?.let { psiClass ->
                val field = psiClass.findFieldByName(value, false)
                return@let if (psiClass.isRecord) field
                else field
                    ?.takeIf { hasGetter(psiClass, it) && hasSetter(psiClass, it) }
            }
    }

    private fun hasGetter(psiClass: PsiClass, psiField: PsiField): Boolean {
        val name = getFieldName(psiField)
        val getterMethod = psiClass.findMethodsByName("get$name", false)
            .any { it.hasModifierProperty(PsiModifier.PUBLIC) && it.returnType == psiField.type }

        if (getterMethod) return true

        return psiClass.findMethodsByName("is$name", false)
            .any { it.hasModifierProperty(PsiModifier.PUBLIC) && it.returnType == psiField.type }
    }

    private fun hasSetter(psiClass: PsiClass, psiField: PsiField): Boolean {
        val name = getFieldName(psiField)
        return psiClass.findMethodsByName("set$name", false)
            .any {
                it.hasModifierProperty(PsiModifier.PUBLIC)
                    && it.parameterList.parameters.size == 1
                    && it.parameterList.parameters[0].type == psiField.type
            }
    }

    private fun getFieldName(psiField: PsiField) = psiField.name.replaceFirstChar { it.uppercase() }
}