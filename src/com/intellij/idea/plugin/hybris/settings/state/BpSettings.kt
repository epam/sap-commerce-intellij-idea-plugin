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

@Tag("BpSettings")
data class BpSettings(
    @JvmField @OptionTag val folding: BpFoldingSettings = BpFoldingSettings(),
) {
    fun mutable() = Mutable(
        folding = folding.mutable()
    )

    data class Mutable(
        var folding: BpFoldingSettings.Mutable,
    ) {
        fun immutable() = BpSettings(
            folding = folding.immutable()
        )
    }
}

@Tag("BpFoldingSettings")
data class BpFoldingSettings(
    @OptionTag override val enabled: Boolean = true,
    @JvmField @OptionTag val tablifyActionTransitions: Boolean = true,
    @JvmField @OptionTag val tablifyEnds: Boolean = true,
    @JvmField @OptionTag val tablifyCaseChoices: Boolean = true,
) : FoldingSettings {

    fun mutable() = Mutable(
        enabled = enabled,
        tablifyActionTransitions = tablifyActionTransitions,
        tablifyEnds = tablifyEnds,
        tablifyCaseChoices = tablifyCaseChoices,
    )

    data class Mutable(
        override var enabled: Boolean,
        var tablifyActionTransitions: Boolean,
        var tablifyEnds: Boolean,
        var tablifyCaseChoices: Boolean,
    ) : FoldingSettings {
        fun immutable() = BpFoldingSettings(
            enabled = enabled,
            tablifyActionTransitions = tablifyActionTransitions,
            tablifyEnds = tablifyEnds,
            tablifyCaseChoices = tablifyCaseChoices,
        )
    }
}
