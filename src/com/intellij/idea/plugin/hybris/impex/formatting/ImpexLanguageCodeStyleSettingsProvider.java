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

package com.intellij.idea.plugin.hybris.impex.formatting;

import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;

public class ImpexLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    @NotNull
    @Override
    public Language getLanguage() {
        return ImpexLanguage.getInstance();
    }

    @Override
    public void customizeSettings(
        @NotNull final CodeStyleSettingsCustomizable consumer,
        @NotNull final SettingsType settingsType
    ) {
        if (SettingsType.SPACING_SETTINGS == settingsType) {
            final var styleOptions = CodeStyleSettingsCustomizableOptions.getInstance();
            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "TABLIFY",
                "Formatting in table-like style",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_FIELD_VALUE_SEPARATOR",
                "After field value separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_FIELD_VALUE_SEPARATOR",
                "Before field value separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_PARAMETERS_SEPARATOR",
                "After parameters separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_PARAMETERS_SEPARATOR",
                "Before parameters separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_COMMA",
                "After comma",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_COMMA",
                "Before comma",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_ATTRIBUTE_SEPARATOR",
                "After attribute separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_ATTRIBUTE_SEPARATOR",
                "Before attribute separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_FIELD_LIST_ITEM_SEPARATOR",
                "After list item separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_FIELD_LIST_ITEM_SEPARATOR",
                "Before list item separator",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_ASSIGN_VALUE",
                "After assign value",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_ASSIGN_VALUE",
                "Before assign value",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_LEFT_ROUND_BRACKET",
                "After left round bracket",
                styleOptions.SPACES_AROUND_OPERATORS
            );

//            consumer.showCustomOption(
//                    ImpexCodeStyleSettings.class,
//                    "SPACE_BEFORE_LEFT_ROUND_BRACKET",
//                    "Before left round bracket",
//                    CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS
//            );

//            consumer.showCustomOption(
//                    ImpexCodeStyleSettings.class,
//                    "SPACE_AFTER_RIGHT_ROUND_BRACKET",
//                    "After right round bracket",
//                    CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS
//            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_RIGHT_ROUND_BRACKET",
                "Before right round bracket",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_LEFT_SQUARE_BRACKET",
                "After left square bracket",
                styleOptions.SPACES_AROUND_OPERATORS
            );

//            consumer.showCustomOption(
//                    ImpexCodeStyleSettings.class,
//                    "SPACE_BEFORE_LEFT_SQUARE_BRACKET",
//                    "Before left square bracket",
//                    CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS
//            );

//            consumer.showCustomOption(
//                    ImpexCodeStyleSettings.class,
//                    "SPACE_AFTER_RIGHT_SQUARE_BRACKET",
//                    "After right square bracket",
//                    CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS
//            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_RIGHT_SQUARE_BRACKET",
                "Before right square bracket",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_AFTER_ALTERNATIVE_PATTERN",
                "After alternative pattern",
                styleOptions.SPACES_AROUND_OPERATORS
            );

            consumer.showCustomOption(
                ImpexCodeStyleSettings.class,
                "SPACE_BEFORE_ALTERNATIVE_PATTERN",
                "Before alternative pattern",
                styleOptions.SPACES_AROUND_OPERATORS
            );

        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {

            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
        }
    }

    @Override
    public String getCodeSample(@NotNull final SettingsType settingsType) {
        return "# Comment\n" +
               "$lang = en\n" +
               "$contentCatalog = projectContentCatalog\n" +
               "$contentCV = catalogVersion(CatalogVersion.catalog(Catalog.id[default = $contentCatalog]),CatalogVersion.version[default = 'Staged'])[default = $contentCatalog:Staged]\n" +
               "$macro = qwe;qwe,qwe,;qwe\n" +
               '\n' +
               "#% impex.setLocale( Locale.GERMAN );\n" +
               '\n' +
               "INSERT_UPDATE SomeType ; $contentCV[unique = true][map-delimiter = |][dateformat = yyyy-MM-dd HH:mm:ss] ; uid[unique = true]          ; title[lang = $lang]\n" +
               "Subtype                ;                                                                                ; account                     ; \"Your Account\"\n" +
               "                       ;                                                                                ; <ignore>                    ; \"Add/Edit Address\"\n" +
               "                       ;                                                                                ; key -> value | key -> value ; \"Address Book\"\n" +
               "                       ;                                                                                ; value1, value2, value3      ; 12345               ;" +
               '\n' +
               "INSERT Address[impex.legacy.mode = true, batchmode = true] ; firstname ; owner(Principal.uid | AbstractOrder.code)\n" +
               "                                                           ; Hans      ; admin\n" +
               '\n' +
               "UPDATE Address ; firstname ; owner(Principal.uid | AbstractOrder.code) ; &docId\n" +
               "               ; Hans      ; admin                                     ; id\n" +
               '\n' +
               "remove Address ; firstname ; owner(Principal.uid | AbstractOrder.code)\n" +
               "               ; Hans      ; admin\n" +
               '\n' +
               "INSERT_UPDATE Media ; @media[translator = de.hybris.platform.impex.jalo.media.MediaDataTranslato r] ; mime[default = 'image/png']\n" +
               "                    ;                                                                               ; $contentResource/images/logo .png\n";
    }
}
