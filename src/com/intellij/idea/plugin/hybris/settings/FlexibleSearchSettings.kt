/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.components.BaseState

data class FlexibleSearchSettings(
    var verifyCaseForReservedWords: Boolean = true,
    var verifyUsedTableAliasSeparator: Boolean = true,
    var fallbackToTableNameIfNoAliasProvided: Boolean = true,
    var defaultCaseForReservedWords: ReservedWordsCase = ReservedWordsCase.UPPERCASE,

    var completion: FlexibleSearchCompletionSettings = FlexibleSearchCompletionSettings(),
    var folding: FlexibleSearchFoldingSettings = FlexibleSearchFoldingSettings(),
    var documentation: FlexibleSearchDocumentationSettings = FlexibleSearchDocumentationSettings(),
)

data class FlexibleSearchCompletionSettings(
    var injectSpaceAfterKeywords: Boolean = true,
    var injectTableAliasSeparator: Boolean = true,
    var suggestTableAliasNames: Boolean = true,
    var injectCommaAfterExpression: Boolean = true,
    var defaultTableAliasSeparator: String = HybrisConstants.FXS_TABLE_ALIAS_SEPARATOR_DOT,
)

data class FlexibleSearchDocumentationSettings(
    var enabled: Boolean = true,
    var showTypeDocumentation: Boolean = true,
)

class FlexibleSearchFoldingSettings : BaseState() {
    var enabled by property(true)
    var showSelectedTableNameForYColumn by property(false)
    var showLanguageForYColumn by property(true)
}

