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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSViewSettings
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import sap.commerce.toolset.HybrisIcons

class BSMetaEnumNode(val parent: BSNode, meta: BSGlobalMetaEnum) : BSMetaNode<BSGlobalMetaEnum>(parent, meta) {

    override fun getName() = meta.shortName ?: "-- no name --"

    override fun update(project: Project, presentation: PresentationData) {
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.setIcon(HybrisIcons.BeanSystem.ENUM)
        if (meta.isDeprecated) {
            presentation.locationString = "deprecated"
        }
    }

    override fun getNewChildren(): Map<String, BSNode> = if (BSViewSettings.getInstance(project).isShowEnumValues()) meta.values.values
        .filter { it.isCustom }
        .map { BSMetaEnumValueNode(this, it) }
        .associateBy { it.name }
    else emptyMap()
}