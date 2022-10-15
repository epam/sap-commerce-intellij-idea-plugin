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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.ui.components.JBTabbedPane
import javax.swing.JTabbedPane

private val ACTIVE_TAB_INDEX = Key.create<Int>("TS_META_ITEM_VIEW_ACTIVE_INDEX")

class TSMetaItemTabbedPane(private val myProject: Project, private val myMeta: TSMetaItem) : JBTabbedPane() {

    fun init() {
        // We have to check user data before registering new listener to ensure that first tab will not be always preselected
        val previouslySelectedTabIndex = myProject.getUserData(ACTIVE_TAB_INDEX)
        if (previouslySelectedTabIndex != null) {
            selectedIndex = previouslySelectedTabIndex
        }

        addChangeListener { e ->
            val source = e.source
            if (source is JTabbedPane) {
                myProject.putUserData(
                    ACTIVE_TAB_INDEX,
                    source.selectedIndex
                )
            }
        }
    }

    companion object {
        private const val serialVersionUID: Long = -6237670566507887697L
    }
}