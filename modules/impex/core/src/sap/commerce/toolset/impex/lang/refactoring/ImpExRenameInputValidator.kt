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
package sap.commerce.toolset.impex.lang.refactoring

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameInputValidator
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.psi.ImpExDocumentIdDec
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExMacroNameDec
import sap.commerce.toolset.impex.psi.ImpExMacroUsageDec

class ImpExRenameInputValidator : RenameInputValidator {

    override fun getPattern(): ElementPattern<out PsiElement> {
        return StandardPatterns.or(
            PlatformPatterns.psiElement(ImpExDocumentIdDec::class.java),
            PlatformPatterns.psiElement(ImpExDocumentIdUsage::class.java),
            PlatformPatterns.psiElement(ImpExMacroNameDec::class.java),
            PlatformPatterns.psiElement(ImpExMacroUsageDec::class.java),
        )
    }

    override fun isInputValid(newName: String, element: PsiElement, context: ProcessingContext) = when (element) {
        is ImpExDocumentIdDec,
        is ImpExDocumentIdUsage -> newName.startsWith(ImpExConstants.IMPEX_PREFIX_DOC_ID)

        is ImpExMacroNameDec,
        is ImpExMacroUsageDec -> newName.startsWith(ImpExConstants.IMPEX_PREFIX_MACRO)

        else -> false
    }
}
