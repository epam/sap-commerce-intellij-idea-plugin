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

package sap.commerce.toolset.settings.state

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("FlexibleSearchDocumentationSettings")
data class FlexibleSearchDocumentationSettingsState(
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
        fun immutable() = FlexibleSearchDocumentationSettingsState(
            enabled = enabled,
            showTypeDocumentation = showTypeDocumentation,
        )
    }
}