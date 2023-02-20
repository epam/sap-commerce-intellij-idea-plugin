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
package com.intellij.idea.plugin.hybris.toolwindow

import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesToolWindow
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.BSToolWindow
import com.intellij.idea.plugin.hybris.toolwindow.system.type.TSToolWindow
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class HybrisToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project, toolWindow: ToolWindow
    ) {
        arrayOf(
            TSToolWindow.getInstance(project).createToolWindowContent(toolWindow),
            BSToolWindow.getInstance(project).createToolWindowContent(toolWindow),
            HybrisConsolesToolWindow.getInstance(project).createToolWindowContent(toolWindow),
        ).forEach { toolWindow.contentManager.addContent(it) }
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    }

    companion object {
        const val ID = "SAP Commerce"
    }
}