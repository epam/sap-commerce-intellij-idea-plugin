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

@Tag("TypeSystemFoldingSettings")
data class TypeSystemFoldingSettingsState(
    @OptionTag override val enabled: Boolean = true,
    @JvmField @OptionTag val tablifyAtomics: Boolean = true,
    @JvmField @OptionTag val tablifyCollections: Boolean = true,
    @JvmField @OptionTag val tablifyMaps: Boolean = true,
    @JvmField @OptionTag val tablifyRelations: Boolean = true,
    @JvmField @OptionTag val tablifyItemAttributes: Boolean = true,
    @JvmField @OptionTag val tablifyItemIndexes: Boolean = true,
    @JvmField @OptionTag val tablifyItemCustomProperties: Boolean = true,
) : FoldingSettings {

    fun mutable() = Mutable(
        enabled = enabled,
        tablifyAtomics = tablifyAtomics,
        tablifyCollections = tablifyCollections,
        tablifyMaps = tablifyMaps,
        tablifyRelations = tablifyRelations,
        tablifyItemAttributes = tablifyItemAttributes,
        tablifyItemIndexes = tablifyItemIndexes,
        tablifyItemCustomProperties = tablifyItemCustomProperties,
    )

    data class Mutable(
        override var enabled: Boolean,
        var tablifyAtomics: Boolean,
        var tablifyCollections: Boolean,
        var tablifyMaps: Boolean,
        var tablifyRelations: Boolean,
        var tablifyItemAttributes: Boolean,
        var tablifyItemIndexes: Boolean,
        var tablifyItemCustomProperties: Boolean,
    ) : FoldingSettings {
        fun immutable() = TypeSystemFoldingSettingsState(
            enabled = enabled,
            tablifyAtomics = tablifyAtomics,
            tablifyCollections = tablifyCollections,
            tablifyMaps = tablifyMaps,
            tablifyRelations = tablifyRelations,
            tablifyItemAttributes = tablifyItemAttributes,
            tablifyItemIndexes = tablifyItemIndexes,
            tablifyItemCustomProperties = tablifyItemCustomProperties,
        )
    }
}