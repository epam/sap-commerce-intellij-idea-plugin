/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
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
package com.intellij.idea.plugin.hybris.impex.highlighting

import com.intellij.codeInsight.template.impl.TemplateColors
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object ImpexHighlighterColors {

    val PROPERTY_COMMENT = key("LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val MACRO_NAME_DECLARATION = key("MACRO_NAME_DECLARATION", TemplateColors.TEMPLATE_VARIABLE_ATTRIBUTES)
    val MACRO_VALUE = key("MACRO_VALUE", HighlighterColors.TEXT)
    val MACRO_USAGE = key("MACRO_USAGE", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val MACRO_CONFIG_PREFIX = key("IMPEX_MACRO_CONFIG_PREFIX", HighlighterColors.TEXT)
    val MACRO_CONFIG_KEY = key("IMPEX_MACRO_CONFIG_KEY", TemplateColors.TEMPLATE_VARIABLE_ATTRIBUTES)
    val MACRO_USAGE_DEC = key("IMPEX_MACRO_USAGE_DEC", TemplateColors.TEMPLATE_VARIABLE_ATTRIBUTES)
    val ASSIGN_VALUE = key("ASSIGN_VALUE", HighlighterColors.TEXT)
    val HEADER_MODE_INSERT = key("HEADER_MODE_INSERT", DefaultLanguageHighlighterColors.KEYWORD)
    val HEADER_MODE_UPDATE = key("HEADER_MODE_UPDATE", DefaultLanguageHighlighterColors.KEYWORD)
    val HEADER_MODE_INSERT_UPDATE = key("HEADER_MODE_INSERT_UPDATE", DefaultLanguageHighlighterColors.KEYWORD)
    val HEADER_MODE_REMOVE = key("HEADER_MODE_REMOVE", DefaultLanguageHighlighterColors.KEYWORD)
    val HEADER_TYPE = key("HEADER_TYPE", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val VALUE_SUBTYPE = key("VALUE_SUBTYPE", DefaultLanguageHighlighterColors.METADATA)
    val VALUE_SUBTYPE_SAME = key("IMPEX_VALUE_SUBTYPE_SAME", CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES)
    val FIELD_VALUE_SEPARATOR = key("FIELD_VALUE_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)
    val FIELD_LIST_ITEM_SEPARATOR = key("FIELD_LIST_ITEM_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)
    val FIELD_VALUE = key("FIELD_VALUE", HighlighterColors.TEXT)
    val SINGLE_STRING = key("SINGLE_STRING", DefaultLanguageHighlighterColors.STRING)
    val DOUBLE_STRING = key("DOUBLE_STRING", DefaultLanguageHighlighterColors.STRING)
    val FIELD_VALUE_IGNORE = key("FIELD_VALUE_IGNORE", DefaultLanguageHighlighterColors.KEYWORD)
    val FIELD_VALUE_NULL = key("FIELD_VALUE_NULL", FIELD_VALUE_IGNORE)
    val BEAN_SHELL_MARKER = key("BEAN_SHELL_MARKER", DefaultLanguageHighlighterColors.KEYWORD)
    val BEAN_SHELL_BODY = key("BEAN_SHELL_BODY", HighlighterColors.TEXT)
    val SQUARE_BRACKETS = key("SQUARE_BRACKETS", DefaultLanguageHighlighterColors.KEYWORD)
    val ROUND_BRACKETS = key("ROUND_BRACKETS", DefaultLanguageHighlighterColors.KEYWORD)
    val ATTRIBUTE_NAME = key("IMPEX_ATTRIBUTE_NAME", HighlighterColors.TEXT)
    val ATTRIBUTE_VALUE = key("ATTRIBUTE_VALUE", HighlighterColors.TEXT)
    val ATTRIBUTE_SEPARATOR = key("ATTRIBUTE_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)
    val BOOLEAN = key("BOOLEAN", DefaultLanguageHighlighterColors.KEYWORD)
    val DIGIT = key("DIGIT", DefaultLanguageHighlighterColors.NUMBER)
    val ALTERNATIVE_MAP_DELIMITER = key("ALTERNATIVE_MAP_DELIMITER", DefaultLanguageHighlighterColors.KEYWORD)
    val DEFAULT_KEY_VALUE_DELIMITER = key("DEFAULT_KEY_VALUE_DELIMITER", DefaultLanguageHighlighterColors.KEYWORD)
    val DEFAULT_PATH_DELIMITER = key("DEFAULT_PATH_DELIMITER", DefaultLanguageHighlighterColors.KEYWORD)
    val MULTI_LINE_SEPARATOR = key("IMPEX_MULTI_LINE_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)
    val HEADER_PARAMETER_NAME = key("HEADER_PARAMETER_NAME", HighlighterColors.TEXT)
    val HEADER_UNIQUE_PARAMETER_NAME = key("HEADER_UNIQUE_PARAMETER_NAME", HEADER_PARAMETER_NAME)
    val HEADER_SPECIAL_PARAMETER_NAME = key("HEADER_SPECIAL_PARAMETER_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val PARAMETERS_SEPARATOR = key("PARAMETERS_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)
    val COMMA = key("COMMA", DefaultLanguageHighlighterColors.KEYWORD)
    val ALTERNATIVE_PATTERN = key("ALTERNATIVE_PATTERN", DefaultLanguageHighlighterColors.KEYWORD)
    val DOCUMENT_ID = key("DOCUMENT_ID", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val WARNINGS_ATTRIBUTES = key("IMPEX_WARNING_ATTRIBUTES", CodeInsightColors.WARNINGS_ATTRIBUTES)
    val FUNCTION_CALL = key("IMPEX_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    val ATTRIBUTE_HEADER_ABBREVIATION = key("IMPEX_ATTRIBUTE_HEADER_ABBREVIATION", DefaultLanguageHighlighterColors.KEYWORD)
    val USER_RIGHTS = key("IMPEX_USER_RIGHTS", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val USER_RIGHTS_HEADER_PARAMETER = key("IMPEX_USER_RIGHTS_HEADER_PARAMETER", HEADER_PARAMETER_NAME)
    val USER_RIGHTS_HEADER_MANDATORY_PARAMETER = key("IMPEX_USER_RIGHTS_HEADER_MANDATORY_PARAMETER", USER_RIGHTS_HEADER_PARAMETER)
    val USER_RIGHTS_PERMISSION_ALLOWED = key("IMPEX_USER_RIGHTS_PERMISSION_ALLOWED", HighlighterColors.TEXT)
    val USER_RIGHTS_PERMISSION_DENIED = key("IMPEX_USER_RIGHTS_PERMISSION_DENIED", HighlighterColors.TEXT)
    val USER_RIGHTS_PERMISSION_INHERITED = key("IMPEX_USER_RIGHTS_PERMISSION_INHERITED", HighlighterColors.TEXT)
    val VALUE_LINE_ODD = key("IMPEX_VALUE_LINE_ODD", HighlighterColors.NO_HIGHLIGHTING)
    val VALUE_LINE_EVEN = key("IMPEX_VALUE_LINE_EVEN", HighlighterColors.NO_HIGHLIGHTING)
    val COLLECTION_APPEND_PREFIX = key("IMPEX_COLLECTION_APPEND_PREFIX", HighlighterColors.TEXT)
    val COLLECTION_REMOVE_PREFIX = key("IMPEX_COLLECTION_REMOVE_PREFIX", HighlighterColors.TEXT)
    val COLLECTION_MERGE_PREFIX = key("IMPEX_COLLECTION_MERGE_PREFIX", HighlighterColors.TEXT)
    val FIELD_VALUE_PREFIX = key("IMPEX_FIELD_VALUE_PREFIX", HighlighterColors.TEXT)
    val FIELD_VALUE_JAR_PREFIX = key("IMPEX_FIELD_VALUE_JAR_PREFIX", FIELD_VALUE_PREFIX)
    val FIELD_VALUE_EXPLODED_JAR_PREFIX = key("IMPEX_FIELD_VALUE_EXPLODED_JAR_PREFIX", FIELD_VALUE_PREFIX)
    val FIELD_VALUE_FILE_PREFIX = key("IMPEX_FIELD_VALUE_FILE_PREFIX", FIELD_VALUE_PREFIX)
    val FIELD_VALUE_ZIP_PREFIX = key("IMPEX_FIELD_VALUE_ZIP_PREFIX", FIELD_VALUE_PREFIX)
    val FIELD_VALUE_HTTP_PREFIX = key("IMPEX_FIELD_VALUE_HTTP_PREFIX", FIELD_VALUE_PREFIX)

    private fun key(externalName: String, fallbackAttributeKey: TextAttributesKey) = TextAttributesKey.createTextAttributesKey(externalName, fallbackAttributeKey)
}
