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

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object AclHighlighterColors {

    val COMMENT = key("LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val DOT = key("DOT", HighlighterColors.TEXT)
    val SINGLE_STRING = key("SINGLE_STRING", DefaultLanguageHighlighterColors.STRING)

    val FIELD_VALUE_SEPARATOR = key("FIELD_VALUE_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)
    val PARAMETERS_SEPARATOR = key("PARAMETERS_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD)
    val DUMMY_SEPARATOR = key("DUMMY_SEPARATOR", DefaultLanguageHighlighterColors.LINE_COMMENT)

    val FIELD_VALUE_TYPE = key("FIELD_VALUE_TYPE", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val FIELD_VALUE_TARGET_TYPE = key("FIELD_VALUE_TARGET_TYPE", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    val FIELD_VALUE_TARGET_ATTRIBUTE = key("FIELD_VALUE_TARGET_ATTRIBUTE", DefaultLanguageHighlighterColors.STATIC_FIELD)

    val USER_RIGHTS = key("ACL_USER_RIGHTS", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val USER_RIGHTS_HEADER_PARAMETER = key("ACL_USER_RIGHTS_HEADER_PARAMETER", HighlighterColors.TEXT)
    val USER_RIGHTS_HEADER_MANDATORY_PARAMETER = key("ACL_USER_RIGHTS_HEADER_MANDATORY_PARAMETER", USER_RIGHTS_HEADER_PARAMETER)
    val USER_RIGHTS_PERMISSION_GRANTED = key("ACL_USER_RIGHTS_PERMISSION_GRANTED", HighlighterColors.TEXT)
    val USER_RIGHTS_PERMISSION_DENIED = key("ACL_USER_RIGHTS_PERMISSION_DENIED", HighlighterColors.TEXT)
    val USER_RIGHTS_PERMISSION_INHERITED = key("ACL_USER_RIGHTS_PERMISSION_INHERITED", HighlighterColors.TEXT)

    val USER_RIGHTS_VALUE_LINE_TYPE = key("ACL_USER_RIGHTS_VALUE_LINE_TYPE", HighlighterColors.TEXT)

    private fun key(externalName: String, fallbackAttributeKey: TextAttributesKey) = TextAttributesKey.createTextAttributesKey(externalName, fallbackAttributeKey)
}
