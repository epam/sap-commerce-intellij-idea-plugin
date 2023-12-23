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
package com.intellij.idea.plugin.hybris.flexibleSearch.highlighting

import com.intellij.codeHighlighting.RainbowHighlighter
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_BRACES
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_BRACKETS
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_COLUMN
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_COLUMN_ALIAS
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_COLUMN_SEPARATOR
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_COMMENT
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_DBRACES
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_FUNCTION_CALL
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_KEYWORD
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_LOCALIZED
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_NUMBER
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_OPERATION_SIGN
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_OUTER_JOIN
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_PARAMETER
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_PARENS
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_STAR
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_STRING
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_SYMBOL
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_TABLE
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_TABLE_ALIAS
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchHighlighterColors.FXS_TABLE_TAIL
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class FlexibleSearchColorSettingsPage : ColorSettingsPage {

    override fun getDisplayName() = "FlexibleSearch"
    override fun getIcon(): Icon = HybrisIcons.FXS_FILE
    override fun getAttributeDescriptors() = descriptions
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = customTags
    override fun getHighlighter(): SyntaxHighlighter = FlexibleSearchSyntaxHighlighter.getInstance()

    override fun getDemoText() = """
SELECT {${tableAlias("cat")}:${column("pk")}} FROM {${table("Category")} AS ${tableAlias("cat")}} WHERE NOT EXISTS (
   {{ SELECT * FROM {${table("CategoryCategoryRelation")}} WHERE {${column("target")}}={${tableAlias("cat")}:${column("pk")}} }}
)

SELECT ${function("COUNT")}(*), ${function("COUNT")}({${column("pk")}}) FROM {${table("Product")}${tableTail("!")} WHERE {${column("code")}} LIKE '%al%'

SELECT ${function("COUNT")}(DISTINCT {${column("code")}}) FROM {${table("Product")}${tableTail("*")}} WHERE {${column("code")}} LIKE '%al%' AND {${column("code")}} LIKE '%15%'

SELECT * FROM {${table("Product")}} WHERE {${column("code")}} IS NULL

SELECT {${tableAlias("cat")}:${column("pk")}}, {${tableAlias("cat")}.${column("code")}} FROM {${table("Category")} AS ${tableAlias("cat")}} WHERE NOT EXISTS (
   {{ SELECT * FROM {${table("CategoryCategoryRelation")}} WHERE {${column("target")}}={${tableAlias("cat")}.${column("pk")}} }}
)

SELECT {${tableAlias("p")}:${column("pk")}} as ${columnAlias("columnAlias")}
FROM {${table("Product")} AS ${tableAlias("p")}}
WHERE {${tableAlias("p")}:${column("code")}} LIKE '%myProduct'
  OR {${tableAlias("p")}:${column("name")}${localized("en")}} LIKE '%myProduct'
  OR {${tableAlias("p")}:${column("name")}${localized("de")}:o} LIKE '%myProduct'
  /*
    OR {p:name[de]:o} LIKE '%myProduct'
    OR {p:name[es]:o} LIKE '%myProduct'
  */
  OR ( {${tableAlias("p")}.${column("modifiedtime")}} >= ?startDate AND {${tableAlias("p")}.${column("modifiedtime")}} <= ?endDate )
  AND (
        {${tableAlias("p")}.${column("code")}} IS NOT NULL
    AND {${tableAlias("p")}:${column("code")}} LIKE '%al%'
--    AND {p:code} LIKE '%al%'
    OR  {${tableAlias("p")}.${column("code")}} = 2
    OR  {${tableAlias("p")}.${column("code")}} != 3
    OR  {${tableAlias("p")}.${column("modifiedtime")}} = ${param("?session")}.${param("user")}.${param("modifiedtime")}
    AND {${tableAlias("p")}.${column("code")}} NOT LIKE '%15%'
  )
ORDER BY {${tableAlias("p")}:${column("code")}} ASC

SELECT ${tableAlias("tableAlias")}.${column("PK")} FROM (
    {{ SELECT * FROM {${table("Product")}} }}
) AS ${tableAlias("tableAlias")}

@@@@@
"""

    private fun localized(value: String) = "[<localized>$value</localized>]"
    private fun param(value: String) = "<param>$value</param>"
    private fun table(value: String) = "<table>$value</table>"
    private fun tableTail(value: String) = "<tableTail>$value</tableTail>"
    private fun tableAlias(value: String) = "<tableAlias>$value</tableAlias>"
    private fun column(value: String) = "<column>$value</column>"
    private fun columnAlias(value: String) = "<columnAlias>$value</columnAlias>"
    private fun function(value: String) = "<function>$value</function>"

    private val descriptions = arrayOf(
        AttributesDescriptor("Keyword", FXS_KEYWORD),
        AttributesDescriptor("Symbol", FXS_SYMBOL),
        AttributesDescriptor("Braces//Single braces", FXS_BRACES),
        AttributesDescriptor("Braces//Double braces", FXS_DBRACES),
        AttributesDescriptor("Braces//Bracket", FXS_BRACKETS),
        AttributesDescriptor("Braces//Paren", FXS_PARENS),
        AttributesDescriptor("Column//Name", FXS_COLUMN),
        AttributesDescriptor("Column//Outer Join `:o`", FXS_OUTER_JOIN),
        AttributesDescriptor("Column//Separator `.` or `:`", FXS_COLUMN_SEPARATOR),
        AttributesDescriptor("Column//Localized `[]`", FXS_LOCALIZED),
        AttributesDescriptor("Column//Alias", FXS_COLUMN_ALIAS),
        AttributesDescriptor("Table//Name", FXS_TABLE),
        AttributesDescriptor("Table//Tail `!` or `*`", FXS_TABLE_TAIL),
        AttributesDescriptor("Table//Alias", FXS_TABLE_ALIAS),
        AttributesDescriptor("Parameter", FXS_PARAMETER),
        AttributesDescriptor("Function", FXS_FUNCTION_CALL),
        AttributesDescriptor("Comment", FXS_COMMENT),
        AttributesDescriptor("Tokens//String", FXS_STRING),
        AttributesDescriptor("Tokens//Number", FXS_NUMBER),
        AttributesDescriptor("Tokens//Star", FXS_STAR),
        AttributesDescriptor("Operation sign", FXS_OPERATION_SIGN),
    )

    private val customTags = with(RainbowHighlighter.createRainbowHLM()) {
        this["table"] = FXS_TABLE
        this["tableTail"] = FXS_TABLE_TAIL
        this["tableAlias"] = FXS_TABLE_ALIAS
        this["column"] = FXS_COLUMN
        this["columnAlias"] = FXS_COLUMN_ALIAS
        this["localized"] = FXS_LOCALIZED
        this["param"] = FXS_PARAMETER
        this["function"] = FXS_FUNCTION_CALL

        this
    }
}
