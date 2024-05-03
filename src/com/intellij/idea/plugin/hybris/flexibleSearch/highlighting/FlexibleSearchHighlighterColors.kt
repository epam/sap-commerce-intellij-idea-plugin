/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

object FlexibleSearchHighlighterColors {

    val FXS_KEYWORD = key("FXS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val FXS_STRING = key("FXS_STRING", DefaultLanguageHighlighterColors.STRING)
    val FXS_BRACES = key("FXS_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val FXS_DBRACES = key("FXS_DBRACES", DefaultLanguageHighlighterColors.BRACES)
    val FXS_PARENS = key("FXS_PARENS", DefaultLanguageHighlighterColors.PARENTHESES)
    val FXS_BRACKETS = key("FXS_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val FXS_SYMBOL = key("FXS_SYMBOL", DefaultLanguageHighlighterColors.KEYWORD)
    val FXS_NUMBER = key("FXS_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val FXS_COLUMN = key("FXS_COLUMN", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val FXS_TABLE = key("FXS_TABLE_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val FXS_COMMENT = key("FXS_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val FXS_FUNCTION_CALL = key("FXS_FUNCTION_CALL", DefaultLanguageHighlighterColors.STATIC_METHOD)
    val FXS_OUTER_JOIN = key("FXS_OUTER_JOIN", DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT_HIGHLIGHTED)
    val FXS_LOCALIZED = key("FXS_LOCALIZED", DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT)
    val FXS_PARAMETER = key("FXS_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)
    val FXS_COLUMN_SEPARATOR = key("FXS_COLUMN_SEPARATOR", DefaultLanguageHighlighterColors.DOT)
    val FXS_TABLE_ALIAS = key("FXS_TABLE_ALIAS", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val FXS_COLUMN_ALIAS = key("FXS_COLUMN_ALIAS", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val FXS_TABLE_TAIL = key("FXS_TABLE_TAIL", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL)
    val FXS_BOOLEAN = key("FXS_BOOLEAN", DefaultLanguageHighlighterColors.KEYWORD)
    val FXS_STAR = key("FXS_STAR", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val FXS_OPERATION_SIGN = key("FXS_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN)

    private fun key(
        externalName: String,
        fallbackAttributeKey: TextAttributesKey
    ) = TextAttributesKey.createTextAttributesKey(externalName, fallbackAttributeKey)
}