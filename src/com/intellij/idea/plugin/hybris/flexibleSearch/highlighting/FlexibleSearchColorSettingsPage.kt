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
package com.intellij.idea.plugin.hybris.flexibleSearch.highlighting

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class FlexibleSearchColorSettingsPage : ColorSettingsPage {

    override fun getDisplayName() = "FlexibleSearch"
    override fun getIcon(): Icon = HybrisIcons.FXS_FILE
    override fun getAdditionalHighlightingTagToDescriptorMap() = null
    override fun getAttributeDescriptors() = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = FlexibleSearchSyntaxHighlighter.instance

    override fun getDemoText() = """
SELECT {cat:pk} FROM {Category AS cat} WHERE NOT EXISTS (
   {{ SELECT * FROM {CategoryCategoryRelation} WHERE {target}={cat:pk} }}
)

SELECT COUNT(*), COUNT({pk}) FROM {Product!} WHERE {code} LIKE '%al%'

SELECT COUNT(DISTINCT {code}) FROM {Product} WHERE {code} LIKE '%al%' AND {code} LIKE '%15%'

SELECT * FROM {Product} WHERE {code} IS NULL

SELECT {cat:pk}, {cat.code} FROM {Category AS cat} WHERE NOT EXISTS (
   {{ SELECT * FROM {CategoryCategoryRelation} WHERE {target}={cat.spk} }}
)

SELECT {p:PK}
FROM {Product AS p}
WHERE {p:code} LIKE '%myProduct'
  OR {p:name[en]} LIKE '%myProduct'
  OR {p:name[de]:o} LIKE '%myProduct'
  /*
    OR {p:name[de]:o} LIKE '%myProduct'
    OR {p:name[es]:o} LIKE '%myProduct'
  */
  OR ( {p.modifiedtime} >= ?startDate AND {p.modifiedtime} <= ?endDate )
  AND (
        {p.code} IS NOT NULL
    AND {p:code} LIKE '%al%'
--    AND {p:code} LIKE '%al%'
    OR  {p.code} = 2
    OR  {p.modifiedtime} = ?session.user.modifiedtime
    AND {p.code} NOT LIKE '%15%'
  )
ORDER BY {p:code} ASC

@@@@@
"""

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", FlexibleSearchHighlighterColors.FS_KEYWORD),
            AttributesDescriptor("Symbol", FlexibleSearchHighlighterColors.FS_SYMBOL),
            AttributesDescriptor("Braces//Braces", FlexibleSearchHighlighterColors.FS_BRACES),
            AttributesDescriptor("Braces//Bracket", FlexibleSearchHighlighterColors.FS_BRACKETS),
            AttributesDescriptor("Braces//Parentheses", FlexibleSearchHighlighterColors.FS_PARENTHESES),
            AttributesDescriptor("Braces//Paren", FlexibleSearchHighlighterColors.FS_PARENS),
            AttributesDescriptor("Column//Outer Join `:o`", FlexibleSearchHighlighterColors.FS_OUTER_JOIN),
            AttributesDescriptor("Column//Separator `.` or `:`", FlexibleSearchHighlighterColors.FS_COLUMN_SEPARATOR),
            AttributesDescriptor("Parameter", FlexibleSearchHighlighterColors.FS_PARAMETER),
            AttributesDescriptor("Function", FlexibleSearchHighlighterColors.FS_FUNCTION_CALL),
            AttributesDescriptor("Comment", FlexibleSearchHighlighterColors.FS_COMMENT),
            AttributesDescriptor("Tokens//String", FlexibleSearchHighlighterColors.FS_STRING),
            AttributesDescriptor("Tokens//Number", FlexibleSearchHighlighterColors.FS_NUMBER),
            AttributesDescriptor("Not available in preview//Table", FlexibleSearchHighlighterColors.FS_TABLE),
            AttributesDescriptor("Not available in preview//Table Trail `!` or `*`", FlexibleSearchHighlighterColors.FS_TABLE_TRAIL),
            AttributesDescriptor("Not available in preview//Table Alias", FlexibleSearchHighlighterColors.FS_TABLE_ALIAS),
            AttributesDescriptor("Not available in preview//Column", FlexibleSearchHighlighterColors.FS_COLUMN),
            AttributesDescriptor("Not available in preview//Column Alias", FlexibleSearchHighlighterColors.FS_COLUMN_ALIAS),
            AttributesDescriptor("Not available in preview//Column - Localized `[]`", FlexibleSearchHighlighterColors.FS_LOCALIZED),
            AttributesDescriptor("Not available in preview//Nested parameter `\$session.user`", FlexibleSearchHighlighterColors.FS_PARAMETER),
        )
    }
}
