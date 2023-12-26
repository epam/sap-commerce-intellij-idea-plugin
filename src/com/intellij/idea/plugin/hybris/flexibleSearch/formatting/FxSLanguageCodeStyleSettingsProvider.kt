/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider

class FxSLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage() = FlexibleSearchLanguage

    override fun customizeSettings(
        consumer: CodeStyleSettingsCustomizable,
        settingsType: SettingsType
    ) {
        val styleOptions = CodeStyleSettingsCustomizableOptions.getInstance()

        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> {
                spacingSettings(styleOptions).forEach { (fieldName, group) ->
                    consumer.showCustomOption(
                        FxSCodeStyleSettings::class.java,
                        fieldName,
                        HybrisI18NBundleUtils.message("hybris.style.settings.project.fxs.$fieldName.name"),
                        group
                    )
                }
            }


            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {
                indentSettings().forEach { (fieldName, group) ->
                    consumer.showCustomOption(
                        FxSCodeStyleSettings::class.java,
                        fieldName,
                        HybrisI18NBundleUtils.message("hybris.style.settings.project.fxs.$fieldName.name"),
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
        "SPACES_INSIDE_DOUBLE_BRACES" to styleOptions.SPACES_AROUND_OPERATORS,
        "SPACES_INSIDE_BRACKETS" to styleOptions.SPACES_AROUND_OPERATORS,
    )

    private fun indentSettings() = mapOf(
        "WRAP_CASE" to "Case Expression",
        "WRAP_CASE_THEN" to "Case Expression",
        "WRAP_CASE_WHEN" to "Case Expression",
        "WRAP_CASE_ELSE" to "Case Expression",
        "WRAP_COMPOUND_OPERATOR" to "Joins",
        "WRAP_JOIN_CONSTRAINT" to "Joins",
        "WRAP_SELECT_STATEMENT_IN_SUBQUERY" to "Clauses",
        "WRAP_FROM_CLAUSE" to "Clauses",
        "WRAP_WHERE_CLAUSE" to "Clauses",
        "WRAP_ORDER_CLAUSE" to "Clauses",
        "WRAP_GROUP_BY_CLAUSE" to "Clauses",
        "WRAP_HAVING_CLAUSE" to "Clauses",
        "WRAP_DBRACES" to "Braces",
    )

    override fun getCodeSample(settingsType: SettingsType) = """
SELECT DISTINCT * FROM {Category AS c} WHERE NOT EXISTS (
    {{
      SELECT *
      FROM {CategoryCategoryRelation}
      WHERE {target}={c:pk}
            and {c.name[en]:o} is not null
    }}
)
"""

}
