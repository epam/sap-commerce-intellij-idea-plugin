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

data class ImpexSettings(
    @JvmField @OptionTag val groupLocalizedFiles: Boolean = true,
    @JvmField val editMode: ImpExEditModeSettings = ImpExEditModeSettings(),
    @JvmField val folding: ImpexFoldingSettings = ImpexFoldingSettings(),
    @JvmField val completion: ImpexCompletionSettings = ImpexCompletionSettings(),
    @JvmField val documentation: ImpexDocumentationSettings = ImpexDocumentationSettings(),
) {
    fun mutable() = Mutable(
        groupLocalizedFiles = groupLocalizedFiles,
        editMode = editMode.mutable(),
        folding = folding.mutable(),
        completion = completion.mutable(),
        documentation = documentation.mutable(),
    )

    data class Mutable(
        var groupLocalizedFiles: Boolean,
        var editMode: ImpExEditModeSettings.Mutable,
        var folding: ImpexFoldingSettings.Mutable,
        var completion: ImpexCompletionSettings.Mutable,
        var documentation: ImpexDocumentationSettings.Mutable,
    ) {
        fun immutable() = ImpexSettings(
            groupLocalizedFiles = groupLocalizedFiles,
            editMode = editMode.immutable(),
            folding = folding.immutable(),
            completion = completion.immutable(),
            documentation = documentation.immutable(),
        )
    }
}

data class ImpExEditModeSettings(
    @JvmField @OptionTag val firstRowIsHeader: Boolean = true,
    @JvmField @OptionTag val trimWhitespace: Boolean = false,
) {
    fun mutable() = Mutable(
        firstRowIsHeader = firstRowIsHeader,
        trimWhitespace = trimWhitespace,
    )

    data class Mutable(
        var firstRowIsHeader: Boolean,
        var trimWhitespace: Boolean,
    ) {
        fun immutable() = ImpExEditModeSettings(
            firstRowIsHeader = firstRowIsHeader,
            trimWhitespace = trimWhitespace,
        )
    }
}

data class ImpexFoldingSettings(
    @JvmField @OptionTag val enabled: Boolean = true,
    @JvmField @OptionTag val useSmartFolding: Boolean = true,
    @JvmField @OptionTag val foldMacroInParameters: Boolean = true,
) {
    fun mutable() = Mutable(
        enabled = enabled,
        useSmartFolding = useSmartFolding,
        foldMacroInParameters = foldMacroInParameters,
    )

    data class Mutable(
        var enabled: Boolean,
        var useSmartFolding: Boolean,
        var foldMacroInParameters: Boolean,
    ) {
        fun immutable() = ImpexFoldingSettings(
            enabled = enabled,
            useSmartFolding = useSmartFolding,
            foldMacroInParameters = foldMacroInParameters,
        )
    }
}

data class ImpexDocumentationSettings(
    @JvmField @OptionTag val enabled: Boolean = true,
    @JvmField @OptionTag val showTypeDocumentation: Boolean = true,
    @JvmField @OptionTag val showModifierDocumentation: Boolean = true,
) {
    fun mutable() = Mutable(
        enabled = enabled,
        showTypeDocumentation = showTypeDocumentation,
        showModifierDocumentation = showModifierDocumentation,
    )

    data class Mutable(
        var enabled: Boolean,
        var showTypeDocumentation: Boolean,
        var showModifierDocumentation: Boolean,
    ) {
        fun immutable() = ImpexDocumentationSettings(
            enabled = enabled,
            showTypeDocumentation = showTypeDocumentation,
            showModifierDocumentation = showModifierDocumentation,
        )
    }
}

data class ImpexCompletionSettings(
    @JvmField @OptionTag val showInlineTypes: Boolean = true,
    @JvmField @OptionTag val addCommaAfterInlineType: Boolean = true,
    @JvmField @OptionTag val addEqualsAfterModifier: Boolean = true,
) {
    fun mutable() = Mutable(
        showInlineTypes = showInlineTypes,
        addCommaAfterInlineType = addCommaAfterInlineType,
        addEqualsAfterModifier = addEqualsAfterModifier,
    )

    data class Mutable(
        var showInlineTypes: Boolean,
        var addCommaAfterInlineType: Boolean,
        var addEqualsAfterModifier: Boolean,
    ) {
        fun immutable() = ImpexCompletionSettings(
            showInlineTypes = showInlineTypes,
            addCommaAfterInlineType = addCommaAfterInlineType,
            addEqualsAfterModifier = addEqualsAfterModifier,
        )
    }
}
