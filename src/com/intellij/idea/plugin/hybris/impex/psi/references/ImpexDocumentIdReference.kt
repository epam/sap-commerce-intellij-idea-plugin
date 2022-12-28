/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.util.setName
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexDocumentIdReference(psiElement: PsiElement) : PsiReferenceBase.Poly<PsiElement>(psiElement, false) {

    override fun getVariants(): Array<PsiReference> = PsiReference.EMPTY_ARRAY

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val originalFile = element.containingFile

        val documentRefs = PsiTreeUtil.collectElements(
                originalFile,
                { psiElement -> psiElement.node.elementType == ImpexTypes.DOCUMENT_ID }
        )

        if (!documentRefs.isEmpty()) {
            val references = ArrayList<PsiElement>()
            for (docID in documentRefs) {
                if (element.firstChild != docID && element.textMatches(docID.text)) {
                    references.add(docID)
                }
            }
            return PsiElementResolveResult.createResults(references)
        }
        return ResolveResult.EMPTY_ARRAY
    }


    override fun getRangeInElement(): TextRange {
        return TextRange.from(0, element.textLength)
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement? {
        return getManipulator().handleContentChange(myElement, rangeInElement, newElementName)
    }

    private fun getManipulator(): ElementManipulator<PsiElement> {
        return object : AbstractElementManipulator<PsiElement>() {
            override fun handleContentChange(element: PsiElement, range: TextRange, newContent: String) = setName(element, newContent)
        }
    }
}
