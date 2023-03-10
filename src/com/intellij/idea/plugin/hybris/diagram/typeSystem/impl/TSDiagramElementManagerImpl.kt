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

package com.intellij.idea.plugin.hybris.diagram.typeSystem.impl

import com.intellij.diagram.AbstractDiagramElementManager
import com.intellij.idea.plugin.hybris.actions.ActionUtils
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.diagram.typeSystem.TSDiagramElementManager
import com.intellij.idea.plugin.hybris.diagram.typeSystem.node.TSGraphItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.openapi.actionSystem.DataContext

class TSDiagramElementManagerImpl : AbstractDiagramElementManager<TSGraphItem>(), TSDiagramElementManager {

    override fun findInDataContext(dataContext: DataContext): TSGraphItem? {
        if (!ActionUtils.isHybrisContext(dataContext)) return null

        return TSGraphItem()
    }

    override fun findElementsInDataContext(context: DataContext) = if (ActionUtils.isHybrisContext(context))
        mutableListOf(TSGraphItem())
    else mutableListOf()

    override fun isAcceptableAsNode(item: Any?) = item is TSGraphItem

    override fun getNodeTooltip(node: TSGraphItem?): String? {
        return node?.meta?.let {
            """
            <html>
                <body>
                    <p><strong>Module</strong>: ${it.module.name}</p>
                    <p><strong>Custom</strong>: ${if (it.isCustom) "Yes" else "No"}</p>
                </body>
            </html>
        """.trimIndent()
        }
    }

    override fun canCollapse(item: TSGraphItem?) = item != null


    override fun getNodeItems(parent: TSGraphItem?): Array<Any> {
        if (true) return emptyArray()
        // TODO: implement attributes
        if (parent?.meta == null) return emptyArray()

        return when (val meta = parent.meta) {
            is TSGlobalMetaItem -> meta.attributes.values
                .map { it.name }
                .toTypedArray()

            else -> emptyArray()
        }
    }

    override fun getElementTitle(node: TSGraphItem?) = node?.meta?.name ?: HybrisConstants.TS_TYPE_ITEM

}
