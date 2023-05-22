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
package com.intellij.idea.plugin.hybris.impex.find.findUsages

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.ImpexLexerAdapter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet

class ImpexFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner() = DefaultWordsScanner(
        ImpexLexerAdapter(),
        TokenSet.orSet(
            TokenSet.create(ImpexTypes.MACRO_NAME_DECLARATION),
            TokenSet.create(ImpexTypes.MACRO_DECLARATION),
            TokenSet.create(ImpexTypes.MACRO_USAGE)
        ),
        TokenSet.create(
            ImpexTypes.COMMENT,
            ImpexTypes.LINE_COMMENT,
        ),
        TokenSet.ANY
    )

    override fun canFindUsagesFor(psiElement: PsiElement) = psiElement is PsiNamedElement
        && !psiElement.text.startsWith(HybrisConstants.IMPEX_CONFIG_COMPLETE_PREFIX)

    override fun getHelpId(psiElement: PsiElement) = null
    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = element.text
    override fun getDescriptiveName(element: PsiElement): String = element.text

    override fun getType(element: PsiElement) = if (ImpexPsiUtils.isMacroNameDeclaration(element) || ImpexPsiUtils.isMacroUsage(element)) {
        "macros"
    } else "unknown"
}
