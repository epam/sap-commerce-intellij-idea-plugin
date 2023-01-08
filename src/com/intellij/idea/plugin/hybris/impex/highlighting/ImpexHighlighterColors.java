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

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.FUNCTION_DECLARATION;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.METADATA;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STATIC_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING;
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class ImpexHighlighterColors {

    private ImpexHighlighterColors() {
    }

    public static final TextAttributesKey PROPERTY_COMMENT = key("LINE_COMMENT", LINE_COMMENT);
    public static final TextAttributesKey MACRO_NAME_DECLARATION = key("MACRO_NAME_DECLARATION", INSTANCE_FIELD);
    public static final TextAttributesKey MACRO_VALUE = key("MACRO_VALUE", HighlighterColors.TEXT);
    public static final TextAttributesKey MACRO_USAGE = key("MACRO_USAGE", INSTANCE_FIELD);
    public static final TextAttributesKey ASSIGN_VALUE = key("ASSIGN_VALUE", HighlighterColors.TEXT);
    public static final TextAttributesKey HEADER_MODE_INSERT = key("HEADER_MODE_INSERT", KEYWORD);
    public static final TextAttributesKey HEADER_MODE_UPDATE = key("HEADER_MODE_UPDATE", KEYWORD);
    public static final TextAttributesKey HEADER_MODE_INSERT_UPDATE = key("HEADER_MODE_INSERT_UPDATE", KEYWORD);
    public static final TextAttributesKey HEADER_MODE_REMOVE = key("HEADER_MODE_REMOVE", KEYWORD);
    public static final TextAttributesKey HEADER_TYPE = key("HEADER_TYPE", FUNCTION_DECLARATION);
    public static final TextAttributesKey VALUE_SUBTYPE = key("VALUE_SUBTYPE", METADATA);
    public static final TextAttributesKey FIELD_VALUE_SEPARATOR = key("FIELD_VALUE_SEPARATOR", KEYWORD);
    public static final TextAttributesKey FIELD_LIST_ITEM_SEPARATOR = key("FIELD_LIST_ITEM_SEPARATOR", KEYWORD);
    public static final TextAttributesKey FIELD_VALUE = key("FIELD_VALUE", HighlighterColors.TEXT);
    public static final TextAttributesKey SINGLE_STRING = key("SINGLE_STRING", STRING);
    public static final TextAttributesKey DOUBLE_STRING = key("DOUBLE_STRING", STRING);
    public static final TextAttributesKey FIELD_VALUE_IGNORE = key("FIELD_VALUE_IGNORE", KEYWORD);
    public static final TextAttributesKey BEAN_SHELL_MARKER = key("BEAN_SHELL_MARKER", KEYWORD);
    public static final TextAttributesKey BEAN_SHELL_BODY = key("BEAN_SHELL_BODY", HighlighterColors.TEXT);
    public static final TextAttributesKey SQUARE_BRACKETS = key("SQUARE_BRACKETS", KEYWORD);
    public static final TextAttributesKey ROUND_BRACKETS = key("ROUND_BRACKETS", KEYWORD);
    public static final TextAttributesKey ATTRIBUTE_NAME = key("ATTRIBUTE_NAME", HighlighterColors.TEXT);
    public static final TextAttributesKey ATTRIBUTE_VALUE = key("ATTRIBUTE_VALUE", HighlighterColors.TEXT);
    public static final TextAttributesKey ATTRIBUTE_SEPARATOR = key("ATTRIBUTE_SEPARATOR", KEYWORD);
    public static final TextAttributesKey BOOLEAN = key("BOOLEAN", KEYWORD);
    public static final TextAttributesKey DIGIT = key("DIGIT", NUMBER);
    public static final TextAttributesKey ALTERNATIVE_MAP_DELIMITER = key("ALTERNATIVE_MAP_DELIMITER", KEYWORD);
    public static final TextAttributesKey DEFAULT_KEY_VALUE_DELIMITER = key("DEFAULT_KEY_VALUE_DELIMITER", KEYWORD);
    public static final TextAttributesKey DEFAULT_PATH_DELIMITER = key("DEFAULT_PATH_DELIMITER", KEYWORD);
    public static final TextAttributesKey HEADER_PARAMETER_NAME = key("HEADER_PARAMETER_NAME", HighlighterColors.TEXT);
    public static final TextAttributesKey HEADER_SPECIAL_PARAMETER_NAME = key("HEADER_SPECIAL_PARAMETER_NAME", INSTANCE_FIELD);
    public static final TextAttributesKey PARAMETERS_SEPARATOR = key("PARAMETERS_SEPARATOR", KEYWORD);
    public static final TextAttributesKey COMMA = key("COMMA", KEYWORD);
    public static final TextAttributesKey ALTERNATIVE_PATTERN = key("ALTERNATIVE_PATTERN", KEYWORD);
    public static final TextAttributesKey DOCUMENT_ID = key("DOCUMENT_ID", STATIC_FIELD);
    public static final TextAttributesKey WARNINGS_ATTRIBUTES = key("IMPEX_WARNING_ATTRIBUTES", CodeInsightColors.WARNINGS_ATTRIBUTES);
    public static final TextAttributesKey IMPEX_FUNCTION_CALL = key("IMPEX_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

    private static TextAttributesKey key(
        @NonNls @NotNull final String externalName,
        final TextAttributesKey fallbackAttributeKey
    ) {
        return createTextAttributesKey(externalName, fallbackAttributeKey);
    }
}
