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

package com.intellij.idea.plugin.hybris.polyglotQuery.highlighting

import com.intellij.codeHighlighting.RainbowHighlighter
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_BRACES
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_BRACKETS
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_COLUMN
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_COMMENT
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_KEYWORD
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_LOCALIZED
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_OPERAND
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_PARAMETER
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_PARENS
import com.intellij.idea.plugin.hybris.polyglotQuery.highlighting.PolyglotQueryHighlighterColors.PGQ_TYPE
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class PolyglotQueryColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName() = "Polyglot Query"

    override fun getIcon(): Icon = HybrisIcons.PGQ_FILE
    override fun getAttributeDescriptors() =  DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey> = customTags
    override fun getHighlighter(): SyntaxHighlighter = PolyglotQuerySyntaxHighlighter.instance

    override fun getDemoText(): String = """
        GET {${type("Title")}} 
        WHERE {${attribute("code")}}=${param("?code1")} 
        OR {${attribute("code")}}=${param("?session")}.${param("user")}.${param("code")} 
        OR ({${attribute("pk")}}=${param("?pk")} AND {${attribute("pk")}} IS NOT NULL)
        -- OR {code}=?code2 
        AND {${attribute("code")}} IS NOT NULL      
        ORDER BY {${attribute("code")}${localized("en")}} DESC
        
        /*
        GET {Customer}
        ORDER BY {pk}
        */
    """.trimIndent()

    companion object {
        private fun localized(value: String) = "[<localized>$value</localized>]"
        private fun param(value: String) = "<param>$value</param>"
        private fun type(value: String) = "<type>$value</type>"
        private fun attribute(value: String) = "<attribute>$value</attribute>"

        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Braces//Braces", PGQ_BRACES),
            AttributesDescriptor("Braces//Bracket", PGQ_BRACKETS),
            AttributesDescriptor("Braces//Paren", PGQ_PARENS),
            AttributesDescriptor("Column//Localized `[]`", PGQ_LOCALIZED),
            AttributesDescriptor("Column//Name", PGQ_COLUMN),
            AttributesDescriptor("Comment", PGQ_COMMENT),
            AttributesDescriptor("Keyword", PGQ_KEYWORD),
            AttributesDescriptor("Parameter", PGQ_PARAMETER),
            AttributesDescriptor("Operand", PGQ_OPERAND),
            AttributesDescriptor("Type", PGQ_TYPE),
        )

        private val customTags = RainbowHighlighter.createRainbowHLM()

        init {
            customTags["localized"] = PGQ_LOCALIZED
            customTags["param"] = PGQ_PARAMETER
            customTags["type"] = PGQ_TYPE
            customTags["attribute"] = PGQ_COLUMN
         }
    }
}