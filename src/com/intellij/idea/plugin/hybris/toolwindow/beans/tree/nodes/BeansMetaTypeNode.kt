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

package com.intellij.idea.plugin.hybris.toolwindow.beans.tree.nodes

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaModelAccess
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaEnum
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaType
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.toolwindow.beans.view.BeansViewSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.xml.DomElement
import java.util.*

class BeansMetaTypeNode(parent: BeansNode, private val metaType: BeansMetaType) : BeansNode(parent), Disposable {

    override fun dispose() = Unit
    override fun getName() = HybrisI18NBundleUtils.message("hybris.toolwindow.beans.group.${metaType.name.lowercase()}.name")

    override fun update(project: Project, presentation: PresentationData) {
        when (metaType) {
            BeansMetaType.META_ENUM -> presentation.setIcon(AllIcons.Actions.GroupByTestProduction)
            else -> {}
        }
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)


        val showOnlyCustom = BeansViewSettings.getInstance(myProject).isShowOnlyCustom()
        val entries = BeansMetaModelAccess.getInstance(myProject).getMetaModel().getMetaType<BeansGlobalMetaClassifier<DomElement>>(metaType).values
            .filter { if (showOnlyCustom) it.isCustom else true }
            .size
        if (entries > 0) {
            presentation.locationString = "$entries"
        }
    }

    override fun getChildren(): Collection<BeansNode?> {
        val showOnlyCustom = BeansViewSettings.getInstance(myProject).isShowOnlyCustom()

        return BeansMetaModelAccess.getInstance(myProject).getMetaModel()
            .getMetaType<BeansGlobalMetaClassifier<DomElement>>(metaType).values
            .filter { if (showOnlyCustom) it.isCustom else true }
            .map {
                when (it) {
                    is BeansGlobalMetaEnum -> BeansMetaEnumNode(this, it)
                    else -> null
                }
            }
            .filter { Objects.nonNull(it) }
            .sortedBy { it!!.name }
    }

}