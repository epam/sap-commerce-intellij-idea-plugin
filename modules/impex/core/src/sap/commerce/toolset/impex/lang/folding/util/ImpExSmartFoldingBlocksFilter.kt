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
package sap.commerce.toolset.impex.lang.folding.util

import com.intellij.openapi.components.Service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import sap.commerce.toolset.impex.psi.ImpExAttribute
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec
import sap.commerce.toolset.impex.psi.ImpExParameters
import sap.commerce.toolset.impex.psi.ImpExUserRightsPermissionValue
import sap.commerce.toolset.impex.utils.ImpExPsiUtils

@Service
class ImpExSmartFoldingBlocksFilter : AbstractImpExFoldingFilter() {

    override fun isFoldable(element: PsiElement) = isSupportedType(element)
        && (ImpExPsiUtils.isLineBreak(element) || isNotBlankPlaceholder(element))

    private fun isSupportedType(element: PsiElement) = element is ImpExParameters
        || element is ImpExUserRightsPermissionValue
        || ImpExPsiUtils.isLineBreak(element)
        || (element is ImpExAttribute && PsiTreeUtil.findChildOfType(element, ImpExMacroUsageDec::class.java) == null)

    private fun isNotBlankPlaceholder(element: PsiElement) = ImpExSmartFoldingPlaceholderBuilder.getInstance().getPlaceholder(element)
        .isNotBlank()

}