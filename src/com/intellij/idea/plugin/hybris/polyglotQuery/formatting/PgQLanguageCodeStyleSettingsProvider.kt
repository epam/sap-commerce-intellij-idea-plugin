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
package com.intellij.idea.plugin.hybris.polyglotQuery.formatting

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider

class PgQLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage() = PolyglotQueryLanguage

    override fun customizeSettings(
        consumer: CodeStyleSettingsCustomizable,
        settingsType: SettingsType
    ) {
        val styleOptions = CodeStyleSettingsCustomizableOptions.getInstance()

        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> {
                spacingSettings(styleOptions).forEach { (fieldName, group) ->
                    consumer.showCustomOption(
                        PgQCodeStyleSettings::class.java,
                        fieldName,
                        message("hybris.style.settings.project.pgq.$fieldName.name"),
                        group
                    )
                }
            }


            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {
                indentSettings().forEach { (fieldName, group) ->
                    consumer.showCustomOption(
                        PgQCodeStyleSettings::class.java,
                        fieldName,
                        message("hybris.style.settings.project.pgq.$fieldName.name"),
                        group
                    )
                }
            }

            SettingsType.BLANK_LINES_SETTINGS -> {
                consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
            }

            else -> Unit
        }
    }

    private fun spacingSettings(styleOptions: CodeStyleSettingsCustomizableOptions) = mapOf(
        "SPACE_AROUND_OP" to styleOptions.SPACES_AROUND_OPERATORS,
        "SPACES_INSIDE_BRACES" to styleOptions.SPACES_AROUND_OPERATORS,
        "SPACES_INSIDE_BRACKETS" to styleOptions.SPACES_AROUND_OPERATORS,
    )

    private fun indentSettings() = mapOf(
        "WRAP_WHERE_CLAUSE" to "Clauses",
        "WRAP_ORDER_CLAUSE" to "Clauses",
    )

    override fun getCodeSample(settingsType: SettingsType) = """
GET {Title}
WHERE { code  } =?code1
OR {code} = ?code2
AND {wrong} IS NOT NULL
ORDER BY {code[ en  ]} DESC
"""

}
