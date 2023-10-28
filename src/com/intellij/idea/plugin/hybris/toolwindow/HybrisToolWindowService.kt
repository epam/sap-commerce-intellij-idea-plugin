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

package com.intellij.idea.plugin.hybris.toolwindow

import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

@Service(Service.Level.PROJECT)
class HybrisToolWindowService(val project: Project) {

    fun activateToolWindow() {
        hybrisToolWindow()
            ?.let {
                ApplicationManager.getApplication().invokeLater {
                    it.isAvailable = true
                    it.activate(null, true)
                }
            }
    }

    fun activateToolWindowTab(id: String) {
        hybrisToolWindow()
            ?.let {
                val contentManager = it.contentManager
                contentManager.findContent(id)
                    ?.let { tab ->
                        contentManager.setSelectedContent(tab);
                    }
            }
    }

    fun finConsolesView() = hybrisToolWindow()
        ?.contentManager
        ?.findContent(HybrisToolWindowFactory.CONSOLES_ID)
        ?.component
        ?.let { it as? HybrisConsolesView }

    private fun hybrisToolWindow() = ToolWindowManager.getInstance(project).getToolWindow(HybrisToolWindowFactory.ID)

    companion object {
        fun getInstance(project: Project): HybrisToolWindowService = project.getService(HybrisToolWindowService::class.java)
    }
}