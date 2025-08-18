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

package sap.commerce.toolset.flexibleSearch

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.psi.tree.IFileElementType
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes

object FlexibleSearchConstants {
    val FILE_NODE_TYPE = IFileElementType(FlexibleSearchLanguage)

    val SUPPORTED_ELEMENT_TYPES = setOf(
        FlexibleSearchTypes.TABLE_ALIAS_NAME,
        FlexibleSearchTypes.COLUMN_ALIAS_NAME
    )

    const val DUMMY_IDENTIFIER = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED

    val RESERVED_KEYWORDS = setOf(
        FlexibleSearchTypes.ALL,
        FlexibleSearchTypes.AND,
        FlexibleSearchTypes.AS,
        FlexibleSearchTypes.ASC,
        FlexibleSearchTypes.BETWEEN,
        FlexibleSearchTypes.BY,
        FlexibleSearchTypes.CASE,
        FlexibleSearchTypes.CAST,
        FlexibleSearchTypes.DESC,
        FlexibleSearchTypes.DISTINCT,
        FlexibleSearchTypes.ELSE,
        FlexibleSearchTypes.END,
        FlexibleSearchTypes.EXISTS,
        FlexibleSearchTypes.FROM,
        FlexibleSearchTypes.FULL,
        FlexibleSearchTypes.GROUP,
        FlexibleSearchTypes.HAVING,
        FlexibleSearchTypes.IN,
        FlexibleSearchTypes.INNER,
        FlexibleSearchTypes.INTERVAL,
        FlexibleSearchTypes.IS,
        FlexibleSearchTypes.JOIN,
        FlexibleSearchTypes.LEFT,
        FlexibleSearchTypes.LIKE,
        FlexibleSearchTypes.LIMIT,
        FlexibleSearchTypes.NOT,
        FlexibleSearchTypes.NULL,
        FlexibleSearchTypes.OFFSET,
        FlexibleSearchTypes.ON,
        FlexibleSearchTypes.OR,
        FlexibleSearchTypes.ORDER,
        FlexibleSearchTypes.OUTER,
        FlexibleSearchTypes.RIGHT,
        FlexibleSearchTypes.SELECT,
        FlexibleSearchTypes.THEN,
        FlexibleSearchTypes.UNION,
        FlexibleSearchTypes.USING,
        FlexibleSearchTypes.WHEN,
        FlexibleSearchTypes.WHERE,
    )
}
