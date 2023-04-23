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
package com.intellij.idea.plugin.hybris.flexibleSearch.formatting

import com.intellij.formatting.*
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage.Companion.INSTANCE
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet

class FxSFormattingModelBuilder : FormattingModelBuilder {

    override fun createModel(formattingContext: FormattingContext) = createModelInternally(
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
        Alignment.createAlignment(),
        Indent.getNoneIndent(),
        Wrap.createWrap(WrapType.NONE, false),
        settings,
        createSpaceBuilder(settings)
    )

    private fun createSpaceBuilder(settings: CodeStyleSettings) = SpacingBuilder(settings, INSTANCE)
        .before(RBRACE)
        .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACES)

        .after(LBRACE)
        .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_BRACES)

        .before(RDBRACE)
        .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES)

        .after(LDBRACE)
        .spaceIf(FxSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES)

        .after(TokenSet.create(SELECT, FROM, BY, ON, CASE, WHEN, THEN, ELSE, AS))
        .spaces(1)

        .before(TokenSet.create(AS, THEN, ELSE, END))
        .spaces(1)

        .before(TokenSet.create(EQ, EQEQ, GT, GTE, LT, LTE, MINUS, MOD, NOT_EQ, PLUS, SHL, SHR, UNEQ))
        .spaces(1)
        .after(TokenSet.create(EQ, EQEQ, GT, GTE, LT, LTE, MINUS, MOD, NOT_EQ, PLUS, SHL, SHR, UNEQ))
        .spaces(1)

        .before(TokenSet.create(IS, NOT, NULL))
        .spaces(1)

        .before(TokenSet.create(COLUMN_OUTER_JOIN_NAME, COLUMN_LOCALIZED_NAME))
        .spaces(0)

        .before(RBRACKET)
        .spaces(0)

        .after(LBRACKET)
        .spaces(0)

        .after(COMMA)
        .spaces(1)

//        .before(TokenSet.create(THEN, ELSE, END))
//        .spacing(1, 5, 0, true, 0)

//        .around(TokenSet.create(FROM, WHERE, ON, AS, ORDER, BY))
//        .spaces(1)
//
//        .around(TokenSet.create(CASE, THEN, ELSE, END))
//        .spaces(1)
//
//        .around(TokenSet.create(AND, OR, IS, NOT, NULL))
//        .spaces(1)
//
//        .after(COMMA)
//        .spaces(1)
//
//        .after(SELECT)
//        .spaces(1)

//            .around(TokenSet.create(WHERE, FROM, ON))
//            .spaces(1)
//            .between(FROM_CLAUSE, WHERE_CLAUSE)
//            .spaces(1)

//            .before(RIGHT_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_BRACES)
//            .after(LEFT_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_BRACES)
//
//            .before(RIGHT_DOUBLE_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES)
//            .after(LEFT_DOUBLE_BRACE)
//            .spaceIf(FSCodeStyleSettings.SPACES_INSIDE_DOUBLE_BRACES);

}
