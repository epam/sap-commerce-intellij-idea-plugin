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

import com.intellij.codeHighlighting.RainbowHighlighter
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class AclColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = HybrisIcons.Acl.FILE
    override fun getHighlighter() = AclSyntaxHighlighter.getInstance()
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = customTags
    override fun getAttributeDescriptors() = descriptors
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName() = HybrisConstants.ACL

    override fun getDemoText(): String {
        return """# Comment
   
${"$"}START_USERRIGHTS;;;;;
Type      ; UID        ; MemberOfGroups ; Password ; Target       ; read ; change ; create ; delete ; change_perm
<vl>UserGroup ; impexgroup ; employeegroup  ;</vl>
          ;            ;                ;          ; Product.code ; +    ; +      ; +      ; +      ; -
<vl>Customer  ; impex-demo ; impexgroup     ; 1234     ;              ; .    ; -      ;        ; .      ;</vl>
          ;            ;                ;          ; Customer.pk  ; +    ; +      ; +      ; +      ; -
          ;            ;                ;          ; Product      ; +    ; +      ; +      ; +      ; -
          ;            ;                ;          ; Test.catalog ; +    ; +      ; +      ; +      ; -
${"$"}END_USERRIGHTS;;;;;

@@@@@
"""
    }

    private val customTags = with (RainbowHighlighter.createRainbowHLM()) {
        put("vl", AclHighlighterColors.USER_RIGHTS_VALUE_LINE_TYPE)
        this
    }

    private val descriptors = arrayOf(
        AttributesDescriptor("Comment line", AclHighlighterColors.COMMENT),
        AttributesDescriptor("Dot", AclHighlighterColors.DOT),

        AttributesDescriptor("Bad character", HighlighterColors.BAD_CHARACTER),

        AttributesDescriptor("Separators//Field value separator", AclHighlighterColors.FIELD_VALUE_SEPARATOR),
        AttributesDescriptor("Separators//Parameters separator", AclHighlighterColors.PARAMETERS_SEPARATOR),

        AttributesDescriptor("Header//Parameter name", AclHighlighterColors.USER_RIGHTS_HEADER_PARAMETER),
        AttributesDescriptor("Header//Mandatory parameter name", AclHighlighterColors.USER_RIGHTS_HEADER_MANDATORY_PARAMETER),

        AttributesDescriptor("User rights", AclHighlighterColors.USER_RIGHTS),

        AttributesDescriptor("Values//Type", AclHighlighterColors.FIELD_VALUE_TYPE),
        AttributesDescriptor("Values//Permission allowed", AclHighlighterColors.USER_RIGHTS_PERMISSION_GRANTED),
        AttributesDescriptor("Values//Permission denied", AclHighlighterColors.USER_RIGHTS_PERMISSION_DENIED),
        AttributesDescriptor("Values//Permission inherited", AclHighlighterColors.USER_RIGHTS_PERMISSION_INHERITED),
        AttributesDescriptor("Values//Target type", AclHighlighterColors.FIELD_VALUE_TARGET_TYPE),
        AttributesDescriptor("Values//Target attribute", AclHighlighterColors.FIELD_VALUE_TARGET_ATTRIBUTE),

        AttributesDescriptor("Lines//Typed value line", AclHighlighterColors.USER_RIGHTS_VALUE_LINE_TYPE)
    )

}
