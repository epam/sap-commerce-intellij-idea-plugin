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

package com.intellij.idea.plugin.hybris.toolwindow.system.type.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.toolwindow.system.type.view.TSViewSettings
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes

class TSMetaItemNode(parent: TSNode, val meta: TSGlobalMetaItem) : TSNode(parent), Disposable {

    override fun dispose() = Unit
    override fun getName() = meta.name ?: "-- no name --"

    override fun update(project: Project, presentation: PresentationData) {
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(HybrisIcons.ITEM)
        presentation.locationString = ": ${meta.extendedMetaItemName ?: HybrisConstants.TS_TYPE_GENERIC_ITEM}"
    }

    override fun getChildren(): Collection<TSNode> {
        val showOnlyCustom = TSViewSettings.getInstance(myProject).isShowOnlyCustom()

        val indexes = meta.indexes.values
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map { TSMetaItemIndexNode(this, it) }
            .sortedBy { it.name }

        val customProperties = meta.customProperties.values
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map { TSMetaItemCustomPropertyNode(this, it) }
            .sortedBy { it.name }

        val attributes = meta.attributes.values
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map { TSMetaItemAttributeNode(this, it) }
            .sortedBy { it.name }

        return indexes + customProperties + attributes
    }

}