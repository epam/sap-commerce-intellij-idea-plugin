/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.meta.model.*
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSViewSettings
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.xml.DomElement

@Suppress("UNCHECKED_CAST")
class BSMetaTypeNode(parent: BSNode, private val metaType: BSMetaType) : BSNode(parent) {

    override fun getName() = HybrisI18NBundleUtils.message("hybris.toolwindow.beans.group.${metaType.name.lowercase()}.name")

    override fun update(existingNode: BSNode, newNode: BSNode) {
        val current = existingNode as? BSMetaNode<BSMetaClassifier<out DomElement>> ?: return
        val new = newNode as? BSMetaNode<*> ?: return
        current.meta = new.meta
    }

    override fun update(project: Project, presentation: PresentationData) {
        when (metaType) {
            BSMetaType.META_ENUM -> presentation.setIcon(HybrisIcons.BS_GROUP_BY_ENUM)
            BSMetaType.META_BEAN -> presentation.setIcon(HybrisIcons.BS_GROUP_BY_BEAN_DTO)
            BSMetaType.META_WS_BEAN -> presentation.setIcon(HybrisIcons.BS_GROUP_BY_BEAN_WS)
            BSMetaType.META_EVENT -> presentation.setIcon(HybrisIcons.BS_GROUP_BY_BEAN_EVENT)
        }
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val settings = BSViewSettings.getInstance(myProject)
        val entries = globalMetaModel
            ?.getMetaType<BSGlobalMetaClassifier<DomElement>>(metaType)
            ?.values
            ?.filter { if (settings.isShowOnlyCustom()) it.isCustom else true }
            ?.filter { if (settings.isShowOnlyDeprecated()) BSMetaHelper.isDeprecated(it) else true }
            ?.size
            ?: 0
        if (entries > 0) {
            presentation.locationString = "$entries"
        }
    }

    override fun getNewChildren(): Map<String, BSNode> {
        val settings = BSViewSettings.getInstance(myProject)

        return globalMetaModel
            ?.getMetaType<BSGlobalMetaClassifier<DomElement>>(metaType)
            ?.values
            ?.filter { if (settings.isShowOnlyCustom()) it.isCustom else true }
            ?.filter { if (settings.isShowOnlyDeprecated()) BSMetaHelper.isDeprecated(it) else true }
            ?.mapNotNull {
                when (it) {
                    is BSGlobalMetaEnum -> BSMetaEnumNode(this, it)
                    is BSGlobalMetaBean -> BSMetaBeanNode(this, it)
                    else -> null
                }
            }
            ?.associateBy { it.name }
            ?: emptyMap()
    }

}