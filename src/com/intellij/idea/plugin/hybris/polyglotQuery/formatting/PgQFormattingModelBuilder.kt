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
package com.intellij.idea.plugin.hybris.polyglotQuery.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings

class PgQFormattingModelBuilder : FormattingModelBuilder {

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

    override fun getRangeAffectingIndent(file: PsiFile, offset: Int, elementAtOffset: ASTNode) = null

    private fun createRootBlock(
        element: PsiElement,
        settings: CodeStyleSettings
    ) = PgQBlock(
        element.node,
        null,
        Indent.getNoneIndent(),
        Wrap.createWrap(WrapType.NONE, false),
        settings,
        PgQSpacingBuilder(settings)
    )

}
