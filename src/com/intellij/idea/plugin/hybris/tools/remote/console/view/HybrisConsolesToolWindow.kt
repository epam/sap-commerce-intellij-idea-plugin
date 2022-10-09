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

package com.intellij.idea.plugin.hybris.tools.remote.console.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content


/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */

class HybrisConsolesToolWindow(val project: Project) : Disposable {

    companion object {
        fun getInstance(project: Project): HybrisConsolesToolWindow = project.getService(HybrisConsolesToolWindow::class.java)
    }

    val consolesPanel = HybrisConsolesPanel(project)

    /**
     * Creates the tool window content
     * @param toolWindow
     */
    fun createToolWindowContent(toolWindow: ToolWindow): Content {
        val content = toolWindow.contentManager.factory.createContent(consolesPanel.component, "Consoles", true)
        content.icon = AllIcons.Debugger.Console
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
        return content
    }

    override fun dispose() {
    }
}
