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
package sap.commerce.toolset.impex.highlighting

import com.intellij.codeHighlighting.RainbowHighlighter
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.ImpExConstants
import javax.swing.Icon

class ImpExColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = HybrisIcons.ImpEx.FILE
    override fun getHighlighter() = ImpExSyntaxHighlighter.getInstance()
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = customTags
    override fun getAttributeDescriptors() = descriptors
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName() = ImpExConstants.IMPEX

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

#% beforeEach: impex.setLocale( Locale.GERMAN );

UPDATE Language; ${unique("isoCode")}[unique=true]; fallbackLanguages(isoCode); enumAttribute(code)
               ; en                               ; (+) de                    ; <sev>SOME_ENUM_VALUE</sev>
               ; en                               ; (+?) z                    ;
               ; en                               ; (-) fr                    ;

<hl>INSERT SomeType; param; param2; param3</hl>
<vlo>; value; value; another value</vlo>
<vle>; value; value; another value</vle>
<vlo>; value; value; another value</vlo>
<vle>; value; value; another value</vle>

INSERT_UPDATE SomeType; ${"$"}contentCV[unique = true][map-delimiter = |][dateformat = yyyy-MM-dd HH:mm:ss]; ${unique("uid")}[unique = true]; title[lang = ${"$"}lang]; ${attributeHeaderAbbreviation("C@someAttribute")}
Subtype ; ; account                ; "Your Account"
        ; ; <ignore>               ; "Add/Edit Address"
        ; ; <null>                 ;
        ; ; key -> vaue | key ->
vaue                               ; "Address Book"
        ; ; value1, value2, value3 ; 12345 ; com.domain.Class ; qwe : asd

INSERT Address[impex.legacy.mode = true, batchmode = true]; firstname; owner(Principal.uid | AbstractOrder.code); Hans; admin

UPDATE Address; firstname\
              ; owner(Principal.uid | AbstractOrder.code)\
              ; &docId
              ; Hans \
              ; admin \
              ; id

remove Address; firstname; owner(Principal.uid | AbstractOrder.code); Hans; admin

INSERT_UPDATE Media; @media[translator = de.hybris.platform.impex.jalo.media.MediaDataTranslator]; mime[default = 'image/png']
; ; ${"$"}contentResource/images/logo.png
; jar:/impex/testfiles/import/media/dummymedia/img_05.jpg ;
; zip:ext/impex/resources/impex/testfiles/import/media/dummymedia/test_9-10.zip&img_09.jpg ;
; file:ext/impex/resources/impex/testfiles/import/media/dummymedia/img 02.jpg ;
; http:http://site.org/picture.png ;
; /medias/fromjar/demo5.jpg ;

INSERT Employee; uid[unique=true]; @password[translator=de.hybris.platform.impex.jalo.translators.UserPasswordTranslator] 
; fritz ; md5:a7c15c415c37626de8fa648127ba1ae5
; max ; *:plainPassword

