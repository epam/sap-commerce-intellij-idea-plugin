/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.diagram.typeSystem

import com.intellij.diagram.AbstractUmlVisibilityManager
import com.intellij.diagram.VisibilityLevel
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.graph.*
import com.intellij.util.ArrayUtil

class TSDiagramVisibilityManager : AbstractUmlVisibilityManager() {

    init {
        currentVisibilityLevel = LEVEL_ONLY_CUSTOM_FIELDS
    }

    override fun getVisibilityLevels() = LEVELS.clone()
    override fun getVisibilityLevel(o: Any?) = when (o) {
        is TSGraphFieldEnumValue -> if (o.meta.isCustom) LEVEL_ONLY_CUSTOM_FIELDS else LEVEL_ALL_FIELDS
        is TSGraphFieldIndex -> if (o.meta.isCustom) LEVEL_ONLY_CUSTOM_FIELDS else LEVEL_ALL_FIELDS
        is TSGraphFieldAttribute -> if (o.meta.isCustom) LEVEL_ONLY_CUSTOM_FIELDS else LEVEL_ALL_FIELDS
        is TSGraphFieldRelationEnd -> if (o.meta.isCustom) LEVEL_ONLY_CUSTOM_FIELDS else LEVEL_ALL_FIELDS
        is TSGraphFieldRelationElement -> if (o.meta.isCustom || o.meta.owner.isCustom) LEVEL_ONLY_CUSTOM_FIELDS else LEVEL_ALL_FIELDS
        is TSGraphFieldCustomProperty -> if (o.meta.isCustom) LEVEL_ONLY_CUSTOM_FIELDS else LEVEL_ALL_FIELDS
        else -> null
    }

    override fun getComparator(): Comparator<VisibilityLevel?> = COMPARATOR
    override fun isRelayoutNeeded() = true

    companion object {
        private const val ONLY_CUSTOM_FIELDS = "ONLY_CUSTOM_FIELDS"
        private const val ALL_FIELDS = "ALL_FIELDS"

        private val LEVEL_ONLY_CUSTOM_FIELDS = VisibilityLevel(ONLY_CUSTOM_FIELDS, message("hybris.diagram.ts.provider.visibility.only_custom_fields"))
        private val LEVEL_ALL_FIELDS = VisibilityLevel(ALL_FIELDS, message("hybris.diagram.ts.provider.visibility.all_fields"))
        private val LEVELS = arrayOf(LEVEL_ONLY_CUSTOM_FIELDS, LEVEL_ALL_FIELDS)
        private val COMPARATOR = Comparator.comparingInt { level: VisibilityLevel? -> ArrayUtil.indexOf(LEVELS, level) }
    }
}
