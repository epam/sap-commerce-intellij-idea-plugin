/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.impex.psi.references

import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.*
import com.intellij.util.IncorrectOperationException

class ImpExJavaEnumValueReference(psiElement: PsiElement, private val className: String) : PsiReferenceBase.Poly<PsiElement>(psiElement, false) {

    override fun getVariants(): Array<PsiReference> = PsiReference.EMPTY_ARRAY

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this to className)
        .let { PsiUtils.getValidResults(it) }

    override fun getRangeInElement() = TextRange.from(0, element.textLength)

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String) = getManipulator().handleContentChange(myElement, rangeInElement, newElementName)

    private fun getManipulator(): ElementManipulator<PsiElement> = object : AbstractElementManipulator<PsiElement>() {
        override fun handleContentChange(element: PsiElement, range: TextRange, newContent: String) = null
    }

    companion object {
        val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, Pair<ImpExJavaEnumValueReference, String>>>("HYBRIS_IMPEXJAVAENUMVALUEREFERENCE")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, Pair<ImpExJavaEnumValueReference, String>> { param ->
            val ref = param.first
            val className = param.second
            val element = ref.element
            val value = ref.value
            val project = element.project

            val result = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project))
                ?.takeIf { it.isEnum }
                ?.fields
                ?.filterIsInstance<PsiEnumConstant>()
                ?.firstOrNull { it.name.equals(value, true) }
                ?.let { PsiElementResolveResult.createResults(it.originalElement) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }
}