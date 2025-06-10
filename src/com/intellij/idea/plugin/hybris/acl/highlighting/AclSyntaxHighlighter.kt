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
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

@Service
class AclSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer() = AclLexer

    override fun getTokenHighlights(tokenType: IElementType) = cache[tokenType]
        ?: emptyArray()

    companion object {
        fun getInstance(): AclSyntaxHighlighter = ApplicationManager.getApplication().getService(AclSyntaxHighlighter::class.java)

        private val USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS = pack(AclHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER)
        private val USER_RIGHTS_KEYS = pack(AclHighlighterColors.USER_RIGHTS)

        private val cache: Map<IElementType, Array<TextAttributesKey>> = mapOf(
            TokenType.BAD_CHARACTER to pack(HighlighterColors.BAD_CHARACTER),

            AclTypes.TYPE to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.PASSWORD to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.UID to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.MEMBEROFGROUPS to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,
            AclTypes.TARGET to USER_RIGHTS_HEADER_MANDATORY_PARAMETER_KEYS,

            AclTypes.LINE_COMMENT to pack(AclHighlighterColors.COMMENT),

            AclTypes.STRING to pack(AclHighlighterColors.SINGLE_STRING),

            ImpexTypes.PERMISSION_ALLOWED to pack(AclHighlighterColors.USER_RIGHTS_PERMISSION_ALLOWED),
            ImpexTypes.PERMISSION_DENIED to pack(AclHighlighterColors.USER_RIGHTS_PERMISSION_DENIED),

            AclTypes.START_USERRIGHTS to USER_RIGHTS_KEYS,
            AclTypes.END_USERRIGHTS to USER_RIGHTS_KEYS,

            AclTypes.PERMISSION to pack(AclHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
        )
    }

}
