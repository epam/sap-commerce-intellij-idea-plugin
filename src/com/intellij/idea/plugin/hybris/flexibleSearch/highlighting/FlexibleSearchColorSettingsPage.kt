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
import com.intellij.idea.plugin.hybris.flexibleSearch.highlighting.FlexibleSearchSyntaxHighlighter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class FlexibleSearchColorSettingsPage : ColorSettingsPage {

    override fun getDisplayName() = "FlexibleSearch"
    override fun getIcon(): Icon = HybrisIcons.FS_FILE
    override fun getAdditionalHighlightingTagToDescriptorMap() = null
    override fun getAttributeDescriptors() = DESCRIPTORS
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = ApplicationManager.getApplication().getService(FlexibleSearchSyntaxHighlighter::class.java)

    override fun getDemoText(): String {
        return """SELECT {cat:pk} FROM {Category AS cat} WHERE NOT EXISTS (
   {{ SELECT * FROM {CategoryCategoryRelation} WHERE {target}={cat:pk} }}
)

SELECT * FROM {Product} WHERE {code} LIKE '%al%'


SELECT * FROM {Product} WHERE {code} LIKE '%al%' AND {code} LIKE '%15%'

SELECT * FROM {Product} WHERE {code} IS NULL

SELECT * FROM {Product} WHERE {code} NOT LIKE '%al%' AND {code} NOT LIKE '%15%' OR {code} IS NULL

SELECT * FROM {Product} WHERE {code} LIKE '%al%' AND {code} NOT LIKE '%15%'

SELECT * FROM {Product} WHERE {code} IS NOT NULL

SELECT {cat:pk} FROM {Category AS cat} WHERE NOT EXISTS (
   {{ SELECT * FROM {CategoryCategoryRelation} WHERE {target}={cat.spk} }}
)

SELECT {code},{pk} FROM  {Product} ORDER BY {code} DESC

SELECT {pk} FROM {Product} WHERE {modifiedtime} >= ?startDate AND {modifiedtime} <= ?endDate

SELECT {p:PK}
   FROM {Product AS p}
   WHERE {p:code} LIKE '%myProduct'
      OR {p:name} LIKE '%myProduct'
   ORDER BY {p:code} ASC

@@@@@
"""
    }

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Comment", FlexibleSearchHighlighterColors.FS_COMMENT),
            AttributesDescriptor("Parameter", FlexibleSearchHighlighterColors.FS_PARAMETER),
            AttributesDescriptor("Keywords", FlexibleSearchHighlighterColors.FS_KEYWORD),
            AttributesDescriptor("Column", FlexibleSearchHighlighterColors.FS_COLUMN),
            AttributesDescriptor("Comma", FlexibleSearchHighlighterColors.FS_SYMBOL),
            AttributesDescriptor("Number", FlexibleSearchHighlighterColors.FS_NUMBER),
            AttributesDescriptor("Table", FlexibleSearchHighlighterColors.FS_TABLE),
            AttributesDescriptor("Braces", FlexibleSearchHighlighterColors.FS_BRACES),
            AttributesDescriptor("Brackets", FlexibleSearchHighlighterColors.FS_BRACKETS),
            AttributesDescriptor("Parentheses", FlexibleSearchHighlighterColors.FS_PARENTHESES),
            AttributesDescriptor("Bad character", HighlighterColors.BAD_CHARACTER)
        )
    }
}
