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

import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.idea.plugin.hybris.impex.rename.manipulator.ImpexMacrosManipulator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil

class ImpexMacroReference(owner: PsiElement) : PsiReferenceBase.Poly<PsiElement?>(owner, false) {

    override fun calculateDefaultRangeInElement() = findMacroDeclaration()
        ?.let { TextRange.from(0, escapeName(it.macroNameDec.text).length) }
        ?: TextRange.from(0, element.textLength)

    override fun getVariants(): Array<ResolveResult> = ResolveResult.EMPTY_ARRAY

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = findMacroDeclaration()
        ?.let { PsiElementResolveResult.createResults(it.macroNameDec) }
        ?: ResolveResult.EMPTY_ARRAY

    private fun findMacroDeclaration() = PsiTreeUtil.findChildrenOfType(
        element.containingFile,
        ImpexMacroDeclaration::class.java
    )
        .reversed()
        .find { element.text.startsWith(escapeName(it.macroNameDec.text)) }

    override fun handleElementRename(newElementName: String) = ImpexMacrosManipulator().handleContentChange(element, rangeInElement, newElementName)

    companion object {
        fun escapeName(macroName: String) = macroName
            .replace("\\", "")
            .replace("\n", "")
    }
}
