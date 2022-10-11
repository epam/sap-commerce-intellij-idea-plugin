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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.panels

import com.intellij.idea.plugin.hybris.toolwindow.TSMetaClassView
import com.intellij.idea.plugin.hybris.toolwindow.typesystem.view.TSViewSettings
import com.intellij.idea.plugin.hybris.type.system.meta.MetaType
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.FinderRecursivePanel
import javax.swing.JComponent

class TSPanel(
    private val myProject: Project,
    private val myActionGroup: DefaultActionGroup,
    myGroupId: String = "HybrisTypeSystemPanel"
) : FinderRecursivePanel<TSMetaClass>(myProject, myGroupId) {

    init {
        isNonBlockingLoad = true
    }

    override fun getListItems(): List<TSMetaClass> {
        val sortedBy = ArrayList(
            TSMetaModelService.getInstance(myProject).metaModel().getMetaType<TSMetaClass>(MetaType.META_CLASS).values
        )
            .sortedBy { it.name }

        return sortedBy;
    }

    override fun hasChildren(t: TSMetaClass): Boolean = false

    override fun getItemText(t: TSMetaClass): String = t.name

    override fun createRightComponent(t: TSMetaClass): JComponent? {
        return TSMetaClassView.create(myProject, t)
    }

    private fun settings(): TSViewSettings = TSViewSettings.getInstance(myProject)
}