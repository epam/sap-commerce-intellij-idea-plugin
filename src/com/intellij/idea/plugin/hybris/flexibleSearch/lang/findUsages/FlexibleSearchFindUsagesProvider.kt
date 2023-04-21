/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.flexibleSearch.lang.findUsages

import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLexer
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTableAliasName
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType

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

    override fun canFindUsagesFor(psiElement: PsiElement) = psiElement.elementType == FlexibleSearchTypes.TABLE_ALIAS_NAME
    override fun getHelpId(psiElement: PsiElement) = null
    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = element.text

    // In case of presentation customization need rely on ElementDescriptionProvider
    override fun getType(element: PsiElement) = ""
    override fun getDescriptiveName(element: PsiElement) = ""

}