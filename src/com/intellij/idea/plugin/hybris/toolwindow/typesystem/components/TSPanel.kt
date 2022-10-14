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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.components

import com.intellij.idea.plugin.hybris.toolwindow.TSMetaItemView
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.view.TSViewSettings
import com.intellij.idea.plugin.hybris.type.system.meta.MetaType
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.FinderRecursivePanel
import javax.swing.JComponent

class TSPanel(
    private val myProject: Project,
    private val myActionGroup: DefaultActionGroup,
    myGroupId: String = "HybrisTypeSystemPanel"
) : FinderRecursivePanel<TSMetaItem>(myProject, myGroupId) {

    init {
        isNonBlockingLoad = true
    }

    override fun getListItems(): List<TSMetaItem> {
        val sortedBy = ArrayList(
            TSMetaModelAccess.getInstance(myProject).metaModel.getMetaType<TSMetaItem>(MetaType.META_ITEM).values
        )
            .sortedBy { it.name }

        return sortedBy;
    }

    override fun hasChildren(t: TSMetaItem): Boolean = false

    override fun getItemText(t: TSMetaItem): String = t.name!!

    override fun createRightComponent(t: TSMetaItem): JComponent? = TSMetaItemView.create(myProject, t)

    private fun settings(): TSViewSettings = TSViewSettings.getInstance(myProject)
}