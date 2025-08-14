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

package com.intellij.idea.plugin.hybris.impex.settings.state

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("ImpexCompletionSettings")
data class ImpExCompletionSettingsState(
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
        fun immutable() = ImpExCompletionSettingsState(
            showInlineTypes = showInlineTypes,
            addCommaAfterInlineType = addCommaAfterInlineType,
            addEqualsAfterModifier = addEqualsAfterModifier,
        )
    }
}