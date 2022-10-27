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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.tree.nodes

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.view.TSViewSettings
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes

class TSMetaItemNode(parent: TSNode, val meta: TSMetaItem) : TSNode(parent), Disposable {

    override fun dispose() = Unit
    override fun getName() = meta.name ?: "-- no name --"

    override fun update(project: Project, presentation: PresentationData) {
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(AllIcons.Nodes.Class)
        presentation.locationString = "extends ${meta.extendedMetaItemName ?: TSMetaItem.IMPLICIT_SUPER_CLASS_NAME}"
    }

    override fun getChildren(): Collection<TSNode> {
        val metaItemService = TSMetaItemService.getInstance(myProject)
        val showOnlyCustom = TSViewSettings.getInstance(myProject).isShowOnlyCustom()

        val indexes = metaItemService.getIndexes(meta,false)
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map { TSMetaItemIndexNode(this, it) }
            .sortedBy { it.name }

        val customProperties = metaItemService.getCustomProperties(meta,false)
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map { TSMetaItemCustomPropertyNode(this, it) }
            .sortedBy { it.name }

        val attributes = metaItemService.getAttributes(meta, false)
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map { TSMetaItemAttributeNode(this, it) }
            .sortedBy { it.name }

        return indexes + customProperties + attributes
    }

}