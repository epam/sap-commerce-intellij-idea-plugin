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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexDocumentIdDec
import com.intellij.idea.plugin.hybris.impex.psi.util.setName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException

class ImpexDocumentIdReference(psiElement: PsiElement) : PsiReferenceBase.Poly<PsiElement>(psiElement, false) {

    override fun getVariants(): Array<PsiReference> = PsiReference.EMPTY_ARRAY

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = PsiTreeUtil
        .collectElementsOfType(element.containingFile, ImpexDocumentIdDec::class.java)
        .filter { element.textMatches(it.text) }
        .takeIf { it.isNotEmpty() }
        ?.let { PsiElementResolveResult.createResults(it) }
        ?: ResolveResult.EMPTY_ARRAY

    override fun getRangeInElement() = TextRange.from(0, element.textLength)

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String) = getManipulator()
        .handleContentChange(myElement, rangeInElement, newElementName)

    private fun getManipulator() = object : AbstractElementManipulator<PsiElement>() {
        override fun handleContentChange(element: PsiElement, range: TextRange, newContent: String) = setName(element, newContent)
    }
}
