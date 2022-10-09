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

package com.intellij.idea.plugin.hybris.impex.highlighting;

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ALTERNATIVE_MAP_DELIMITER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ALTERNATIVE_PATTERN;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ASSIGN_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ATTRIBUTE_NAME;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ATTRIBUTE_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ATTRIBUTE_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.BEAN_SHELL_BODY;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.BEAN_SHELL_MARKER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.BOOLEAN;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.COMMA;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DEFAULT_KEY_VALUE_DELIMITER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DEFAULT_PATH_DELIMITER;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DIGIT;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DOCUMENT_ID;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.DOUBLE_STRING;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_LIST_ITEM_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_VALUE_IGNORE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.FIELD_VALUE_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_INSERT;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_INSERT_UPDATE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_REMOVE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_MODE_UPDATE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_PARAMETER_NAME;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_SPECIAL_PARAMETER_NAME;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.HEADER_TYPE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.IMPEX_FUNCTION_CALL;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.MACRO_NAME_DECLARATION;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.MACRO_USAGE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.MACRO_VALUE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.PARAMETERS_SEPARATOR;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.PROPERTY_COMMENT;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.ROUND_BRACKETS;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.SINGLE_STRING;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.SQUARE_BRACKETS;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.VALUE_SUBTYPE;
import static com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors.WARNINGS_ATTRIBUTES;

public class ImpexColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
        new AttributesDescriptor("Comment line", PROPERTY_COMMENT),
        new AttributesDescriptor("Macro name declaration", MACRO_NAME_DECLARATION),
        new AttributesDescriptor("Macro value", MACRO_VALUE),
        new AttributesDescriptor("Macro usage", MACRO_USAGE),
        new AttributesDescriptor("Assign value", ASSIGN_VALUE),
        new AttributesDescriptor("Insert", HEADER_MODE_INSERT),
        new AttributesDescriptor("Update", HEADER_MODE_UPDATE),
        new AttributesDescriptor("Insert or update", HEADER_MODE_INSERT_UPDATE),
        new AttributesDescriptor("Remove", HEADER_MODE_REMOVE),
        new AttributesDescriptor("Function call", IMPEX_FUNCTION_CALL),
        new AttributesDescriptor("Header type", HEADER_TYPE),
        new AttributesDescriptor("Value subtype", VALUE_SUBTYPE),
        new AttributesDescriptor("Field value separator", FIELD_VALUE_SEPARATOR),
        new AttributesDescriptor("List item separator", FIELD_LIST_ITEM_SEPARATOR),
        new AttributesDescriptor("Field value", FIELD_VALUE),
        new AttributesDescriptor("Single string", SINGLE_STRING),
        new AttributesDescriptor("Double string", DOUBLE_STRING),
        new AttributesDescriptor("Ignore value", FIELD_VALUE_IGNORE),
        new AttributesDescriptor("Bean Shell marker", BEAN_SHELL_MARKER),
        new AttributesDescriptor("Bean Shell body", BEAN_SHELL_BODY),
        new AttributesDescriptor("Square brackets", SQUARE_BRACKETS),
        new AttributesDescriptor("Round brackets", ROUND_BRACKETS),
        new AttributesDescriptor("Attribute name", ATTRIBUTE_NAME),
        new AttributesDescriptor("Attribute value", ATTRIBUTE_VALUE),
        new AttributesDescriptor("Attribute separator", ATTRIBUTE_SEPARATOR),
        new AttributesDescriptor("Boolean", BOOLEAN),
        new AttributesDescriptor("Digit", DIGIT),
        new AttributesDescriptor("Alternative map delimiter", ALTERNATIVE_MAP_DELIMITER),
        new AttributesDescriptor("Default key-value delimiter", DEFAULT_KEY_VALUE_DELIMITER),
        new AttributesDescriptor("Default path delimiter", DEFAULT_PATH_DELIMITER),
        new AttributesDescriptor("Parameter name", HEADER_PARAMETER_NAME),
        new AttributesDescriptor("Special parameter name", HEADER_SPECIAL_PARAMETER_NAME),
        new AttributesDescriptor("Parameters separator", PARAMETERS_SEPARATOR),
        new AttributesDescriptor("Comma", COMMA),
        new AttributesDescriptor("Alternative pattern", ALTERNATIVE_PATTERN),
        new AttributesDescriptor("Document id", DOCUMENT_ID),
        new AttributesDescriptor("Bad character", HighlighterColors.BAD_CHARACTER),
        new AttributesDescriptor("Warnings", WARNINGS_ATTRIBUTES)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return HybrisIcons.IMPEX_FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return ApplicationManager.getApplication().getService(ImpexSyntaxHighlighter.class);
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "# Comment\n" +
               "$lang = en\n" +
               "$contentCatalog = projectContentCatalog\n" +
               "$contentCV = catalogVersion(CatalogVersion.catalog(Catalog.id[default = $contentCatalog]), CatalogVersion.version[default = 'Staged'])[default = $contentCatalog:Staged]\n" +
               "$macro = qwe;qwe, qwe, ;qwe\n" +
               "\n" +
               "#% impex.setLocale( Locale.GERMAN );\n" +
               "\n" +
               "INSERT_UPDATE SomeType; $contentCV[unique = true][map-delimiter = |][dateformat = yyyy-MM-dd HH:mm:ss]; uid[unique = true]; title[lang = $lang]\n" +
               "Subtype ; ; account                ; \"Your Account\"\n" +
               "        ; ; <ignore>               ; \"Add/Edit Address\"\n" +
               "        ; ; key -> vaue | key ->\n" +
               "vaue                               ; \"Address Book\"\n" +
               "        ; ; value1, value2, value3 ; 12345 ; com.domain.Class ; qwe : asd\n" +
               "\n" +
               "INSERT Address[impex.legacy.mode = true, batchmode = true]; firstname; owner(Principal.uid | AbstractOrder.code); Hans; admin\n" +
               "\n" +
               "UPDATE Address; firstname; owner(Principal.uid | AbstractOrder.code); &docId\n" +
               "; Hans ; admin ; id\n" +
               "\n" +
               "remove Address; firstname; owner(Principal.uid | AbstractOrder.code); Hans; admin\n" +
               "\n" +
               "INSERT_UPDATE Media; @media[translator = de.hybris.platform.impex.jalo.media.MediaDataTranslator]; mime[default = 'image/png']\n" +
               "; ; $contentResource/images/logo .png\n" +
               "\n" +
               "@@@@@\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Impex";
    }
}
