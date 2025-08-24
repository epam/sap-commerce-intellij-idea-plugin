/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package sap.commerce.toolset.impex.highlighting

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.util.application
import sap.commerce.toolset.impex.ImpExLexerAdapter
import sap.commerce.toolset.impex.psi.ImpExTypes

@Service
class ImpExSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer() = ImpExLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType) = cache[tokenType]
        ?: emptyArray()

    companion object {
        fun getInstance(): ImpExSyntaxHighlighter = application.service()

        private val USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS = pack(ImpExHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER)
        private val SCRIPT_MARKER_KEYS = pack(ImpExHighlighterColors.SCRIPT_MARKER)
        private val SQUARE_BRACKETS_KEYS = pack(ImpExHighlighterColors.SQUARE_BRACKETS)
        private val ROUND_BRACKETS_KEYS = pack(ImpExHighlighterColors.ROUND_BRACKETS)
        private val USER_RIGHTS_KEYS = pack(ImpExHighlighterColors.USER_RIGHTS)

        private val cache: Map<IElementType, Array<TextAttributesKey>> = mapOf(
            TokenType.BAD_CHARACTER to pack(HighlighterColors.BAD_CHARACTER),

            ImpExTypes.FIELD_VALUE_JAR_PREFIX to pack(ImpExHighlighterColors.FIELD_VALUE_JAR_PREFIX),
            ImpExTypes.FIELD_VALUE_EXPLODED_JAR_PREFIX to pack(ImpExHighlighterColors.FIELD_VALUE_EXPLODED_JAR_PREFIX),
            ImpExTypes.FIELD_VALUE_FILE_PREFIX to pack(ImpExHighlighterColors.FIELD_VALUE_FILE_PREFIX),
            ImpExTypes.FIELD_VALUE_ZIP_PREFIX to pack(ImpExHighlighterColors.FIELD_VALUE_ZIP_PREFIX),
            ImpExTypes.FIELD_VALUE_HTTP_PREFIX to pack(ImpExHighlighterColors.FIELD_VALUE_HTTP_PREFIX),
            ImpExTypes.FIELD_VALUE_PASSWORD_ENCODING_PREFIX to pack(ImpExHighlighterColors.FIELD_VALUE_PASSWORD_ENCODING_PREFIX),
            ImpExTypes.FIELD_VALUE_SCRIPT_PREFIX to pack(ImpExHighlighterColors.FIELD_VALUE_SCRIPT_PREFIX),

            ImpExTypes.COLLECTION_APPEND_PREFIX to pack(ImpExHighlighterColors.COLLECTION_APPEND_PREFIX),
            ImpExTypes.COLLECTION_REMOVE_PREFIX to pack(ImpExHighlighterColors.COLLECTION_REMOVE_PREFIX),
            ImpExTypes.COLLECTION_MERGE_PREFIX to pack(ImpExHighlighterColors.COLLECTION_MERGE_PREFIX),

            ImpExTypes.PERMISSION_ALLOWED to pack(ImpExHighlighterColors.USER_RIGHTS_PERMISSION_ALLOWED),
            ImpExTypes.PERMISSION_DENIED to pack(ImpExHighlighterColors.USER_RIGHTS_PERMISSION_DENIED),

            ImpExTypes.TYPE to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpExTypes.PASSWORD to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpExTypes.UID to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpExTypes.MEMBEROFGROUPS to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            ImpExTypes.TARGET to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,

            ImpExTypes.MULTILINE_SEPARATOR to pack(ImpExHighlighterColors.MULTI_LINE_SEPARATOR),
            ImpExTypes.ALTERNATIVE_MAP_DELIMITER to pack(ImpExHighlighterColors.ALTERNATIVE_MAP_DELIMITER),
            ImpExTypes.DEFAULT_KEY_VALUE_DELIMITER to pack(ImpExHighlighterColors.DEFAULT_KEY_VALUE_DELIMITER),
            ImpExTypes.ASSIGN_VALUE to pack(ImpExHighlighterColors.ASSIGN_VALUE),
            ImpExTypes.ATTRIBUTE_NAME to pack(ImpExHighlighterColors.ATTRIBUTE_NAME),
            ImpExTypes.ATTRIBUTE_SEPARATOR to pack(ImpExHighlighterColors.ATTRIBUTE_SEPARATOR),
            ImpExTypes.ATTRIBUTE_VALUE to pack(ImpExHighlighterColors.ATTRIBUTE_VALUE),

            ImpExTypes.BEAN_SHELL_MARKER to SCRIPT_MARKER_KEYS,
            ImpExTypes.GROOVY_MARKER to SCRIPT_MARKER_KEYS,
            ImpExTypes.JAVASCRIPT_MARKER to SCRIPT_MARKER_KEYS,
            ImpExTypes.SCRIPT_ACTION to pack(ImpExHighlighterColors.SCRIPT_ACTION),

            ImpExTypes.BOOLEAN to pack(ImpExHighlighterColors.BOOLEAN),
            ImpExTypes.COMMA to pack(ImpExHighlighterColors.COMMA),
            ImpExTypes.LINE_COMMENT to pack(ImpExHighlighterColors.PROPERTY_COMMENT),
            ImpExTypes.DEFAULT_PATH_DELIMITER to pack(ImpExHighlighterColors.DEFAULT_PATH_DELIMITER),
            ImpExTypes.DIGIT to pack(ImpExHighlighterColors.DIGIT),
            ImpExTypes.DOUBLE_STRING to pack(ImpExHighlighterColors.DOUBLE_STRING),

            ImpExTypes.FIELD_LIST_ITEM_SEPARATOR to pack(ImpExHighlighterColors.FIELD_LIST_ITEM_SEPARATOR),
            ImpExTypes.FIELD_VALUE to pack(ImpExHighlighterColors.FIELD_VALUE),
            ImpExTypes.FIELD_VALUE_IGNORE to pack(ImpExHighlighterColors.FIELD_VALUE_IGNORE),
            ImpExTypes.FIELD_VALUE_NULL to pack(ImpExHighlighterColors.FIELD_VALUE_IGNORE),
            ImpExTypes.FIELD_VALUE_SEPARATOR to pack(ImpExHighlighterColors.FIELD_VALUE_SEPARATOR),

            ImpExTypes.HEADER_MODE_INSERT to pack(ImpExHighlighterColors.HEADER_MODE_INSERT),
            ImpExTypes.HEADER_MODE_INSERT_UPDATE to pack(ImpExHighlighterColors.HEADER_MODE_INSERT_UPDATE),
            ImpExTypes.HEADER_MODE_REMOVE to pack(ImpExHighlighterColors.HEADER_MODE_REMOVE),
            ImpExTypes.HEADER_MODE_UPDATE to pack(ImpExHighlighterColors.HEADER_MODE_UPDATE),
            ImpExTypes.HEADER_PARAMETER_NAME to pack(ImpExHighlighterColors.HEADER_PARAMETER_NAME),
            ImpExTypes.HEADER_SPECIAL_PARAMETER_NAME to pack(ImpExHighlighterColors.HEADER_SPECIAL_PARAMETER_NAME),
            ImpExTypes.HEADER_TYPE to pack(ImpExHighlighterColors.HEADER_TYPE),

            ImpExTypes.MACRO_NAME_DECLARATION to pack(ImpExHighlighterColors.MACRO_NAME_DECLARATION),
            ImpExTypes.MACRO_USAGE to pack(ImpExHighlighterColors.MACRO_USAGE),
            ImpExTypes.MACRO_VALUE to pack(ImpExHighlighterColors.MACRO_VALUE),

            ImpExTypes.PARAMETERS_SEPARATOR to pack(ImpExHighlighterColors.PARAMETERS_SEPARATOR),
            ImpExTypes.SINGLE_STRING to pack(ImpExHighlighterColors.SINGLE_STRING),
            ImpExTypes.VALUE_SUBTYPE to pack(ImpExHighlighterColors.VALUE_SUBTYPE),
            ImpExTypes.ALTERNATIVE_PATTERN to pack(ImpExHighlighterColors.ALTERNATIVE_PATTERN),
            ImpExTypes.DOCUMENT_ID to pack(ImpExHighlighterColors.DOCUMENT_ID),
            ImpExTypes.FUNCTION to pack(ImpExHighlighterColors.FUNCTION_CALL),

            ImpExTypes.LEFT_ROUND_BRACKET to ROUND_BRACKETS_KEYS,
            ImpExTypes.RIGHT_ROUND_BRACKET to ROUND_BRACKETS_KEYS,

            ImpExTypes.LEFT_SQUARE_BRACKET to SQUARE_BRACKETS_KEYS,
            ImpExTypes.RIGHT_SQUARE_BRACKET to SQUARE_BRACKETS_KEYS,

            ImpExTypes.START_USERRIGHTS to USER_RIGHTS_KEYS,
            ImpExTypes.END_USERRIGHTS to USER_RIGHTS_KEYS,

            ImpExTypes.PERMISSION to pack(ImpExHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
        )
    }

}
