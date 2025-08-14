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

package com.intellij.idea.plugin.hybris.settings.state

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("FlexibleSearchSettings")
data class FlexibleSearchSettings(
    @JvmField @OptionTag val verifyCaseForReservedWords: Boolean = true,
    @JvmField @OptionTag val verifyUsedTableAliasSeparator: Boolean = true,
    @JvmField @OptionTag val fallbackToTableNameIfNoAliasProvided: Boolean = true,
    @JvmField @OptionTag val defaultCaseForReservedWords: ReservedWordsCase = ReservedWordsCase.UPPERCASE,

    @JvmField @OptionTag val completion: FlexibleSearchCompletionSettings = FlexibleSearchCompletionSettings(),
    @JvmField @OptionTag val folding: FlexibleSearchFoldingSettings = FlexibleSearchFoldingSettings(),
    @JvmField @OptionTag val documentation: FlexibleSearchDocumentationSettings = FlexibleSearchDocumentationSettings(),
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
        var completion: FlexibleSearchCompletionSettings.Mutable,
        var folding: FlexibleSearchFoldingSettings.Mutable,
        var documentation: FlexibleSearchDocumentationSettings.Mutable,
    ) {
        fun immutable() = FlexibleSearchSettings(
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

@Tag("FlexibleSearchCompletionSettings")
data class FlexibleSearchCompletionSettings(
    @JvmField @OptionTag val injectSpaceAfterKeywords: Boolean = true,
    @JvmField @OptionTag val injectTableAliasSeparator: Boolean = true,
    @JvmField @OptionTag val suggestTableAliasNames: Boolean = true,
    @JvmField @OptionTag val injectCommaAfterExpression: Boolean = true,
    @JvmField @OptionTag val defaultTableAliasSeparator: String = HybrisConstants.FXS_TABLE_ALIAS_SEPARATOR_DOT,
) {
    fun mutable() = Mutable(
        injectSpaceAfterKeywords = injectSpaceAfterKeywords,
        injectTableAliasSeparator = injectTableAliasSeparator,
        suggestTableAliasNames = suggestTableAliasNames,
        injectCommaAfterExpression = injectCommaAfterExpression,
        defaultTableAliasSeparator = defaultTableAliasSeparator,
    )

    data class Mutable(
        var injectSpaceAfterKeywords: Boolean,
        var injectTableAliasSeparator: Boolean,
        var suggestTableAliasNames: Boolean,
        var injectCommaAfterExpression: Boolean,
        var defaultTableAliasSeparator: String,
    ) {
        fun immutable() = FlexibleSearchCompletionSettings(
            injectSpaceAfterKeywords = injectSpaceAfterKeywords,
            injectTableAliasSeparator = injectTableAliasSeparator,
            suggestTableAliasNames = suggestTableAliasNames,
            injectCommaAfterExpression = injectCommaAfterExpression,
            defaultTableAliasSeparator = defaultTableAliasSeparator,
        )
    }
}

@Tag("FlexibleSearchDocumentationSettings")
data class FlexibleSearchDocumentationSettings(
    @JvmField @OptionTag val enabled: Boolean = true,
    @JvmField @OptionTag val showTypeDocumentation: Boolean = true,
) {
    fun mutable() = Mutable(
        enabled = enabled,
        showTypeDocumentation = showTypeDocumentation,
    )

    data class Mutable(
        override var enabled: Boolean,
        var showTypeDocumentation: Boolean,
    ) : FoldingSettings {
        fun immutable() = FlexibleSearchDocumentationSettings(
            enabled = enabled,
            showTypeDocumentation = showTypeDocumentation,
        )
    }
}

@Tag("FlexibleSearchFoldingSettings")
data class FlexibleSearchFoldingSettings(
    @OptionTag override val enabled: Boolean = true,
    @JvmField @OptionTag val showSelectedTableNameForYColumn: Boolean = true,
    @JvmField @OptionTag val showLanguageForYColumn: Boolean = true,
) : FoldingSettings {

    fun mutable() = Mutable(
        enabled = enabled,
        showSelectedTableNameForYColumn = showSelectedTableNameForYColumn,
        showLanguageForYColumn = showLanguageForYColumn,
    )

    data class Mutable(
        override var enabled: Boolean,
        var showSelectedTableNameForYColumn: Boolean,
        var showLanguageForYColumn: Boolean,
    ) : FoldingSettings {
        fun immutable() = FlexibleSearchFoldingSettings(
            enabled = enabled,
            showSelectedTableNameForYColumn = showSelectedTableNameForYColumn,
            showLanguageForYColumn = showLanguageForYColumn,
        )
    }
}

