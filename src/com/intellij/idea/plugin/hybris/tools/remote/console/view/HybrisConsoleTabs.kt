/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.console.view

import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBTabsPaneImpl
import com.intellij.ui.tabs.impl.JBEditorTabs
import com.intellij.util.asSafely
import javax.swing.Icon

class HybrisConsoleTabs(project: Project, tabPlacement: Int, defaultConsoles: Array<HybrisConsole>, disposable: Disposable) : JBTabsPaneImpl(project, tabPlacement, disposable) {

    private val consoles = arrayListOf<HybrisConsole>()

    init {
        defaultConsoles.forEach {
            addConsoleTab(it.title(), it.icon(), it, it.tip())
        }

        addChangeListener {
            it.source.asSafely<JBEditorTabs>()
                ?.selectedInfo
                ?.component.asSafely<HybrisConsole>()
                ?.onSelection()
        }
    }

    private fun addConsoleTab(title: String, icon: Icon?, console: HybrisConsole, tip: String) {
        insertTab(title, icon, console.component, tip, consoles.size)
        consoles.add(console)
    }

    fun activeConsole() = consoles[selectedIndex]
    fun setActiveConsole(console: HybrisConsole) {
        selectedIndex = consoles.indexOf(console)
    }
}