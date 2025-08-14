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

package com.intellij.idea.plugin.hybris.system.cockpitng.settings.state

import com.intellij.idea.plugin.hybris.settings.state.FoldingSettings
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("CngFoldingSettings")
data class CngFoldingSettingsState(
    @OptionTag override val enabled: Boolean = true,
    @JvmField @OptionTag val tablifyWizardProperties: Boolean = true,
    @JvmField @OptionTag val tablifyNavigationNodes: Boolean = true,
    @JvmField @OptionTag val tablifySearchFields: Boolean = true,
    @JvmField @OptionTag val tablifyListColumns: Boolean = true,
    @JvmField @OptionTag val tablifyParameters: Boolean = true,
    @JvmField @OptionTag val tablifyMolds: Boolean = true,
) : FoldingSettings {

    fun mutable() = Mutable(
        enabled = enabled,
        tablifyWizardProperties = tablifyWizardProperties,
        tablifyNavigationNodes = tablifyNavigationNodes,
        tablifySearchFields = tablifySearchFields,
        tablifyListColumns = tablifyListColumns,
        tablifyParameters = tablifyParameters,
        tablifyMolds = tablifyMolds,
    )

    data class Mutable(
        override var enabled: Boolean,
        var tablifyWizardProperties: Boolean,
        var tablifyNavigationNodes: Boolean,
        var tablifySearchFields: Boolean,
        var tablifyListColumns: Boolean,
        var tablifyParameters: Boolean,
        var tablifyMolds: Boolean,
    ) : FoldingSettings {
        fun immutable() = CngFoldingSettingsState(
            enabled = enabled,
            tablifyWizardProperties = tablifyWizardProperties,
            tablifyNavigationNodes = tablifyNavigationNodes,
            tablifySearchFields = tablifySearchFields,
            tablifyListColumns = tablifyListColumns,
            tablifyParameters = tablifyParameters,
            tablifyMolds = tablifyMolds,
        )
    }
}