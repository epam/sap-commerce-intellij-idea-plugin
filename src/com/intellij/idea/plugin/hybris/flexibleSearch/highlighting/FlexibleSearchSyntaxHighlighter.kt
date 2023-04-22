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
package com.intellij.idea.plugin.hybris.flexibleSearch.highlighting

import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLexer
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class FlexibleSearchSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = FlexibleSearchLexer()

    override fun getTokenHighlights(tokenType: IElementType) = cache[tokenType]
        ?: emptyArray()

    companion object {

        val instance: FlexibleSearchSyntaxHighlighter = ApplicationManager.getApplication().getService(FlexibleSearchSyntaxHighlighter::class.java)

        private val CASE_KEYS = pack(FlexibleSearchHighlighterColors.FS_KEYWORD)
        private val PARAMETER_KEYS = pack(FlexibleSearchHighlighterColors.FS_PARAMETER)
        private val KEYWORD_KEYS = pack(FlexibleSearchHighlighterColors.FS_KEYWORD)
        private val STRING_KEYS = pack(FlexibleSearchHighlighterColors.FS_STRING)
        private val SYMBOL_KEYS = pack(FlexibleSearchHighlighterColors.FS_SYMBOL)
        private val BRACES_KEYS = pack(FlexibleSearchHighlighterColors.FS_BRACES)
        private val PARENS_KEYS = pack(FlexibleSearchHighlighterColors.FS_PARENS)
        private val BRACKETS_KEYS = pack(FlexibleSearchHighlighterColors.FS_BRACKETS)
        private val PARENTHESES_KEYS = pack(FlexibleSearchHighlighterColors.FS_PARENTHESES)
        private val NUMBER_KEYS = pack(FlexibleSearchHighlighterColors.FS_NUMBER)
        private val COLUMN_KEYS = pack(FlexibleSearchHighlighterColors.FS_COLUMN)
        private val TABLE_KEYS = pack(FlexibleSearchHighlighterColors.FS_TABLE)
        private val COMMENT_KEYS = pack(FlexibleSearchHighlighterColors.FS_COMMENT)
        private val OUTER_JOIN_KEYS = pack(FlexibleSearchHighlighterColors.FS_OUTER_JOIN)
        private val FUNCTION_CALL_KEYS = pack(FlexibleSearchHighlighterColors.FS_FUNCTION_CALL)
        private val LOCALIZED_KEYS = pack(FlexibleSearchHighlighterColors.FS_LOCALIZED)
        private val COLUMN_SEPARATOR = pack(FlexibleSearchHighlighterColors.FS_COLUMN_SEPARATOR)
        private val ALIAS = pack(FlexibleSearchHighlighterColors.FS_ALIAS)
        private val BAD_CHARACTER_KEYS = pack(HighlighterColors.BAD_CHARACTER)

        private val cache: Map<IElementType, Array<TextAttributesKey>> = mapOf(
            TokenType.BAD_CHARACTER to BAD_CHARACTER_KEYS,

            FlexibleSearchTypes.COLUMN_OUTER_JOIN_NAME to OUTER_JOIN_KEYS,
            FlexibleSearchTypes.OUTER_JOIN to OUTER_JOIN_KEYS,

            FlexibleSearchTypes.FUNCTION_NAME to FUNCTION_CALL_KEYS,

            FlexibleSearchTypes.COLUMN_LOCALIZED_NAME to LOCALIZED_KEYS,

            FlexibleSearchTypes.COLUMN_SEPARATOR to COLUMN_SEPARATOR,

            FlexibleSearchTypes.TABLE_ALIAS_NAME to ALIAS,
            FlexibleSearchTypes.COLUMN_ALIAS_NAME to ALIAS,

            FlexibleSearchTypes.SINGLE_QUOTE_STRING_LITERAL to STRING_KEYS,
            FlexibleSearchTypes.DOUBLE_QUOTE_STRING_LITERAL to STRING_KEYS,

            FlexibleSearchTypes.DEFINED_TABLE_NAME to TABLE_KEYS,
            FlexibleSearchTypes.SELECTED_TABLE_NAME to TABLE_KEYS,

            FlexibleSearchTypes.COLUMN_NAME to COLUMN_KEYS,
            FlexibleSearchTypes.Y_COLUMN_NAME to COLUMN_KEYS,

            FlexibleSearchTypes.SIGNED_NUMBER to NUMBER_KEYS,

            FlexibleSearchTypes.LINE_COMMENT to COMMENT_KEYS,
            FlexibleSearchTypes.COMMENT to COMMENT_KEYS,

            FlexibleSearchTypes.LBRACE to BRACES_KEYS,
            FlexibleSearchTypes.LDBRACE to BRACES_KEYS,
            FlexibleSearchTypes.RBRACE to BRACES_KEYS,
            FlexibleSearchTypes.RDBRACE to BRACES_KEYS,

            FlexibleSearchTypes.LBRACKET to BRACKETS_KEYS,
            FlexibleSearchTypes.RBRACKET to BRACKETS_KEYS,

            FlexibleSearchTypes.LPAREN to PARENS_KEYS,
            FlexibleSearchTypes.RPAREN to PARENS_KEYS,

            FlexibleSearchTypes.QUESTION_MARK to PARAMETER_KEYS,
            FlexibleSearchTypes.NAMED_PARAMETER to PARAMETER_KEYS,
            FlexibleSearchTypes.EXT_PARAMETER_NAME to PARAMETER_KEYS,

            FlexibleSearchTypes.DOT to SYMBOL_KEYS,
            FlexibleSearchTypes.COLON to SYMBOL_KEYS,
            FlexibleSearchTypes.COMMA to SYMBOL_KEYS,

            FlexibleSearchTypes.CASE to CASE_KEYS,
            FlexibleSearchTypes.WHEN to CASE_KEYS,
            FlexibleSearchTypes.ELSE to CASE_KEYS,
            FlexibleSearchTypes.END to CASE_KEYS,
            FlexibleSearchTypes.THEN to CASE_KEYS,

            FlexibleSearchTypes.SELECT to KEYWORD_KEYS,
            FlexibleSearchTypes.FROM to KEYWORD_KEYS,
            FlexibleSearchTypes.WHERE to KEYWORD_KEYS,
            FlexibleSearchTypes.DISTINCT to KEYWORD_KEYS,
            FlexibleSearchTypes.GROUP to KEYWORD_KEYS,
            FlexibleSearchTypes.AS to KEYWORD_KEYS,
            FlexibleSearchTypes.IS to KEYWORD_KEYS,
            FlexibleSearchTypes.NULL to KEYWORD_KEYS,
            FlexibleSearchTypes.LIKE to KEYWORD_KEYS,
            FlexibleSearchTypes.AND to KEYWORD_KEYS,
            FlexibleSearchTypes.OR to KEYWORD_KEYS,
            FlexibleSearchTypes.IN to KEYWORD_KEYS,
            FlexibleSearchTypes.ORDER to KEYWORD_KEYS,
            FlexibleSearchTypes.BY to KEYWORD_KEYS,
            FlexibleSearchTypes.DESC to KEYWORD_KEYS,
            FlexibleSearchTypes.ASC to KEYWORD_KEYS,
            FlexibleSearchTypes.ALL to KEYWORD_KEYS,
            FlexibleSearchTypes.ON to KEYWORD_KEYS,
            FlexibleSearchTypes.JOIN to KEYWORD_KEYS,
            FlexibleSearchTypes.RIGHT to KEYWORD_KEYS,
            FlexibleSearchTypes.LEFT to KEYWORD_KEYS,
            FlexibleSearchTypes.UNION to KEYWORD_KEYS,
            FlexibleSearchTypes.NOT to KEYWORD_KEYS,
            FlexibleSearchTypes.EXISTS to KEYWORD_KEYS,

            )
    }
}