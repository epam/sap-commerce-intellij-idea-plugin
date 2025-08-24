/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.impex.psi.references

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import sap.commerce.toolset.impex.lang.refactoring.ImpExPsiElementManipulator
import sap.commerce.toolset.impex.psi.ImpExFile
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.impex.psi.util.getLineNumber
import sap.commerce.toolset.psi.getValidResults

class ImpExMacroReference(owner: PsiElement) : PsiReferenceBase.Poly<PsiElement?>(owner, false) {

    override fun calculateDefaultRangeInElement() = findMacroDeclaration()
        ?.let { TextRange.from(0, escapeName(it.macroNameDec.text).length) }
        ?: TextRange.from(0, element.textLength)

    override fun getVariants(): Array<ResolveResult> = ResolveResult.EMPTY_ARRAY

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, CACHE_KEY, provider, false, this)
        .let { getValidResults(it) }

    override fun handleElementRename(newElementName: String) = ImpExPsiElementManipulator().handleContentChange(element, rangeInElement, newElementName)

    private fun findMacroDeclaration(): ImpExMacroDeclaration? {
        val text = element.text
        val macroUsageLineNumber = element.getLineNumber()

        return PsiTreeUtil.findChildrenOfType(
            element.containingFile,
            ImpExMacroDeclaration::class.java
        )
            .reversed()
            .find { it.getLineNumber() < macroUsageLineNumber && text.startsWith(escapeName(it.macroNameDec.text)) }
            ?: findExternalMacroDeclaration(text)
    }

    private fun findExternalMacroDeclaration(text: String) = (element.containingFile as ImpExFile).getExternalImpExFiles()
        .map { PsiTreeUtil.findChildrenOfType(it, ImpExMacroDeclaration::class.java) }
        .flatMap { it.reversed() }
        .find { text.startsWith(escapeName(it.macroNameDec.text)) }

    companion object {
        private val CACHE_KEY = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExMacroReference>>("SAP_CX_IMPEXMACRO_REFERENCE")

        fun escapeName(macroName: String) = macroName
            .replace("\\", "")
            .replace("\n", "")

        private val provider = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExMacroReference> { ref ->
            val result = ref.findMacroDeclaration()
                ?.let { PsiElementResolveResult.createResults(it.macroNameDec) }
                ?: ResolveResult.EMPTY_ARRAY

            CachedValueProvider.Result.create(
                result,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }
}
