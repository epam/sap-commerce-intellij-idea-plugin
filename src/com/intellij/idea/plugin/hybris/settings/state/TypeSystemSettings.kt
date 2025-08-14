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
import kotlinx.collections.immutable.toImmutableSet

@Tag("TypeSystemSettings")
data class TypeSystemSettings(
    @JvmField @OptionTag val folding: TypeSystemFoldingSettings = TypeSystemFoldingSettings(),
) {

    fun mutable() = Mutable(
        folding = folding.mutable(),
    )

    data class Mutable(
        var folding: TypeSystemFoldingSettings.Mutable,
    ) {
        fun immutable() = TypeSystemSettings(
            folding = folding.immutable(),
        )
    }
}

@Tag("TypeSystemFoldingSettings")
data class TypeSystemFoldingSettings(
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
        fun immutable() = TypeSystemFoldingSettings(
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

@Tag("TSDiagramSettings")
data class TypeSystemDiagramSettings(
    @JvmField @OptionTag val nodesCollapsedByDefault: Boolean = true,
    @JvmField @OptionTag val showOOTBMapNodes: Boolean = false,
    @JvmField @OptionTag val showCustomAtomicNodes: Boolean = false,
    @JvmField @OptionTag val showCustomCollectionNodes: Boolean = false,
    @JvmField @OptionTag val showCustomEnumNodes: Boolean = false,
    @JvmField @OptionTag val showCustomMapNodes: Boolean = false,
    @JvmField @OptionTag val showCustomRelationNodes: Boolean = false,
    @JvmField val excludedTypeNames: Set<String> = setOf(
        HybrisConstants.TS_TYPE_ITEM,
        HybrisConstants.TS_TYPE_GENERIC_ITEM,
        HybrisConstants.TS_TYPE_LOCALIZABLE_ITEM,
        HybrisConstants.TS_TYPE_EXTENSIBLE_ITEM,
        HybrisConstants.TS_TYPE_CRON_JOB
    )
) {

    fun mutable() = Mutable(
        nodesCollapsedByDefault = nodesCollapsedByDefault,
        showOOTBMapNodes = showOOTBMapNodes,
        showCustomAtomicNodes = showCustomAtomicNodes,
        showCustomCollectionNodes = showCustomCollectionNodes,
        showCustomEnumNodes = showCustomEnumNodes,
        showCustomMapNodes = showCustomMapNodes,
        showCustomRelationNodes = showCustomRelationNodes,
        excludedTypeNames = excludedTypeNames.toMutableSet(),
    )

    data class Mutable(
        var nodesCollapsedByDefault: Boolean,
        var showOOTBMapNodes: Boolean,
        var showCustomAtomicNodes: Boolean,
        var showCustomCollectionNodes: Boolean,
        var showCustomEnumNodes: Boolean,
        var showCustomMapNodes: Boolean,
        var showCustomRelationNodes: Boolean,
        var excludedTypeNames: MutableSet<String>,
    ) {
        fun immutable() = TypeSystemDiagramSettings(
            nodesCollapsedByDefault = nodesCollapsedByDefault,
            showOOTBMapNodes = showOOTBMapNodes,
            showCustomAtomicNodes = showCustomAtomicNodes,
            showCustomCollectionNodes = showCustomCollectionNodes,
            showCustomEnumNodes = showCustomEnumNodes,
            showCustomMapNodes = showCustomMapNodes,
            showCustomRelationNodes = showCustomRelationNodes,
            excludedTypeNames = excludedTypeNames.toImmutableSet(),
        )
    }
}