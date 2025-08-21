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
package sap.commerce.toolset.flexibleSearch.lang

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes

class FlexibleSearchPairedBraceMatcher : PairedBraceMatcher {

    private val _pairs = arrayOf(
        BracePair(FlexibleSearchTypes.LPAREN, FlexibleSearchTypes.RPAREN, true),
        BracePair(FlexibleSearchTypes.LBRACE, FlexibleSearchTypes.RBRACE, true),
        BracePair(FlexibleSearchTypes.LDBRACE, FlexibleSearchTypes.RDBRACE, true),
        BracePair(FlexibleSearchTypes.LBRACKET, FlexibleSearchTypes.RBRACKET, true)
    )

    override fun getPairs(): Array<BracePair> = _pairs
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

}