/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes

class TSMetaItemAttributeNode(val parent: TSMetaItemNode, meta: TSGlobalMetaItem.TSGlobalMetaItemAttribute) : TSMetaNode<TSGlobalMetaItem.TSGlobalMetaItemAttribute>(parent, meta) {

    override fun getName() = meta.name

    override fun update(project: Project, presentation: PresentationData) {
        presentation.setIcon(HybrisIcons.TypeSystem.ATTRIBUTE)
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.locationString = meta.flattenType
    }

}