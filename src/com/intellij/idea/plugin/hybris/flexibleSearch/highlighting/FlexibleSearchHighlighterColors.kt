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

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import org.jetbrains.annotations.NonNls

object FlexibleSearchHighlighterColors {

    val FS_KEYWORD = key("KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val FS_STRING = key("STRING", DefaultLanguageHighlighterColors.STRING)
    val FS_BRACES = key("BRACES", DefaultLanguageHighlighterColors.BRACES)
    val FS_PARENS = key("PARENS", DefaultLanguageHighlighterColors.BRACES)
    val FS_BRACKETS = key("BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val FS_PARENTHESES = key("PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val FS_SYMBOL = key("COMMA", DefaultLanguageHighlighterColors.KEYWORD)
    val FS_NUMBER = key("NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val FS_COLUMN = key("COLUMN", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val FS_TABLE = key("TABLE NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val FS_COMMENT = key("COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val FS_FUNCTION_CALL = key("FS_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    val FS_OUTER_JOIN = key("FS_OUTER_JOIN", DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT_HIGHLIGHTED)
    val FS_LOCALIZED = key("FS_LOCALIZED", DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT)
    val FS_PARAMETER = key("FS_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)
    val FS_COLUMN_SEPARATOR = key("FS_COLUMN_SEPARATOR", DefaultLanguageHighlighterColors.DOT)
    val FS_ALIAS = key("FS_ALIAS", DefaultLanguageHighlighterColors.LABEL)

    private fun key(
        externalName: @NonNls String,
        fallbackAttributeKey: TextAttributesKey
    ) = TextAttributesKey.createTextAttributesKey(externalName, fallbackAttributeKey)
}