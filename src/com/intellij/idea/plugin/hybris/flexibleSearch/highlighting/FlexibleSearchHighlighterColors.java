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

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACKETS;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.PARENTHESES;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING;

public final class FlexibleSearchHighlighterColors {

    private FlexibleSearchHighlighterColors() {
    }

    public static final TextAttributesKey FS_KEYWORD = key("KEYWORD", KEYWORD);
    public static final TextAttributesKey FS_STRING = key("STRING", STRING);

    public static final TextAttributesKey FS_BRACES = key("BRACES", BRACES);
    public static final TextAttributesKey FS_PARENTHESES = key("PARENTHESES", PARENTHESES);
    public static final TextAttributesKey FS_BRACKETS = key("BRACKETS", BRACKETS);

    public static final TextAttributesKey FS_SYMBOL = key("COMMA", KEYWORD);
    public static final TextAttributesKey FS_NUMBER = key("NUMBER", NUMBER);

    public static final TextAttributesKey FS_COLUMN = key("COLUMN", INSTANCE_FIELD);
    public static final TextAttributesKey FS_TABLE = key("TABLE NAME", FUNCTION_DECLARATION);

    public static final TextAttributesKey FS_COMMENT = key("COMMENT", LINE_COMMENT);

    public static final TextAttributesKey FS_PARAMETER = key("FS_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);


    private static TextAttributesKey key(
        @NonNls @NotNull final String externalName,
        final TextAttributesKey fallbackAttributeKey
    ) {
        return TextAttributesKey.createTextAttributesKey(externalName, fallbackAttributeKey);
    }
}