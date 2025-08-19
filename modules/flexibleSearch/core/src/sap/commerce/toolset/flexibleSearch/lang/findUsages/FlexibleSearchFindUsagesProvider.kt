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

package sap.commerce.toolset.flexibleSearch.lang.findUsages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLexer
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes

class FlexibleSearchFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner() = DefaultWordsScanner(
        FlexibleSearchLexer(),
        TokenSet.create(
            FlexibleSearchTypes.IDENTIFIER,
            FlexibleSearchTypes.BACKTICK_LITERAL,
        ),
        TokenSet.create(
            FlexibleSearchTypes.COMMENT,
            FlexibleSearchTypes.LINE_COMMENT
        ),
        TokenSet.EMPTY
    )

    override fun canFindUsagesFor(psiElement: PsiElement) = FlexibleSearchConstants.SUPPORTED_ELEMENT_TYPES.contains(psiElement.elementType)
    override fun getHelpId(psiElement: PsiElement) = null
    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = element.text

    // In case of presentation customization need rely on ElementDescriptionProvider
    override fun getType(element: PsiElement) = ""
    override fun getDescriptiveName(element: PsiElement) = ""

}