@@@@@
"""
    }

    private val customTags = with (RainbowHighlighter.createRainbowHLM()) {
        put("permission_inherited", ImpExHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED)
        put("attribute_header_abbreviation", ImpExHighlighterColors.ATTRIBUTE_HEADER_ABBREVIATION)
        put("hl", ImpExHighlighterColors.HEADER_LINE)
        put("vle", ImpExHighlighterColors.VALUE_LINE_EVEN)
        put("vlo", ImpExHighlighterColors.VALUE_LINE_ODD)
        put("sev", ImpExHighlighterColors.ENUM_VALUE)
        put("unique", ImpExHighlighterColors.HEADER_UNIQUE_PARAMETER_NAME)
        this
    }
    private val inheritedPermission = "<permission_inherited>.</permission_inherited>"
    private fun attributeHeaderAbbreviation(abbreviation : String) = "<attribute_header_abbreviation>${"$$abbreviation"}</attribute_header_abbreviation>"
    private fun unique(parameterName : String) = "<unique>${parameterName}</unique>"

    private val descriptors = arrayOf(
        AttributesDescriptor("Comment line", ImpExHighlighterColors.PROPERTY_COMMENT),

        AttributesDescriptor("Macro//Name declaration", ImpExHighlighterColors.MACRO_NAME_DECLARATION),
        AttributesDescriptor("Macro//Value", ImpExHighlighterColors.MACRO_VALUE),
        AttributesDescriptor("Macro//Usage", ImpExHighlighterColors.MACRO_USAGE),
        AttributesDescriptor("Macro//Assign value", ImpExHighlighterColors.ASSIGN_VALUE),

        AttributesDescriptor("Mode//Insert", ImpExHighlighterColors.HEADER_MODE_INSERT),
        AttributesDescriptor("Mode//Update", ImpExHighlighterColors.HEADER_MODE_UPDATE),
        AttributesDescriptor("Mode//Insert or update", ImpExHighlighterColors.HEADER_MODE_INSERT_UPDATE),
        AttributesDescriptor("Mode//Remove", ImpExHighlighterColors.HEADER_MODE_REMOVE),

        AttributesDescriptor("Type//Header type", ImpExHighlighterColors.HEADER_TYPE),
        AttributesDescriptor("Type//Value sub-type", ImpExHighlighterColors.VALUE_SUBTYPE),

        AttributesDescriptor("Separators//Field value separator", ImpExHighlighterColors.FIELD_VALUE_SEPARATOR),
        AttributesDescriptor("Separators//List item separator", ImpExHighlighterColors.FIELD_LIST_ITEM_SEPARATOR),
        AttributesDescriptor("Separators//Parameters separator", ImpExHighlighterColors.PARAMETERS_SEPARATOR),
        AttributesDescriptor("Separators//Multi-line separator", ImpExHighlighterColors.MULTI_LINE_SEPARATOR),

        AttributesDescriptor("Lines//Header line", ImpExHighlighterColors.HEADER_LINE),
        AttributesDescriptor("Lines//Even value line", ImpExHighlighterColors.VALUE_LINE_EVEN),
        AttributesDescriptor("Lines//Odd value line", ImpExHighlighterColors.VALUE_LINE_ODD),

        AttributesDescriptor("Value//Field value", ImpExHighlighterColors.FIELD_VALUE),
        AttributesDescriptor("Value//Single string", ImpExHighlighterColors.SINGLE_STRING),
        AttributesDescriptor("Value//Double string", ImpExHighlighterColors.DOUBLE_STRING),
        AttributesDescriptor("Value//Boolean", ImpExHighlighterColors.BOOLEAN),
        AttributesDescriptor("Value//Digit", ImpExHighlighterColors.DIGIT),
        AttributesDescriptor("Value//<null> value", ImpExHighlighterColors.FIELD_VALUE_NULL),
        AttributesDescriptor("Value//<ignore> value", ImpExHighlighterColors.FIELD_VALUE_IGNORE),
        AttributesDescriptor("Value//jar: prefix", ImpExHighlighterColors.FIELD_VALUE_JAR_PREFIX),
        AttributesDescriptor("Value//exploded jar prefix", ImpExHighlighterColors.FIELD_VALUE_EXPLODED_JAR_PREFIX),
        AttributesDescriptor("Value//file: prefix", ImpExHighlighterColors.FIELD_VALUE_FILE_PREFIX),
        AttributesDescriptor("Value//zip: prefix", ImpExHighlighterColors.FIELD_VALUE_ZIP_PREFIX),
        AttributesDescriptor("Value//http: prefix", ImpExHighlighterColors.FIELD_VALUE_HTTP_PREFIX),
        AttributesDescriptor("Value//model: prefix", ImpExHighlighterColors.FIELD_VALUE_SCRIPT_PREFIX),
        AttributesDescriptor("Value//password encoding prefix", ImpExHighlighterColors.FIELD_VALUE_PASSWORD_ENCODING_PREFIX),
        AttributesDescriptor("Value//Enum value", ImpExHighlighterColors.ENUM_VALUE),

        AttributesDescriptor("Scripting//Marker", ImpExHighlighterColors.SCRIPT_MARKER),
        AttributesDescriptor("Scripting//Action", ImpExHighlighterColors.SCRIPT_ACTION),

        AttributesDescriptor("Brackets//Square brackets", ImpExHighlighterColors.SQUARE_BRACKETS),
        AttributesDescriptor("Brackets//Round brackets", ImpExHighlighterColors.ROUND_BRACKETS),

        AttributesDescriptor("Collection prefix//Append", ImpExHighlighterColors.COLLECTION_APPEND_PREFIX),
        AttributesDescriptor("Collection prefix//Remove", ImpExHighlighterColors.COLLECTION_REMOVE_PREFIX),
        AttributesDescriptor("Collection prefix//Merge", ImpExHighlighterColors.COLLECTION_MERGE_PREFIX),

        AttributesDescriptor("Attribute//Name", ImpExHighlighterColors.ATTRIBUTE_NAME),
        AttributesDescriptor("Attribute//Value", ImpExHighlighterColors.ATTRIBUTE_VALUE),
        AttributesDescriptor("Attribute//Separator", ImpExHighlighterColors.ATTRIBUTE_SEPARATOR),
        AttributesDescriptor("Attribute//Header abbreviation", ImpExHighlighterColors.ATTRIBUTE_HEADER_ABBREVIATION),

        AttributesDescriptor("Parameter//Document id", ImpExHighlighterColors.DOCUMENT_ID),
        AttributesDescriptor("Parameter//Parameter name", ImpExHighlighterColors.HEADER_PARAMETER_NAME),
        AttributesDescriptor("Parameter//Unique parameter name", ImpExHighlighterColors.HEADER_UNIQUE_PARAMETER_NAME),
        AttributesDescriptor("Parameter//Special parameter name", ImpExHighlighterColors.HEADER_SPECIAL_PARAMETER_NAME),
        AttributesDescriptor("Parameter//Function call", ImpExHighlighterColors.FUNCTION_CALL),

        AttributesDescriptor("Delimiters//Alternative map delimiter", ImpExHighlighterColors.ALTERNATIVE_MAP_DELIMITER),
        AttributesDescriptor("Delimiters//Default key-value delimiter", ImpExHighlighterColors.DEFAULT_KEY_VALUE_DELIMITER),
        AttributesDescriptor("Delimiters//Default path delimiter", ImpExHighlighterColors.DEFAULT_PATH_DELIMITER),

        AttributesDescriptor("Comma", ImpExHighlighterColors.COMMA),
        AttributesDescriptor("Alternative pattern", ImpExHighlighterColors.ALTERNATIVE_PATTERN),
        AttributesDescriptor("Bad character", HighlighterColors.BAD_CHARACTER),
        AttributesDescriptor("Warnings", ImpExHighlighterColors.WARNINGS_ATTRIBUTES),

        AttributesDescriptor("User rights", ImpExHighlighterColors.USER_RIGHTS),
        AttributesDescriptor("User rights//Parameter name", ImpExHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
        AttributesDescriptor("User rights//Mandatory parameter name", ImpExHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER),
        AttributesDescriptor("User rights//Permission allowed", ImpExHighlighterColors.USER_RIGHTS_PERMISSION_ALLOWED),
        AttributesDescriptor("User rights//Permission denied", ImpExHighlighterColors.USER_RIGHTS_PERMISSION_DENIED),
        AttributesDescriptor("User rights//Permission inherited", ImpExHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED)
    )

}
