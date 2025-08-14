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

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("PolyglotQuerySettings")
data class PolyglotQuerySettings(
    @JvmField @OptionTag val verifyCaseForReservedWords: Boolean = true,
    @JvmField @OptionTag val defaultCaseForReservedWords: ReservedWordsCase = ReservedWordsCase.UPPERCASE,

    @JvmField @OptionTag val folding: PolyglotQueryFoldingSettings = PolyglotQueryFoldingSettings(),
) {

    fun mutable() = Mutable(
        verifyCaseForReservedWords = verifyCaseForReservedWords,
        defaultCaseForReservedWords = defaultCaseForReservedWords,
        folding = folding.mutable(),
    )

    data class Mutable(
        var verifyCaseForReservedWords: Boolean,
        var defaultCaseForReservedWords: ReservedWordsCase,
        var folding: PolyglotQueryFoldingSettings.Mutable,
    ) {
        fun immutable() = PolyglotQuerySettings(
            verifyCaseForReservedWords = verifyCaseForReservedWords,
            defaultCaseForReservedWords = defaultCaseForReservedWords,
            folding = folding.immutable(),
        )
    }
}

@Tag("PolyglotQueryFoldingSettings")
data class PolyglotQueryFoldingSettings(
    @OptionTag override val enabled: Boolean = true,
    @JvmField @OptionTag val showLanguage: Boolean = true,
) : FoldingSettings {

    fun mutable() = Mutable(
        enabled = enabled,
        showLanguage = showLanguage,
    )

    data class Mutable(
        override var enabled: Boolean,
        var showLanguage: Boolean,
    ) : FoldingSettings {
        fun immutable() = PolyglotQueryFoldingSettings(
            enabled = enabled,
            showLanguage = showLanguage,
        )
    }
}
