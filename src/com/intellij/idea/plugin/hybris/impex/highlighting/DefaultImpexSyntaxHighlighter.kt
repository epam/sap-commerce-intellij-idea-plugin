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

import com.intellij.idea.plugin.hybris.impex.ImpexLexerAdapter
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

@Service
class DefaultImpexSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer() = ImpexLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            ImpexTypes.MULTILINE_SEPARATOR -> MULTI_LINE_SEPARATOR
            ImpexTypes.ALTERNATIVE_MAP_DELIMITER -> ALTERNATIVE_MAP_DELIMITER_KEYS
            ImpexTypes.DEFAULT_KEY_VALUE_DELIMITER -> DEFAULT_KEY_VALUE_DELIMITER_KEYS
            ImpexTypes.ASSIGN_VALUE -> ASSIGN_VALUE_KEYS
            ImpexTypes.ATTRIBUTE_NAME -> ATTRIBUTE_NAME_KEYS
            ImpexTypes.ATTRIBUTE_SEPARATOR -> ATTRIBUTE_SEPARATOR_KEYS
            ImpexTypes.ATTRIBUTE_VALUE -> ATTRIBUTE_VALUE_KEYS
            ImpexTypes.BEAN_SHELL_BODY -> BEAN_SHELL_BODY_KEYS
            ImpexTypes.BEAN_SHELL_MARKER -> BEAN_SHELL_MARKER_KEYS
            ImpexTypes.BOOLEAN -> BOOLEAN_KEYS
            ImpexTypes.COMMA -> COMMA_KEYS
            ImpexTypes.LINE_COMMENT -> PROPERTY_COMMENT_KEYS
            ImpexTypes.DEFAULT_PATH_DELIMITER -> DEFAULT_PATH_DELIMITER_KEYS
            ImpexTypes.DIGIT -> DIGIT_KEYS
            ImpexTypes.DOUBLE_STRING -> DOUBLE_STRING_KEYS
            ImpexTypes.FIELD_LIST_ITEM_SEPARATOR -> FIELD_LIST_ITEM_SEPARATOR_KEYS
            ImpexTypes.FIELD_VALUE -> FIELD_VALUE_KEYS
            ImpexTypes.FIELD_VALUE_IGNORE -> FIELD_VALUE_IGNORE_KEYS
            ImpexTypes.FIELD_VALUE_NULL -> FIELD_VALUE_NULL_KEYS
            ImpexTypes.FIELD_VALUE_SEPARATOR -> FIELD_VALUE_SEPARATOR_KEYS
            ImpexTypes.HEADER_MODE_INSERT -> HEADER_MODE_INSERT_KEYS
            ImpexTypes.HEADER_MODE_INSERT_UPDATE -> HEADER_MODE_INSERT_UPDATE_KEYS
            ImpexTypes.HEADER_MODE_REMOVE -> HEADER_MODE_REMOVE_KEYS
            ImpexTypes.HEADER_MODE_UPDATE -> HEADER_MODE_UPDATE_KEYS
            ImpexTypes.HEADER_PARAMETER_NAME -> HEADER_PARAMETER_NAME_KEYS
            ImpexTypes.HEADER_SPECIAL_PARAMETER_NAME -> HEADER_SPECIAL_PARAMETER_NAME_KEYS
            ImpexTypes.HEADER_TYPE -> HEADER_TYPE_KEYS
            ImpexTypes.MACRO_NAME_DECLARATION -> MACRO_NAME_DECLARATION_KEYS
            ImpexTypes.MACRO_USAGE -> MACRO_USAGE_KEYS
            ImpexTypes.MACRO_VALUE -> MACRO_VALUE_KEYS
            ImpexTypes.PARAMETERS_SEPARATOR -> PARAMETERS_SEPARATOR_KEYS
            ImpexTypes.SINGLE_STRING -> SINGLE_STRING_KEYS
            ImpexTypes.VALUE_SUBTYPE -> VALUE_SUBTYPE_KEYS
            ImpexTypes.ALTERNATIVE_PATTERN -> ALTERNATIVE_PATTERN_KEYS
            ImpexTypes.DOCUMENT_ID -> DOCUMENT_ID_KEYS
            ImpexTypes.FUNCTION -> FUNCTION_KEYS

            ImpexTypes.LEFT_ROUND_BRACKET,
            ImpexTypes.RIGHT_ROUND_BRACKET -> ROUND_BRACKETS_KEYS

            ImpexTypes.LEFT_SQUARE_BRACKET,
            ImpexTypes.RIGHT_SQUARE_BRACKET -> SQUARE_BRACKETS_KEYS

            ImpexTypes.START_USERRIGHTS,
            ImpexTypes.END_USERRIGHTS -> USER_RIGHTS_KEYS

            ImpexTypes.PERMISSION -> USER_RIGHTS_HEADER_PERMISSION_PARAMETER_KEYS

            ImpexTypes.TYPE,
            ImpexTypes.PASSWORD,
            ImpexTypes.UID,
            ImpexTypes.MEMBEROFGROUPS,
            ImpexTypes.TARGET -> USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS

            ImpexTypes.PERMISSION_ALLOWED -> USER_RIGHTS_PERMISSION_ALLOWED_KEYS
            ImpexTypes.PERMISSION_DENIED -> USER_RIGHTS_PERMISSION_DENIED_KEYS

            ImpexTypes.COLLECTION_APPEND_PREFIX -> COLLECTION_APPEND_PREFIX
            ImpexTypes.COLLECTION_REMOVE_PREFIX -> COLLECTION_REMOVE_PREFIX
            ImpexTypes.COLLECTION_MERGE_PREFIX -> COLLECTION_MERGE_PREFIX

            ImpexTypes.FIELD_VALUE_JAR_PREFIX -> FIELD_VALUE_JAR_PREFIX
            ImpexTypes.FIELD_VALUE_EXPLODED_JAR_PREFIX -> FIELD_VALUE_EXPLODED_JAR_PREFIX
            ImpexTypes.FIELD_VALUE_FILE_PREFIX -> FIELD_VALUE_FILE_PREFIX
            ImpexTypes.FIELD_VALUE_ZIP_PREFIX -> FIELD_VALUE_ZIP_PREFIX
            ImpexTypes.FIELD_VALUE_HTTP_PREFIX -> FIELD_VALUE_HTTP_PREFIX
            ImpexTypes.FIELD_VALUE_PASSWORD_ENCODING_PREFIX -> FIELD_VALUE_PASSWORD_ENCODING_PREFIX

            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS
            else -> EMPTY_KEYS
        }
    }

    companion object {
        fun getInstance(): DefaultImpexSyntaxHighlighter = ApplicationManager.getApplication().getService(DefaultImpexSyntaxHighlighter::class.java)

        val PROPERTY_COMMENT_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.PROPERTY_COMMENT)
        val COLLECTION_APPEND_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.COLLECTION_APPEND_PREFIX)
        val COLLECTION_REMOVE_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.COLLECTION_REMOVE_PREFIX)
        val COLLECTION_MERGE_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.COLLECTION_MERGE_PREFIX)
        val FIELD_VALUE_JAR_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_JAR_PREFIX)
        val FIELD_VALUE_EXPLODED_JAR_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_EXPLODED_JAR_PREFIX)
        val FIELD_VALUE_FILE_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_FILE_PREFIX)
        val FIELD_VALUE_ZIP_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_ZIP_PREFIX)
        val FIELD_VALUE_HTTP_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_HTTP_PREFIX)
        val FIELD_VALUE_PASSWORD_ENCODING_PREFIX: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_PASSWORD_ENCODING_PREFIX)
        val MACRO_NAME_DECLARATION_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.MACRO_NAME_DECLARATION)
        val MACRO_VALUE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.MACRO_VALUE)
        val MACRO_USAGE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.MACRO_USAGE)
        val ASSIGN_VALUE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.ASSIGN_VALUE)
        val HEADER_MODE_INSERT_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.HEADER_MODE_INSERT)
        val HEADER_MODE_UPDATE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.HEADER_MODE_UPDATE)
        val HEADER_MODE_INSERT_UPDATE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.HEADER_MODE_INSERT_UPDATE)
        val HEADER_MODE_REMOVE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.HEADER_MODE_REMOVE)
        val HEADER_TYPE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.HEADER_TYPE)
        val FUNCTION_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FUNCTION_CALL)
        val VALUE_SUBTYPE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.VALUE_SUBTYPE)
        val FIELD_VALUE_SEPARATOR_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_SEPARATOR)
        val FIELD_LIST_ITEM_SEPARATOR_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_LIST_ITEM_SEPARATOR)
        val FIELD_VALUE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE)
        val SINGLE_STRING_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.SINGLE_STRING)
        val DOUBLE_STRING_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.DOUBLE_STRING)
        val FIELD_VALUE_IGNORE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_IGNORE)
        val FIELD_VALUE_NULL_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.FIELD_VALUE_IGNORE)
        val BEAN_SHELL_MARKER_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.BEAN_SHELL_MARKER)
        val BEAN_SHELL_BODY_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.BEAN_SHELL_BODY)
        val SQUARE_BRACKETS_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.SQUARE_BRACKETS)
        val ROUND_BRACKETS_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.ROUND_BRACKETS)
        val ATTRIBUTE_NAME_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.ATTRIBUTE_NAME)
        val ATTRIBUTE_VALUE_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.ATTRIBUTE_VALUE)
        val ATTRIBUTE_SEPARATOR_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.ATTRIBUTE_SEPARATOR)
        val BOOLEAN_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.BOOLEAN)
        val DIGIT_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.DIGIT)
        val MULTI_LINE_SEPARATOR: Array<TextAttributesKey> = pack(ImpexHighlighterColors.MULTI_LINE_SEPARATOR)
        val ALTERNATIVE_MAP_DELIMITER_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.ALTERNATIVE_MAP_DELIMITER)
        val DEFAULT_KEY_VALUE_DELIMITER_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.DEFAULT_KEY_VALUE_DELIMITER)
        val DEFAULT_PATH_DELIMITER_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.DEFAULT_PATH_DELIMITER)
        val HEADER_PARAMETER_NAME_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.HEADER_PARAMETER_NAME)
        val HEADER_SPECIAL_PARAMETER_NAME_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.HEADER_SPECIAL_PARAMETER_NAME)
        val PARAMETERS_SEPARATOR_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.PARAMETERS_SEPARATOR)
        val COMMA_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.COMMA)
        val BAD_CHARACTER_KEYS: Array<TextAttributesKey> = pack(HighlighterColors.BAD_CHARACTER)
        val ALTERNATIVE_PATTERN_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.ALTERNATIVE_PATTERN)
        val DOCUMENT_ID_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.DOCUMENT_ID)
        val USER_RIGHTS_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.USER_RIGHTS)
        val USER_RIGHTS_HEADER_PERMISSION_PARAMETER_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.USER_RIGHTS_HEADER_PARAMETER)
        val USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER)
        val USER_RIGHTS_PERMISSION_ALLOWED_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.USER_RIGHTS_PERMISSION_ALLOWED)
        val USER_RIGHTS_PERMISSION_DENIED_KEYS: Array<TextAttributesKey> = pack(ImpexHighlighterColors.USER_RIGHTS_PERMISSION_DENIED)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

}
