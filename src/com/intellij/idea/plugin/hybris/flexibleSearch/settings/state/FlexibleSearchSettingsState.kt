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

package com.intellij.idea.plugin.hybris.flexibleSearch.settings.state

import com.intellij.idea.plugin.hybris.settings.state.ReservedWordsCase
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("FlexibleSearchSettings")
data class FlexibleSearchSettingsState(
    @JvmField @OptionTag val verifyCaseForReservedWords: Boolean = true,
    @JvmField @OptionTag val verifyUsedTableAliasSeparator: Boolean = true,
    @JvmField @OptionTag val fallbackToTableNameIfNoAliasProvided: Boolean = true,
    @JvmField @OptionTag val defaultCaseForReservedWords: ReservedWordsCase = ReservedWordsCase.UPPERCASE,

    @JvmField @OptionTag val completion: FlexibleSearchCompletionSettingsState = FlexibleSearchCompletionSettingsState(),
    @JvmField @OptionTag val folding: FlexibleSearchFoldingSettingsState = FlexibleSearchFoldingSettingsState(),
    @JvmField @OptionTag val documentation: FlexibleSearchDocumentationSettingsState = FlexibleSearchDocumentationSettingsState(),
) {

    fun mutable() = Mutable(
        verifyCaseForReservedWords = verifyCaseForReservedWords,
        verifyUsedTableAliasSeparator = verifyUsedTableAliasSeparator,
        fallbackToTableNameIfNoAliasProvided = fallbackToTableNameIfNoAliasProvided,
        defaultCaseForReservedWords = defaultCaseForReservedWords,
        completion = completion.mutable(),
        folding = folding.mutable(),
        documentation = documentation.mutable(),
    )

    data class Mutable(
        var verifyCaseForReservedWords: Boolean,
        var verifyUsedTableAliasSeparator: Boolean,
        var fallbackToTableNameIfNoAliasProvided: Boolean,
        var defaultCaseForReservedWords: ReservedWordsCase,
        var completion: FlexibleSearchCompletionSettingsState.Mutable,
        var folding: FlexibleSearchFoldingSettingsState.Mutable,
        var documentation: FlexibleSearchDocumentationSettingsState.Mutable,
    ) {
        fun immutable() = FlexibleSearchSettingsState(
            verifyCaseForReservedWords = verifyCaseForReservedWords,
            verifyUsedTableAliasSeparator = verifyUsedTableAliasSeparator,
            fallbackToTableNameIfNoAliasProvided = fallbackToTableNameIfNoAliasProvided,
            defaultCaseForReservedWords = defaultCaseForReservedWords,
            completion = completion.immutable(),
            folding = folding.immutable(),
            documentation = documentation.immutable(),
        )
    }
}
