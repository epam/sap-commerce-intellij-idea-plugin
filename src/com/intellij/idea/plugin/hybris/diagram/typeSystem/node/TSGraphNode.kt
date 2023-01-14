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

package com.intellij.idea.plugin.hybris.diagram.typeSystem.node

import com.intellij.diagram.DiagramNodeBase
import com.intellij.diagram.DiagramProvider
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import javax.swing.Icon

class TSGraphNode(val item: TSGraphItem, provider: DiagramProvider<TSGraphItem>) : DiagramNodeBase<TSGraphItem>(provider) {

    override fun getIcon(): Icon {
        val meta = item.meta ?: return HybrisIcons.TYPE_SYSTEM

        return when (meta) {
            is TSGlobalMetaAtomic -> HybrisIcons.ATOMIC
            is TSGlobalMetaEnum -> HybrisIcons.ENUM
            is TSGlobalMetaItem -> HybrisIcons.ITEM
            is TSGlobalMetaCollection -> HybrisIcons.COLLECTION
            is TSGlobalMetaMap -> HybrisIcons.MAP
            is TSGlobalMetaRelation -> HybrisIcons.RELATION
            else -> return HybrisIcons.TYPE_SYSTEM
        }
    }

    override fun getIdentifyingElement() = item
    override fun getTooltip() = item.meta?.name ?: "Root Type"

    companion object {
        private const val serialVersionUID: Long = -8508256123440006334L
    }
}