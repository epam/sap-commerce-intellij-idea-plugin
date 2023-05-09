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

package com.intellij.idea.plugin.hybris.polyglotQuery.highlighting

import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLexer
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class PolyglotQuerySyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = PolyglotQueryLexer()

    override fun getTokenHighlights(tokenType: IElementType?) = cache[tokenType]
        ?: emptyArray()

    companion object {
        val instance: PolyglotQuerySyntaxHighlighter = ApplicationManager.getApplication().getService(PolyglotQuerySyntaxHighlighter::class.java)

        private val BAD_CHARACTER_KEYS = pack(HighlighterColors.BAD_CHARACTER)

        private val KEYWORD = pack(PolyglotQueryHighlighterColors.PGQ_KEYWORD)
        private val STRING = pack(PolyglotQueryHighlighterColors.PGQ_STRING)
        private val PARENS = pack(PolyglotQueryHighlighterColors.PGQ_PARENS)
        private val BRACKETS = pack(PolyglotQueryHighlighterColors.PGQ_BRACKETS)
        private val BRACES = pack(PolyglotQueryHighlighterColors.PGQ_BRACES)
        private val NUMBER = pack(PolyglotQueryHighlighterColors.PGQ_NUMBER)
        private val COMMENT = pack(PolyglotQueryHighlighterColors.PGQ_COMMENT)
        private val LOCALIZED = pack(PolyglotQueryHighlighterColors.PGQ_LOCALIZED)
        private val PARAMETER = pack(PolyglotQueryHighlighterColors.PGQ_PARAMETER)
        private val OPERAND = pack(PolyglotQueryHighlighterColors.PGQ_OPERAND)
        private val TYPE = pack(PolyglotQueryHighlighterColors.PGQ_TYPE)
        private val COLUMN = pack(PolyglotQueryHighlighterColors.PGQ_COLUMN)


        private val cache: Map<IElementType, Array<TextAttributesKey>> = mapOf(
            TokenType.BAD_CHARACTER to BAD_CHARACTER_KEYS,

            PolyglotQueryTypes.LINE_COMMENT to COMMENT,
            PolyglotQueryTypes.COMMENT to COMMENT,

            PolyglotQueryTypes.BY to KEYWORD,
            PolyglotQueryTypes.AND to KEYWORD,
            PolyglotQueryTypes.ASC to KEYWORD,
            PolyglotQueryTypes.DESC to KEYWORD,
            PolyglotQueryTypes.GET to KEYWORD,
            PolyglotQueryTypes.IS to KEYWORD,
            PolyglotQueryTypes.NOT to KEYWORD,
            PolyglotQueryTypes.NULL to KEYWORD,
            PolyglotQueryTypes.OR to KEYWORD,
            PolyglotQueryTypes.ORDER to KEYWORD,
            PolyglotQueryTypes.WHERE to KEYWORD,

            PolyglotQueryTypes.EQ to OPERAND,
            PolyglotQueryTypes.GT to OPERAND,
            PolyglotQueryTypes.GTE to OPERAND,
            PolyglotQueryTypes.LT to OPERAND,
            PolyglotQueryTypes.LTE to OPERAND,
            PolyglotQueryTypes.UNEQ to OPERAND,
            PolyglotQueryTypes.AMP to OPERAND,

            PolyglotQueryTypes.LBRACE to BRACES,
            PolyglotQueryTypes.RBRACE to BRACES,

            PolyglotQueryTypes.LBRACKET to BRACKETS,
            PolyglotQueryTypes.RBRACKET to BRACKETS,

            PolyglotQueryTypes.LPAREN to PARENS,
            PolyglotQueryTypes.RPAREN to PARENS,

            PolyglotQueryTypes.TYPE_KEY_NAME to TYPE,

            PolyglotQueryTypes.BIND_PARAMETER to PARAMETER,

            PolyglotQueryTypes.LOCALIZED_NAME to LOCALIZED,

            PolyglotQueryTypes.ATTRIBUTE_KEY_NAME to COLUMN,
        )
    }
}