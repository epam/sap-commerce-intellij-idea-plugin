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
package com.intellij.idea.plugin.hybris.acl.highlighting

import com.intellij.idea.plugin.hybris.acl.AclLexer
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.util.application

@Service
class AclSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer() = AclLexer()

    override fun getTokenHighlights(tokenType: IElementType) = cache[tokenType]
        ?: emptyArray()

    companion object {
        fun getInstance(): AclSyntaxHighlighter = application.service()

        private val USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS = pack(AclHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER)
        private val USER_RIGHTS_KEYS = pack(AclHighlighterColors.USER_RIGHTS)

        private val cache: Map<IElementType, Array<TextAttributesKey>> = mapOf(
            TokenType.BAD_CHARACTER to pack(HighlighterColors.BAD_CHARACTER),

            AclTypes.HEADER_TYPE to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.HEADER_PASSWORD to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.HEADER_UID to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.HEADER_MEMBEROFGROUPS to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.HEADER_TARGET to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,

            AclTypes.HEADER_READ to pack(AclHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
            AclTypes.HEADER_CHANGE to pack(AclHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
            AclTypes.HEADER_CREATE to pack(AclHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
            AclTypes.HEADER_REMOVE to pack(AclHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
            AclTypes.HEADER_CHANGE_PERM to pack(AclHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),

            AclTypes.USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_AWARE to pack(AclHighlighterColors.USER_RIGHTS_VALUE_LINE_TYPE),
            AclTypes.USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_UNAWARE to pack(AclHighlighterColors.USER_RIGHTS_VALUE_LINE_TYPE),

            AclTypes.LINE_COMMENT to pack(AclHighlighterColors.COMMENT),

            AclTypes.PASSWORD to pack(AclHighlighterColors.SINGLE_STRING),

            AclTypes.FIELD_VALUE_SEPARATOR to pack(AclHighlighterColors.FIELD_VALUE_SEPARATOR),
            AclTypes.FIELD_VALUE_TYPE_SEPARATOR to pack(AclHighlighterColors.FIELD_VALUE_SEPARATOR),
            AclTypes.PARAMETERS_SEPARATOR to pack(AclHighlighterColors.PARAMETERS_SEPARATOR),
            AclTypes.DUMMY_SEPARATOR to pack(AclHighlighterColors.DUMMY_SEPARATOR),

            AclTypes.FIELD_VALUE_TYPE to pack(AclHighlighterColors.FIELD_VALUE_TYPE),
            AclTypes.FIELD_VALUE_TARGET_TYPE to pack(AclHighlighterColors.FIELD_VALUE_TARGET_TYPE),
            AclTypes.FIELD_VALUE_TARGET_ATTRIBUTE to pack(AclHighlighterColors.FIELD_VALUE_TARGET_ATTRIBUTE),

            AclTypes.PERMISSION_GRANTED to pack(AclHighlighterColors.USER_RIGHTS_PERMISSION_GRANTED),
            AclTypes.PERMISSION_DENIED to pack(AclHighlighterColors.USER_RIGHTS_PERMISSION_DENIED),
            AclTypes.PERMISSION_INHERITED to pack(AclHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED),

            AclTypes.START_USERRIGHTS to USER_RIGHTS_KEYS,
            AclTypes.END_USERRIGHTS to USER_RIGHTS_KEYS,
        )
    }

}
