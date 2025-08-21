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
package sap.commerce.toolset.flexibleSearch.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings

class FxSFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(formattingContext: FormattingContext): FormattingModel = createModelInternally(
        formattingContext.psiElement,
        formattingContext.codeStyleSettings
    )

    private fun createModelInternally(element: PsiElement, settings: CodeStyleSettings) = FormattingModelProvider
        .createFormattingModelForPsiFile(
            element.containingFile,
            createRootBlock(element, settings),
            settings
        )

    override fun getRangeAffectingIndent(
        file: PsiFile, offset: Int, elementAtOffset: ASTNode
    ): TextRange? {
        return null
    }

    private fun createRootBlock(
        element: PsiElement,
        settings: CodeStyleSettings
    ) = FxSBlock(
        element.node,
        null,
        Indent.getNoneIndent(),
        Wrap.createWrap(WrapType.NONE, false),
        settings,
        FxSSpacingBuilder(settings)
    )

}
