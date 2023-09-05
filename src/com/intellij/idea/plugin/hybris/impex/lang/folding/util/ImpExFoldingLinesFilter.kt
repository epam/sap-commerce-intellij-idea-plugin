/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.impex.lang.folding.util

import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexModifier
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.PsiTreeUtil

class ImpExFoldingLinesFilter : AbstractImpExFoldingFilter() {

    override fun isFoldable(element: PsiElement) = isSupportedType(element)
        && !(ImpexPsiUtils.isLineBreak(element) && ImpexPsiUtils.isHeaderLine(element.prevSibling))
        && !(ImpexPsiUtils.isLineBreak(element) && ImpexPsiUtils.isUserRightsMacros(element.prevSibling))
        && !(ImpexPsiUtils.isLineBreak(element) && element.nextSibling == null)

    private fun isSupportedType(element: PsiElement) = element !is PsiFileSystemItem
        && element !is ImpexAnyAttributeName
        && element !is ImpexAnyAttributeValue
        && element !is ImpexAnyHeaderParameterName
        && element !is ImpexAttribute
        && element !is ImpexFullHeaderType
        && element !is ImpexFullHeaderParameter
        && element !is ImpexHeaderTypeName
        && element !is ImpexParameter
        && element !is ImpexParameters
        && element !is ImpexAnyHeaderMode
        && element !is ImpexModifier
        && element !is ImpexModifiers
        && PsiTreeUtil.getParentOfType(element, ImpexHeaderLine::class.java) == null
        && (element is ImpexValueLine || element is ImpexHeaderLine || ImpexPsiUtils.aroundIsValueLine(element))
}
