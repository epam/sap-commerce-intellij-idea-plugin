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
package com.intellij.idea.plugin.hybris.acl.formatting

import com.intellij.idea.plugin.hybris.acl.AclLanguage
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider

class AclLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage() = AclLanguage

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> applySpacingSettings(consumer)
            SettingsType.BLANK_LINES_SETTINGS -> applyBlankLineSettings(consumer)
            else -> return
        }
    }

    private fun applySpacingSettings(consumer: CodeStyleSettingsCustomizable) {
        val styleOptions = CodeStyleSettingsCustomizableOptions.getInstance()

        consumer.showCustomOption(
            AclCodeStyleSettings::class.java,
            "SPACE_AFTER_FIELD_VALUE_SEPARATOR",
            "After field value separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            AclCodeStyleSettings::class.java,
            "SPACE_BEFORE_FIELD_VALUE_SEPARATOR",
            "Before field value separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            AclCodeStyleSettings::class.java,
            "SPACE_AFTER_PARAMETERS_SEPARATOR",
            "After parameters separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            AclCodeStyleSettings::class.java,
            "SPACE_BEFORE_PARAMETERS_SEPARATOR",
            "Before parameters separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            AclCodeStyleSettings::class.java,
            "SPACE_AFTER_COMMA",
            "After comma",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            AclCodeStyleSettings::class.java,
            "SPACE_BEFORE_COMMA",
            "Before comma",
            styleOptions.SPACES_AROUND_OPERATORS
        )
    }

    private fun applyBlankLineSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
    }

    override fun getCodeSample(settingsType: SettingsType): String {
        return $$"""
            # Comment
            $START_USERRIGHTS
Type     ; UID         ; MemberOfGroups        ; Password; Target      ; read; change; create; remove; change_perm
UserGroup; newUserGroup; group1, group2, group3;         ;             ;     ;       ;       ;       ;
         ;             ;                       ;         ; Product.code; +   ; +     ; -     ; .     ;
User     ; newUser     ; newUserGroup          ;         ;             ;     ;       ;       ;       ;
         ;             ;                       ;         ; Product.code; .   ; +     ; +     ; .     ; 
$END_USERRIGHTS
            """.trimIndent()
    }
}
