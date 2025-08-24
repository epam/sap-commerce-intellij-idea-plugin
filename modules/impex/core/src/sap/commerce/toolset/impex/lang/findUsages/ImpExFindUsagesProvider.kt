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
package sap.commerce.toolset.impex.lang.findUsages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.ImpExLexerAdapter
import sap.commerce.toolset.impex.psi.ImpExTypes
import sap.commerce.toolset.impex.utils.ImpExPsiUtils

class ImpExFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner() = DefaultWordsScanner(
        ImpExLexerAdapter(),
        TokenSet.orSet(
            TokenSet.create(ImpExTypes.MACRO_NAME_DECLARATION),
            TokenSet.create(ImpExTypes.MACRO_DECLARATION),
            TokenSet.create(ImpExTypes.MACRO_USAGE),
            TokenSet.create(ImpExTypes.FIELD_VALUE),
        ),
        TokenSet.create(
            ImpExTypes.COMMENT,
            ImpExTypes.LINE_COMMENT,
        ),
        TokenSet.ANY
    )

    override fun canFindUsagesFor(psiElement: PsiElement) = psiElement is PsiNamedElement
        && !psiElement.text.startsWith(ImpExConstants.IMPEX_CONFIG_COMPLETE_PREFIX)

    override fun getHelpId(psiElement: PsiElement) = null
    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = element.text
    override fun getDescriptiveName(element: PsiElement): String = element.text

    override fun getType(element: PsiElement) = if (ImpExPsiUtils.isMacroNameDeclaration(element) || ImpExPsiUtils.isMacroUsage(element)) {
        "macros"
    } else "unknown"
}
