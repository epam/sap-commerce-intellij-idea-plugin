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

    override fun getTokenHighlights(tokenType: IElementType) = cache[tokenType]
        ?: emptyArray()

    companion object {
        fun getInstance(): DefaultImpexSyntaxHighlighter = ApplicationManager.getApplication().getService(DefaultImpexSyntaxHighlighter::class.java)

        private val USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS = pack(ImpexHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER)
        private val SCRIPT_MARKER_KEYS = pack(ImpexHighlighterColors.SCRIPT_MARKER)
        private val SQUARE_BRACKETS_KEYS = pack(ImpexHighlighterColors.SQUARE_BRACKETS)
        private val ROUND_BRACKETS_KEYS = pack(ImpexHighlighterColors.ROUND_BRACKETS)
        private val USER_RIGHTS_KEYS = pack(ImpexHighlighterColors.USER_RIGHTS)

        private val cache: Map<IElementType, Array<TextAttributesKey>> = mapOf(
            TokenType.BAD_CHARACTER to pack(HighlighterColors.BAD_CHARACTER),

            ImpexTypes.FIELD_VALUE_JAR_PREFIX to pack(ImpexHighlighterColors.FIELD_VALUE_JAR_PREFIX),
            ImpexTypes.FIELD_VALUE_EXPLODED_JAR_PREFIX to pack(ImpexHighlighterColors.FIELD_VALUE_EXPLODED_JAR_PREFIX),
            ImpexTypes.FIELD_VALUE_FILE_PREFIX to pack(ImpexHighlighterColors.FIELD_VALUE_FILE_PREFIX),
            ImpexTypes.FIELD_VALUE_ZIP_PREFIX to pack(ImpexHighlighterColors.FIELD_VALUE_ZIP_PREFIX),
            ImpexTypes.FIELD_VALUE_HTTP_PREFIX to pack(ImpexHighlighterColors.FIELD_VALUE_HTTP_PREFIX),
            ImpexTypes.FIELD_VALUE_PASSWORD_ENCODING_PREFIX to pack(ImpexHighlighterColors.FIELD_VALUE_PASSWORD_ENCODING_PREFIX),

            ImpexTypes.COLLECTION_APPEND_PREFIX to pack(ImpexHighlighterColors.COLLECTION_APPEND_PREFIX),
            ImpexTypes.COLLECTION_REMOVE_PREFIX to pack(ImpexHighlighterColors.COLLECTION_REMOVE_PREFIX),
            ImpexTypes.COLLECTION_MERGE_PREFIX to pack(ImpexHighlighterColors.COLLECTION_MERGE_PREFIX),

            ImpexTypes.PERMISSION_ALLOWED to pack(ImpexHighlighterColors.USER_RIGHTS_PERMISSION_ALLOWED),
            ImpexTypes.PERMISSION_DENIED to pack(ImpexHighlighterColors.USER_RIGHTS_PERMISSION_DENIED),

            ImpexTypes.TYPE to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpexTypes.PASSWORD to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpexTypes.UID to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpexTypes.MEMBEROFGROUPS to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpexTypes.TARGET to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,

            ImpexTypes.MULTILINE_SEPARATOR to pack(ImpexHighlighterColors.MULTI_LINE_SEPARATOR),
            ImpexTypes.ALTERNATIVE_MAP_DELIMITER to pack(ImpexHighlighterColors.ALTERNATIVE_MAP_DELIMITER),
            ImpexTypes.DEFAULT_KEY_VALUE_DELIMITER to pack(ImpexHighlighterColors.DEFAULT_KEY_VALUE_DELIMITER),
            ImpexTypes.ASSIGN_VALUE to pack(ImpexHighlighterColors.ASSIGN_VALUE),
            ImpexTypes.ATTRIBUTE_NAME to pack(ImpexHighlighterColors.ATTRIBUTE_NAME),
            ImpexTypes.ATTRIBUTE_SEPARATOR to pack(ImpexHighlighterColors.ATTRIBUTE_SEPARATOR),
            ImpexTypes.ATTRIBUTE_VALUE to pack(ImpexHighlighterColors.ATTRIBUTE_VALUE),

            ImpexTypes.BEAN_SHELL_MARKER to SCRIPT_MARKER_KEYS,
            ImpexTypes.GROOVY_MARKER to SCRIPT_MARKER_KEYS,
            ImpexTypes.JAVASCRIPT_MARKER to SCRIPT_MARKER_KEYS,
            ImpexTypes.SCRIPT_ACTION to pack(ImpexHighlighterColors.SCRIPT_ACTION),

            ImpexTypes.BOOLEAN to pack(ImpexHighlighterColors.BOOLEAN),
            ImpexTypes.COMMA to pack(ImpexHighlighterColors.COMMA),
            ImpexTypes.LINE_COMMENT to pack(ImpexHighlighterColors.PROPERTY_COMMENT),
            ImpexTypes.DEFAULT_PATH_DELIMITER to pack(ImpexHighlighterColors.DEFAULT_PATH_DELIMITER),
            ImpexTypes.DIGIT to pack(ImpexHighlighterColors.DIGIT),
            ImpexTypes.DOUBLE_STRING to pack(ImpexHighlighterColors.DOUBLE_STRING),

            ImpexTypes.FIELD_LIST_ITEM_SEPARATOR to pack(ImpexHighlighterColors.FIELD_LIST_ITEM_SEPARATOR),
            ImpexTypes.FIELD_VALUE to pack(ImpexHighlighterColors.FIELD_VALUE),
            ImpexTypes.FIELD_VALUE_IGNORE to pack(ImpexHighlighterColors.FIELD_VALUE_IGNORE),
            ImpexTypes.FIELD_VALUE_NULL to pack(ImpexHighlighterColors.FIELD_VALUE_IGNORE),
            ImpexTypes.FIELD_VALUE_SEPARATOR to pack(ImpexHighlighterColors.FIELD_VALUE_SEPARATOR),

            ImpexTypes.HEADER_MODE_INSERT to pack(ImpexHighlighterColors.HEADER_MODE_INSERT),
            ImpexTypes.HEADER_MODE_INSERT_UPDATE to pack(ImpexHighlighterColors.HEADER_MODE_INSERT_UPDATE),
            ImpexTypes.HEADER_MODE_REMOVE to pack(ImpexHighlighterColors.HEADER_MODE_REMOVE),
            ImpexTypes.HEADER_MODE_UPDATE to pack(ImpexHighlighterColors.HEADER_MODE_UPDATE),
            ImpexTypes.HEADER_PARAMETER_NAME to pack(ImpexHighlighterColors.HEADER_PARAMETER_NAME),
            ImpexTypes.HEADER_SPECIAL_PARAMETER_NAME to pack(ImpexHighlighterColors.HEADER_SPECIAL_PARAMETER_NAME),
            ImpexTypes.HEADER_TYPE to pack(ImpexHighlighterColors.HEADER_TYPE),

            ImpexTypes.MACRO_NAME_DECLARATION to pack(ImpexHighlighterColors.MACRO_NAME_DECLARATION),
            ImpexTypes.MACRO_USAGE to pack(ImpexHighlighterColors.MACRO_USAGE),
            ImpexTypes.MACRO_VALUE to pack(ImpexHighlighterColors.MACRO_VALUE),

            ImpexTypes.PARAMETERS_SEPARATOR to pack(ImpexHighlighterColors.PARAMETERS_SEPARATOR),
            ImpexTypes.SINGLE_STRING to pack(ImpexHighlighterColors.SINGLE_STRING),
            ImpexTypes.VALUE_SUBTYPE to pack(ImpexHighlighterColors.VALUE_SUBTYPE),
            ImpexTypes.ALTERNATIVE_PATTERN to pack(ImpexHighlighterColors.ALTERNATIVE_PATTERN),
            ImpexTypes.DOCUMENT_ID to pack(ImpexHighlighterColors.DOCUMENT_ID),
            ImpexTypes.FUNCTION to pack(ImpexHighlighterColors.FUNCTION_CALL),

            ImpexTypes.LEFT_ROUND_BRACKET to ROUND_BRACKETS_KEYS,
            ImpexTypes.RIGHT_ROUND_BRACKET to ROUND_BRACKETS_KEYS,

            ImpexTypes.LEFT_SQUARE_BRACKET to SQUARE_BRACKETS_KEYS,
            ImpexTypes.RIGHT_SQUARE_BRACKET to SQUARE_BRACKETS_KEYS,

            ImpexTypes.START_USERRIGHTS to USER_RIGHTS_KEYS,
            ImpexTypes.END_USERRIGHTS to USER_RIGHTS_KEYS,

            ImpexTypes.PERMISSION to pack(ImpexHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
        )
    }

}
