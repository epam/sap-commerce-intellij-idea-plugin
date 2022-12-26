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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.tree.nodes

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSViewSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.xml.DomElement
import java.util.*

class BSMetaTypeNode(parent: BSNode, private val metaType: BSMetaType) : BSNode(parent), Disposable {

    override fun dispose() = Unit
    override fun getName() = HybrisI18NBundleUtils.message("hybris.toolwindow.beans.group.${metaType.name.lowercase()}.name")

    override fun update(project: Project, presentation: PresentationData) {
        when (metaType) {
            BSMetaType.META_ENUM -> presentation.setIcon(AllIcons.Actions.GroupByTestProduction)
            BSMetaType.META_BEAN -> presentation.setIcon(HybrisIcons.GROUP_BY_BEAN_DTO)
            BSMetaType.META_WS_BEAN -> presentation.setIcon(HybrisIcons.GROUP_BY_BEAN_WS)
            BSMetaType.META_EVENT -> presentation.setIcon(HybrisIcons.GROUP_BY_BEAN_EVENT)
        }
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val settings = BSViewSettings.getInstance(myProject)
        val entries = BSMetaModelAccess.getInstance(myProject).getMetaModel().getMetaType<BSGlobalMetaClassifier<DomElement>>(metaType).values
            .filter { if (settings.isShowOnlyCustom()) it.isCustom else true }
            .filter { if (settings.isShowOnlyDeprecated()) BSMetaHelper.isDeprecated(it) else true }
            .size
        if (entries > 0) {
            presentation.locationString = "$entries"
        }
    }

    override fun getChildren(): Collection<BSNode?> {
        val settings = BSViewSettings.getInstance(myProject)

        return BSMetaModelAccess.getInstance(myProject).getMetaModel()
            .getMetaType<BSGlobalMetaClassifier<DomElement>>(metaType).values
            .asSequence()
            .filter { if (settings.isShowOnlyCustom()) it.isCustom else true }
            .filter { if (settings.isShowOnlyDeprecated()) BSMetaHelper.isDeprecated(it) else true }
            .map {
                when (it) {
                    is BSGlobalMetaEnum -> BSMetaEnumNode(this, it)
                    is BSGlobalMetaBean -> BSMetaBeanNode(this, it)
                    else -> null
                }
            }
            .filter { Objects.nonNull(it) }
            .sortedBy { it!!.name }
            .toList()
    }

}