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
package com.intellij.idea.plugin.hybris.flexibleSearch.formatting

import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage.Companion.INSTANCE
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider

class FxSLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage() = FlexibleSearchLanguage.INSTANCE

    override fun customizeSettings(
        consumer: CodeStyleSettingsCustomizable,
        settingsType: SettingsType
    ) {
        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> {
                val styleOptions = CodeStyleSettingsCustomizableOptions.getInstance()
                consumer.showCustomOption(
                    FxSCodeStyleSettings::class.java,
                    "SPACE_AROUND_OP",
                    "Around comparison operator",
                    styleOptions.SPACES_AROUND_OPERATORS
                )
                consumer.showCustomOption(
                    FxSCodeStyleSettings::class.java,
                    "SPACES_INSIDE_BRACES",
                    "Inside braces",
                    styleOptions.SPACES_AROUND_OPERATORS
                )
                consumer.showCustomOption(
                    FxSCodeStyleSettings::class.java,
                    "SPACES_INSIDE_DOUBLE_BRACES",
                    "Inside double braces",
                    styleOptions.SPACES_AROUND_OPERATORS
                )
                consumer.showCustomOption(
                    FxSCodeStyleSettings::class.java,
                    "SPACES_INSIDE_BRACKETS",
                    "Inside brackets",
                    styleOptions.SPACES_AROUND_OPERATORS
                )
            }

            SettingsType.BLANK_LINES_SETTINGS -> {
                consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
            }

            else -> Unit
        }
    }

    override fun getCodeSample(settingsType: SettingsType) = """
SELECT DISTINCT * FROM {Category AS c} WHERE NOT EXISTS (
    {{
      SELECT *
      FROM {CategoryCategoryRelation}
      WHERE {target}={c:pk}
//            and {
    }}
)
"""

}
