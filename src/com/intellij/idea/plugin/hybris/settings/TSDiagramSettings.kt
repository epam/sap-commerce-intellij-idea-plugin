/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.common.HybrisConstants

data class TSDiagramSettings(
    var nodesCollapsedByDefault: Boolean = true,
    var showOOTBMapNodes: Boolean = false,
    var showCustomAtomicNodes: Boolean = false,
    var showCustomCollectionNodes: Boolean = false,
    var showCustomEnumNodes: Boolean = false,
    var showCustomMapNodes: Boolean = false,
    var showCustomRelationNodes: Boolean = false,
    var excludedTypeNames: Set<String> = setOf(
        HybrisConstants.TS_TYPE_ITEM,
        HybrisConstants.TS_TYPE_GENERIC_ITEM,
        HybrisConstants.TS_TYPE_LOCALIZABLE_ITEM,
        HybrisConstants.TS_TYPE_EXTENSIBLE_ITEM,
        HybrisConstants.TS_TYPE_CRON_JOB
    )
)
