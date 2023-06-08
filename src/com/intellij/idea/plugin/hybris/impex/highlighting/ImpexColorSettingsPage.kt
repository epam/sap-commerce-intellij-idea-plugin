/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.impex.highlighting

import com.intellij.codeHighlighting.RainbowHighlighter
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class ImpexColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = HybrisIcons.IMPEX_FILE
    override fun getHighlighter() = DefaultImpexSyntaxHighlighter.instance
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = customTags
    override fun getAttributeDescriptors() = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName() = HybrisConstants.IMPEX

    override fun getDemoText(): String {
        return """# Comment
   
${"$"}START_USERRIGHTS
Type      ; UID        ; MemberOfGroups ; Password ; Target       ; read ; change ; create ; delete ; change_perm
UserGroup ; impexgroup ; employeegroup  ;
          ;            ;                ;          ; Product.code ; +    ; +      ; +      ; +      ; -
Customer  ; impex-demo ; impexgroup     ; 1234     ;              ; $inheritedPermission    ; -      ;        ; $inheritedPermission      ;
${"$"}END_USERRIGHTS

${"$"}lang = en
${"$"}configProperty = ${"$"}config-HYBRIS_BIN_DIR
${"$"}contentCatalog = projectContentCatalog
${"$"}contentCV = catalogVersion(CatalogVersion.catalog(Catalog.id[default = ${"$"}contentCatalog]), CatalogVersion.version[default = 'Staged'])[default = ${"$"}contentCatalog:Staged]
${"$"}macro = qwe;qwe, qwe, ;qwe

#% impex.setLocale( Locale.GERMAN );

INSERT_UPDATE SomeType; ${"$"}contentCV[unique = true][map-delimiter = |][dateformat = yyyy-MM-dd HH:mm:ss]; uid[unique = true]; title[lang = ${"$"}lang]
Subtype ; ; account                ; "Your Account"
        ; ; <ignore>               ; "Add/Edit Address"
        ; ; key -> vaue | key ->
vaue                               ; "Address Book"
        ; ; value1, value2, value3 ; 12345 ; com.domain.Class ; qwe : asd

INSERT Address[impex.legacy.mode = true, batchmode = true]; firstname; owner(Principal.uid | AbstractOrder.code); Hans; admin

UPDATE Address; firstname; owner(Principal.uid | AbstractOrder.code); &docId
; Hans ; admin ; id

remove Address; firstname; owner(Principal.uid | AbstractOrder.code); Hans; admin

INSERT_UPDATE Media; @media[translator = de.hybris.platform.impex.jalo.media.MediaDataTranslator]; mime[default = 'image/png']
; ; ${"$"}contentResource/images/logo .png

@@@@@
"""
    }

    companion object {
        private const val inheritedPermission = "<permission_inherited>.</permission_inherited>"

        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Comment line", ImpexHighlighterColors.PROPERTY_COMMENT),
            AttributesDescriptor("Macro name declaration", ImpexHighlighterColors.MACRO_NAME_DECLARATION),
            AttributesDescriptor("Macro value", ImpexHighlighterColors.MACRO_VALUE),
            AttributesDescriptor("Macro usage", ImpexHighlighterColors.MACRO_USAGE),
            AttributesDescriptor("Assign value", ImpexHighlighterColors.ASSIGN_VALUE),
            AttributesDescriptor("Insert", ImpexHighlighterColors.HEADER_MODE_INSERT),
            AttributesDescriptor("Update", ImpexHighlighterColors.HEADER_MODE_UPDATE),
            AttributesDescriptor("Insert or update", ImpexHighlighterColors.HEADER_MODE_INSERT_UPDATE),
            AttributesDescriptor("Remove", ImpexHighlighterColors.HEADER_MODE_REMOVE),
            AttributesDescriptor("Function call", ImpexHighlighterColors.FUNCTION_CALL),
            AttributesDescriptor("Header type", ImpexHighlighterColors.HEADER_TYPE),
            AttributesDescriptor("Value sub-type", ImpexHighlighterColors.VALUE_SUBTYPE),
            AttributesDescriptor("Field value separator", ImpexHighlighterColors.FIELD_VALUE_SEPARATOR),
            AttributesDescriptor("List item separator", ImpexHighlighterColors.FIELD_LIST_ITEM_SEPARATOR),
            AttributesDescriptor("Field value", ImpexHighlighterColors.FIELD_VALUE),
            AttributesDescriptor("Single string", ImpexHighlighterColors.SINGLE_STRING),
            AttributesDescriptor("Double string", ImpexHighlighterColors.DOUBLE_STRING),
            AttributesDescriptor("Ignore value", ImpexHighlighterColors.FIELD_VALUE_IGNORE),
            AttributesDescriptor("Bean Shell marker", ImpexHighlighterColors.BEAN_SHELL_MARKER),
            AttributesDescriptor("Bean Shell body", ImpexHighlighterColors.BEAN_SHELL_BODY),
            AttributesDescriptor("Square brackets", ImpexHighlighterColors.SQUARE_BRACKETS),
            AttributesDescriptor("Round brackets", ImpexHighlighterColors.ROUND_BRACKETS),
            AttributesDescriptor("Attribute name", ImpexHighlighterColors.ATTRIBUTE_NAME),
            AttributesDescriptor("Attribute value", ImpexHighlighterColors.ATTRIBUTE_VALUE),
            AttributesDescriptor("Attribute separator", ImpexHighlighterColors.ATTRIBUTE_SEPARATOR),
            AttributesDescriptor("Boolean", ImpexHighlighterColors.BOOLEAN),
            AttributesDescriptor("Digit", ImpexHighlighterColors.DIGIT),
            AttributesDescriptor("Alternative map delimiter", ImpexHighlighterColors.ALTERNATIVE_MAP_DELIMITER),
            AttributesDescriptor("Default key-value delimiter", ImpexHighlighterColors.DEFAULT_KEY_VALUE_DELIMITER),
            AttributesDescriptor("Default path delimiter", ImpexHighlighterColors.DEFAULT_PATH_DELIMITER),
            AttributesDescriptor("Parameter name", ImpexHighlighterColors.HEADER_PARAMETER_NAME),
            AttributesDescriptor("Special parameter name", ImpexHighlighterColors.HEADER_SPECIAL_PARAMETER_NAME),
            AttributesDescriptor("Parameters separator", ImpexHighlighterColors.PARAMETERS_SEPARATOR),
            AttributesDescriptor("Comma", ImpexHighlighterColors.COMMA),
            AttributesDescriptor("Alternative pattern", ImpexHighlighterColors.ALTERNATIVE_PATTERN),
            AttributesDescriptor("Document id", ImpexHighlighterColors.DOCUMENT_ID),
            AttributesDescriptor("Bad character", HighlighterColors.BAD_CHARACTER),
            AttributesDescriptor("Warnings", ImpexHighlighterColors.WARNINGS_ATTRIBUTES),
            AttributesDescriptor("User rights", ImpexHighlighterColors.USER_RIGHTS),
            AttributesDescriptor("User rights//Parameter name", ImpexHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
            AttributesDescriptor("User rights//Mandatory parameter name", ImpexHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER),
            AttributesDescriptor("User rights//Permission allowed", ImpexHighlighterColors.USER_RIGHTS_PERMISSION_ALLOWED),
            AttributesDescriptor("User rights//Permission denied", ImpexHighlighterColors.USER_RIGHTS_PERMISSION_DENIED),
            AttributesDescriptor("User rights//Permission inherited", ImpexHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED)
        )

        private val customTags = RainbowHighlighter.createRainbowHLM()

        init {
            customTags["permission_inherited"] = ImpexHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED
        }
    }
}
