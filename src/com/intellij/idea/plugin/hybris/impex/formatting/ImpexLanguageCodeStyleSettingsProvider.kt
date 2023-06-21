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

package com.intellij.idea.plugin.hybris.impex.formatting

import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider

class ImpexLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage(): Language = ImpexLanguage.getInstance()

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> applySpacingSettings(consumer)
            SettingsType.BLANK_LINES_SETTINGS ->  applyBlankLineSettings(consumer)
            else -> return
        }
    }

    private fun applySpacingSettings(consumer: CodeStyleSettingsCustomizable) {
        val styleOptions = CodeStyleSettingsCustomizableOptions.getInstance()
        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "TABLIFY",
            "Formatting in table-like style",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_FIELD_VALUE_SEPARATOR",
            "After field value separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_FIELD_VALUE_SEPARATOR",
            "Before field value separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_PARAMETERS_SEPARATOR",
            "After parameters separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_PARAMETERS_SEPARATOR",
            "Before parameters separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_COMMA",
            "After comma",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_COMMA",
            "Before comma",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_ATTRIBUTE_SEPARATOR",
            "After attribute separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_ATTRIBUTE_SEPARATOR",
            "Before attribute separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_FIELD_LIST_ITEM_SEPARATOR",
            "After list item separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_FIELD_LIST_ITEM_SEPARATOR",
            "Before list item separator",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_ASSIGN_VALUE",
            "After assign value",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_ASSIGN_VALUE",
            "Before assign value",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_LEFT_ROUND_BRACKET",
            "After left round bracket",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_RIGHT_ROUND_BRACKET",
            "Before right round bracket",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_LEFT_SQUARE_BRACKET",
            "After left square bracket",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_RIGHT_SQUARE_BRACKET",
            "Before right square bracket",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_AFTER_ALTERNATIVE_PATTERN",
            "After alternative pattern",
            styleOptions.SPACES_AROUND_OPERATORS
        )

        consumer.showCustomOption(
            ImpexCodeStyleSettings::class.java,
            "SPACE_BEFORE_ALTERNATIVE_PATTERN",
            "Before alternative pattern",
            styleOptions.SPACES_AROUND_OPERATORS
        )
    }

    private fun applyBlankLineSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
    }

    override fun getCodeSample(settingsType: SettingsType): String {
        val lang = "\$lang"
        val contentCatalog = "\$contentCatalog"
        val contentCV = "\$contentCV"
        val macro = "\$macro"
        val contentResource = "\$contentResource"
        return """
            # Comment
            $lang = en
            $contentCatalog = projectContentCatalog
            $contentCV = catalogVersion(CatalogVersion.catalog(Catalog.id[default = \$contentCatalog]),CatalogVersion.version[default = 'Staged'])[default = \$contentCatalog:Staged]
            $macro = qwe;qwe,qwe,;qwe
        
            #% impex.setLocale( Locale.GERMAN );
        
            INSERT_UPDATE SomeType ; \$contentCV[unique = true][map-delimiter = |][dateformat = yyyy-MM-dd HH:mm:ss] ; uid[unique = true]          ; title[lang = \$lang]
            Subtype                ;                                                                                ; account                     ; "Your Account"
                                   ;                                                                                ; <ignore>                    ; "Add/Edit Address"
                                   ;                                                                                ; key -> value | key -> value ; "Address Book"
                                   ;                                                                                ; value1, value2, value3      ; 12345               ;
        
            INSERT Address[impex.legacy.mode = true, batchmode = true] ; firstname ; owner(Principal.uid | AbstractOrder.code)
                                                                       ; Hans      ; admin
        
            UPDATE Address ; firstname ; owner(Principal.uid | AbstractOrder.code) ; &docId
                           ; Hans      ; admin                                     ; id
        
            remove Address ; firstname ; owner(Principal.uid | AbstractOrder.code)
                           ; Hans      ; admin
        
            INSERT_UPDATE Media ; @media[translator = de.hybris.platform.impex.jalo.media.MediaDataTranslato r] ; mime[default = 'image/png']
                                ;                                                                               ; $contentResource/images/logo .png
            """.trimIndent()
    }
}