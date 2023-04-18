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

package com.intellij.idea.plugin.hybris.flexibleSearch.highlighting;

import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLexer;
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchParserDefinition;
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_BRACES;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_BRACKETS;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_COLUMN;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_COMMENT;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_KEYWORD;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_NUMBER;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_PARAMETER;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_PARENTHESES;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_STRING;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_SYMBOL;
import static com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FS_TABLE;

public class FlexibleSearchSyntaxHighlighter extends SyntaxHighlighterBase {

//    private static final TokenSet BRACES_TOKEN_SET = TokenSet.create(
//        FlexibleSearchTypes.LEFT_BRACE,
//        FlexibleSearchTypes.LEFT_DOUBLE_BRACE,
//        FlexibleSearchTypes.RIGHT_BRACE,
//        FlexibleSearchTypes.RIGHT_DOUBLE_BRACE
//    );
    private static final TokenSet SYMBOL_TOKEN_SET = TokenSet.create(
        FlexibleSearchTypes.DOT,
        FlexibleSearchTypes.COMMA
    );

    private static final TokenSet PARAMETER_REFERENCE_TOKEN_SET = TokenSet.create(
        FlexibleSearchTypes.QUESTION_MARK,
        FlexibleSearchTypes.NAMED_PARAMETER
    );

    private static final TokenSet KEYWORD_TOKEN_SET = TokenSet.create(
        FlexibleSearchTypes.SELECT,
        FlexibleSearchTypes.FROM,
        FlexibleSearchTypes.WHERE,
        FlexibleSearchTypes.DISTINCT,
        FlexibleSearchTypes.GROUP,
        FlexibleSearchTypes.AS,
        FlexibleSearchTypes.IS,
        FlexibleSearchTypes.NULL,
        FlexibleSearchTypes.LIKE,
        FlexibleSearchTypes.AND,
        FlexibleSearchTypes.OR,
        FlexibleSearchTypes.IN,
        FlexibleSearchTypes.ORDER,
        FlexibleSearchTypes.BY,
        FlexibleSearchTypes.DESC,
        FlexibleSearchTypes.ASC,
        FlexibleSearchTypes.ALL,
        FlexibleSearchTypes.JOIN,
        FlexibleSearchTypes.ON,
        FlexibleSearchTypes.LEFT,
        FlexibleSearchTypes.UNION,
        FlexibleSearchTypes.NOT,
        FlexibleSearchTypes.EXISTS
    );

    public static final TextAttributesKey[] KEYWORD_KEYS = pack(FS_KEYWORD);
    public static final TextAttributesKey[] STRING_KEYS = pack(FS_STRING);
    public static final TextAttributesKey[] SYMBOL_KEYS = pack(FS_SYMBOL);

    public static final TextAttributesKey[] BRACES_KEYS = pack(FS_BRACES);
    public static final TextAttributesKey[] BRACKETS_KEYS = pack(FS_BRACKETS);
    public static final TextAttributesKey[] PARENTHESES_KEYS = pack(FS_PARENTHESES);

    public static final TextAttributesKey[] NUMBER_KEYS = pack(FS_NUMBER);

    public static final TextAttributesKey[] COLUMN_KEYS = pack(FS_COLUMN);
    public static final TextAttributesKey[] TABLE_KEYS = pack(FS_TABLE);

    public static final TextAttributesKey[] COMMENT_KEYS = pack(FS_COMMENT);

    public static final TextAttributesKey[] BAD_CHARACTER_KEYS = pack(HighlighterColors.BAD_CHARACTER);

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new FlexibleSearchLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (KEYWORD_TOKEN_SET.contains(tokenType)) {
            return KEYWORD_KEYS;
        } else if (SYMBOL_TOKEN_SET.contains(tokenType)) {
            return SYMBOL_KEYS;
        } else if (PARAMETER_REFERENCE_TOKEN_SET.contains(tokenType)) {
            return pack(FS_PARAMETER);
//        } else if (BRACES_TOKEN_SET.contains(tokenType)) {
//            return BRACES_KEYS;
        } else if (tokenType.equals(FlexibleSearchTypes.LINE_COMMENT) || tokenType.equals(FlexibleSearchTypes.COMMENT)) {
            return COMMENT_KEYS;
        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return STRING_KEYS;
        } else {
            return EMPTY_KEYS;
        }
    }
}