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
package com.intellij.idea.plugin.hybris.impex.highlighting.findUsages

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase
import com.intellij.featureStatistics.ProductivityFeatureNames
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroNameDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroUsageDec
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.Consumer

class HighlightMacrosHandlerFactory : HighlightUsagesHandlerFactoryBase() {

    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile, target: PsiElement) =
        if (ImpexPsiUtils.isMacroNameDeclaration(target) || ImpexPsiUtils.isMacroUsage(target)) {
            ImpexMacroNameHighlightUsagesHandler(editor, file, target)
        } else null

    class ImpexMacroNameHighlightUsagesHandler(editor: Editor, file: PsiFile, private val myTarget: PsiElement) : HighlightUsagesHandlerBase<PsiElement>(editor, file) {

        override fun getFeatureId() = ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN
        override fun getTargets() = listOf(myTarget)
        override fun selectTargets(targets: List<PsiElement>, selectionConsumer: Consumer<in List<PsiElement>>) {
            selectionConsumer.consume(targets)
        }

        override fun computeUsages(targets: List<PsiElement>) {
            val name = getName()
            PsiTreeUtil.collectElements(myTarget.containingFile) {
                (it is ImpexMacroNameDec && it.textMatches(name))
                    || (it is ImpexMacroUsageDec && it.reference?.resolve()?.text == name)
            }
                .forEach { addOccurrence(it) }
        }

        private fun getName() = if (myTarget.elementType == ImpexTypes.MACRO_USAGE) {
            myTarget.parent.reference
                ?.resolve()
                ?.text
                ?: myTarget.text
        } else {
            myTarget.text
        }
    }

}
