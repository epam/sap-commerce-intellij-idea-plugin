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

package com.intellij.idea.plugin.hybris.impex.highlighting;

import com.intellij.idea.plugin.hybris.impex.ImpexLexerAdapter;
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ALTERNATIVE_MAP_DELIMITER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ALTERNATIVE_PATTERN;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ASSIGN_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ATTRIBUTE_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ATTRIBUTE_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.BEAN_SHELL_BODY;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.BEAN_SHELL_MARKER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.BOOLEAN;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.COMMA;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DEFAULT_KEY_VALUE_DELIMITER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DEFAULT_PATH_DELIMITER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DIGIT;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DOCUMENT_ID;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DOUBLE_STRING;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_LIST_ITEM_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_VALUE_IGNORE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_VALUE_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_INSERT;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_INSERT_UPDATE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_REMOVE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_UPDATE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_PARAMETER_NAME;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_SPECIAL_PARAMETER_NAME;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_TYPE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FUNCTION_CALL;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.MACRO_NAME_DECLARATION;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.MACRO_USAGE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.MACRO_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.PARAMETERS_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.PROPERTY_COMMENT;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ROUND_BRACKETS;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.SINGLE_STRING;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.SQUARE_BRACKETS;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.VALUE_SUBTYPE;

public class DefaultImpexSyntaxHighlighter extends ImpexSyntaxHighlighter {

