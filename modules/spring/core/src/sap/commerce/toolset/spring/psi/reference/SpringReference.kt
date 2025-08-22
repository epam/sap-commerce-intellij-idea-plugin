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

package sap.commerce.toolset.spring.psi.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.spring.SpringHelper

class SpringReference(
    element: PsiElement,
    private val name: String
) : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {

    override fun calculateDefaultRangeInElement() = if (element.text.startsWith("\"") || element.text.startsWith("'"))
        TextRange.from(1, element.textLength - HybrisConstants.QUOTE_LENGTH)
    else
        TextRange.from(0, element.textLength)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = SpringHelper.resolveBeanClass(element, name)
        ?.let { PsiElementResolveResult.createResults(it) }
        ?: ResolveResult.EMPTY_ARRAY

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<PsiReference> = EMPTY_ARRAY

}