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

package com.intellij.idea.plugin.hybris.acl.settings.state

import com.intellij.idea.plugin.hybris.settings.state.FoldingSettings
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("AclFoldingSettings")
data class AclFoldingSettingsState(
    @OptionTag override val enabled: Boolean = true
) : FoldingSettings {
    fun mutable() = Mutable(
        enabled = enabled,
    )

    data class Mutable(
        var enabled: Boolean,
    ) {
        fun immutable() = AclFoldingSettingsState(
            enabled = enabled,
        )
    }
}