    public static final TextAttributesKey[] PROPERTY_COMMENT_KEYS = pack(PROPERTY_COMMENT);
    public static final TextAttributesKey[] MACRO_NAME_DECLARATION_KEYS = pack(MACRO_NAME_DECLARATION);
    public static final TextAttributesKey[] MACRO_VALUE_KEYS = pack(MACRO_VALUE);
    public static final TextAttributesKey[] MACRO_USAGE_KEYS = pack(MACRO_USAGE);
    public static final TextAttributesKey[] ASSIGN_VALUE_KEYS = pack(ASSIGN_VALUE);
    public static final TextAttributesKey[] HEADER_MODE_INSERT_KEYS = pack(HEADER_MODE_INSERT);
    public static final TextAttributesKey[] HEADER_MODE_UPDATE_KEYS = pack(HEADER_MODE_UPDATE);
    public static final TextAttributesKey[] HEADER_MODE_INSERT_UPDATE_KEYS = pack(HEADER_MODE_INSERT_UPDATE);
    public static final TextAttributesKey[] HEADER_MODE_REMOVE_KEYS = pack(HEADER_MODE_REMOVE);
    public static final TextAttributesKey[] HEADER_TYPE_KEYS = pack(HEADER_TYPE);
    public static final TextAttributesKey[] FUNCTION_KEYS = pack(FUNCTION_CALL);
    public static final TextAttributesKey[] VALUE_SUBTYPE_KEYS = pack(VALUE_SUBTYPE);
    public static final TextAttributesKey[] FIELD_VALUE_SEPARATOR_KEYS = pack(FIELD_VALUE_SEPARATOR);
    public static final TextAttributesKey[] FIELD_LIST_ITEM_SEPARATOR_KEYS = pack(FIELD_LIST_ITEM_SEPARATOR);
    public static final TextAttributesKey[] FIELD_VALUE_KEYS = pack(FIELD_VALUE);
    public static final TextAttributesKey[] SINGLE_STRING_KEYS = pack(SINGLE_STRING);
    public static final TextAttributesKey[] DOUBLE_STRING_KEYS = pack(DOUBLE_STRING);
    public static final TextAttributesKey[] FIELD_VALUE_IGNORE_KEYS = pack(FIELD_VALUE_IGNORE);
    public static final TextAttributesKey[] BEAN_SHELL_MARKER_KEYS = pack(BEAN_SHELL_MARKER);
    public static final TextAttributesKey[] BEAN_SHELL_BODY_KEYS = pack(BEAN_SHELL_BODY);
    public static final TextAttributesKey[] SQUARE_BRACKETS_KEYS = pack(SQUARE_BRACKETS);
    public static final TextAttributesKey[] ROUND_BRACKETS_KEYS = pack(ROUND_BRACKETS);
    public static final TextAttributesKey[] ATTRIBUTE_NAME_KEYS = pack(ATTRIBUTE_NAME);
    public static final TextAttributesKey[] ATTRIBUTE_VALUE_KEYS = pack(ATTRIBUTE_VALUE);
    public static final TextAttributesKey[] ATTRIBUTE_SEPARATOR_KEYS = pack(ATTRIBUTE_SEPARATOR);
    public static final TextAttributesKey[] BOOLEAN_KEYS = pack(BOOLEAN);
    public static final TextAttributesKey[] DIGIT_KEYS = pack(DIGIT);
    public static final TextAttributesKey[] ALTERNATIVE_MAP_DELIMITER_KEYS = pack(ALTERNATIVE_MAP_DELIMITER);
    public static final TextAttributesKey[] DEFAULT_KEY_VALUE_DELIMITER_KEYS = pack(DEFAULT_KEY_VALUE_DELIMITER);
    public static final TextAttributesKey[] DEFAULT_PATH_DELIMITER_KEYS = pack(DEFAULT_PATH_DELIMITER);
    public static final TextAttributesKey[] HEADER_PARAMETER_NAME_KEYS = pack(HEADER_PARAMETER_NAME);
    public static final TextAttributesKey[] HEADER_SPECIAL_PARAMETER_NAME_KEYS = pack(HEADER_SPECIAL_PARAMETER_NAME);
    public static final TextAttributesKey[] PARAMETERS_SEPARATOR_KEYS = pack(PARAMETERS_SEPARATOR);
    public static final TextAttributesKey[] COMMA_KEYS = pack(COMMA);
    public static final TextAttributesKey[] BAD_CHARACTER_KEYS = pack(HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey[] ALTERNATIVE_PATTERN_KEYS = pack(ALTERNATIVE_PATTERN);
    public static final TextAttributesKey[] DOCUMENT_ID_KEYS = pack(DOCUMENT_ID);

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new ImpexLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(@NotNull final IElementType tokenType) {
        if (tokenType.equals(ImpexTypes.ALTERNATIVE_MAP_DELIMITER)) {
            return ALTERNATIVE_MAP_DELIMITER_KEYS;
        } else if (tokenType.equals(ImpexTypes.DEFAULT_KEY_VALUE_DELIMITER)) {
            return DEFAULT_KEY_VALUE_DELIMITER_KEYS;
        } else if (tokenType.equals(ImpexTypes.ASSIGN_VALUE)) {
            return ASSIGN_VALUE_KEYS;
        } else if (tokenType.equals(ImpexTypes.ATTRIBUTE_NAME)) {
            return ATTRIBUTE_NAME_KEYS;
        } else if (tokenType.equals(ImpexTypes.ATTRIBUTE_SEPARATOR)) {
            return ATTRIBUTE_SEPARATOR_KEYS;
        } else if (tokenType.equals(ImpexTypes.ATTRIBUTE_VALUE)) {
            return ATTRIBUTE_VALUE_KEYS;
        } else if (tokenType.equals(ImpexTypes.BEAN_SHELL_BODY)) {
            return BEAN_SHELL_BODY_KEYS;
        } else if (tokenType.equals(ImpexTypes.BEAN_SHELL_MARKER)) {
            return BEAN_SHELL_MARKER_KEYS;
        } else if (tokenType.equals(ImpexTypes.BOOLEAN)) {
            return BOOLEAN_KEYS;
        } else if (tokenType.equals(ImpexTypes.COMMA)) {
            return COMMA_KEYS;
        } else if (tokenType.equals(ImpexTypes.LINE_COMMENT)) {
            return PROPERTY_COMMENT_KEYS;
        } else if (tokenType.equals(ImpexTypes.DEFAULT_PATH_DELIMITER)) {
            return DEFAULT_PATH_DELIMITER_KEYS;
        } else if (tokenType.equals(ImpexTypes.DIGIT)) {
            return DIGIT_KEYS;
        } else if (tokenType.equals(ImpexTypes.DOUBLE_STRING)) {
            return DOUBLE_STRING_KEYS;
        } else if (tokenType.equals(ImpexTypes.FIELD_LIST_ITEM_SEPARATOR)) {
            return FIELD_LIST_ITEM_SEPARATOR_KEYS;
        } else if (tokenType.equals(ImpexTypes.FIELD_VALUE)) {
            return FIELD_VALUE_KEYS;
        } else if (tokenType.equals(ImpexTypes.FIELD_VALUE_IGNORE)) {
            return FIELD_VALUE_IGNORE_KEYS;
        } else if (tokenType.equals(ImpexTypes.FIELD_VALUE_SEPARATOR)) {
            return FIELD_VALUE_SEPARATOR_KEYS;
        } else if (tokenType.equals(ImpexTypes.HEADER_MODE_INSERT)) {
            return HEADER_MODE_INSERT_KEYS;
        } else if (tokenType.equals(ImpexTypes.HEADER_MODE_INSERT_UPDATE)) {
            return HEADER_MODE_INSERT_UPDATE_KEYS;
        } else if (tokenType.equals(ImpexTypes.HEADER_MODE_REMOVE)) {
            return HEADER_MODE_REMOVE_KEYS;
        } else if (tokenType.equals(ImpexTypes.HEADER_MODE_UPDATE)) {
            return HEADER_MODE_UPDATE_KEYS;
        } else if (tokenType.equals(ImpexTypes.HEADER_PARAMETER_NAME)) {
            return HEADER_PARAMETER_NAME_KEYS;
        } else if (tokenType.equals(ImpexTypes.HEADER_SPECIAL_PARAMETER_NAME)) {
            return HEADER_SPECIAL_PARAMETER_NAME_KEYS;
        } else if (tokenType.equals(ImpexTypes.HEADER_TYPE)) {
            return HEADER_TYPE_KEYS;
        } else if (tokenType.equals(ImpexTypes.MACRO_NAME_DECLARATION)) {
            return MACRO_NAME_DECLARATION_KEYS;
        } else if (tokenType.equals(ImpexTypes.MACRO_USAGE)) {
            return MACRO_USAGE_KEYS;
        } else if (tokenType.equals(ImpexTypes.MACRO_VALUE)) {
            return MACRO_VALUE_KEYS;
        } else if (tokenType.equals(ImpexTypes.PARAMETERS_SEPARATOR)) {
            return PARAMETERS_SEPARATOR_KEYS;
        } else if (tokenType.equals(ImpexTypes.LEFT_ROUND_BRACKET) || tokenType.equals(ImpexTypes.RIGHT_ROUND_BRACKET)) {
            return ROUND_BRACKETS_KEYS;
        } else if (tokenType.equals(ImpexTypes.SINGLE_STRING)) {
            return SINGLE_STRING_KEYS;
        } else if (tokenType.equals(ImpexTypes.LEFT_SQUARE_BRACKET) || tokenType.equals(ImpexTypes.RIGHT_SQUARE_BRACKET)) {
            return SQUARE_BRACKETS_KEYS;
        } else if (tokenType.equals(ImpexTypes.VALUE_SUBTYPE)) {
            return VALUE_SUBTYPE_KEYS;
        } else if (tokenType.equals(ImpexTypes.ALTERNATIVE_PATTERN)) {
            return ALTERNATIVE_PATTERN_KEYS;
        } else if (tokenType.equals(ImpexTypes.DOCUMENT_ID)) {
            return DOCUMENT_ID_KEYS;
        } else if (tokenType.equals(ImpexTypes.FUNCTION)) {
            return FUNCTION_KEYS;
        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHARACTER_KEYS;
        } else {
            return EMPTY_KEYS;
        }
    }
}
