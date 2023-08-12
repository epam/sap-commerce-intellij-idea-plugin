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

package com.intellij.idea.plugin.hybris.psi.util

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

object PsiTreeUtilExt {

    fun getPrevSiblingOfElementType(sibling: PsiElement?, elementType: IElementType): PsiElement? = sibling
        ?.let {
            var prevSibling = sibling.prevSibling
            while (prevSibling != null) {
                if (prevSibling.elementType == elementType) {
                    return@let prevSibling
                }
                prevSibling = prevSibling.prevSibling
            }
            null
        }

    fun getLeafsOfAnyElementType(element: PsiElement, vararg elementTypes: IElementType): List<PsiElement> = PsiTreeUtil
        .collectElementsOfType(element, LeafPsiElement::class.java)
        .filter { elementTypes.contains(it.elementType) }

    fun getLeafsOfElementType(
        element: PsiElement,
        elementType: IElementType
    ) = getLeafsOfAnyElementType(element, elementType)

    fun <T : PsiElement?> getTopmostParentOfTypeUntil(element: PsiElement, aClass: Class<T>, untilClass: Class<*>): T? {
        var answer = PsiTreeUtil.getParentOfType(element, aClass)


        while (true) {
            val next = PsiTreeUtil.getParentOfType(answer, aClass)
                ?.takeUnless { untilClass.isInstance(it) }
                ?: return answer
            answer = next
        }
    }
}