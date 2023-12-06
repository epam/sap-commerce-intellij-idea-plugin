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

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSView
import com.intellij.idea.plugin.hybris.toolwindow.system.type.view.TSView
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class HybrisToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project, toolWindow: ToolWindow
    ) {
        arrayOf(
            createTSContent(toolWindow, TSView(project)),
            createBSContent(toolWindow, BSView(project)),
            createConsolesContent(toolWindow, project, HybrisConsolesView(project)),
        ).forEach { toolWindow.contentManager.addContent(it) }
    }

    override suspend fun isApplicableAsync(project: Project) = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
    override fun shouldBeAvailable(project: Project) = HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()

    private fun createTSContent(toolWindow: ToolWindow, panel: TSView) = with(toolWindow.contentManager.factory.createContent(panel, TS_ID, true)) {
        Disposer.register(toolWindow.disposable, panel)

        icon = HybrisIcons.TYPE_SYSTEM
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)

        this
    }

    private fun createBSContent(toolWindow: ToolWindow, panel: BSView) = with(toolWindow.contentManager.factory.createContent(panel, BS_ID, true)) {
        Disposer.register(toolWindow.disposable, panel)

        icon = HybrisIcons.BEAN_FILE
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
        this
    }

    private fun createConsolesContent(toolWindow: ToolWindow, project: Project, panel: HybrisConsolesView) = with(toolWindow.contentManager.factory.createContent(panel, CONSOLES_ID, true)) {
        Disposer.register(LineStatusTrackerManager.getInstanceImpl(project), toolWindow.disposable)
        Disposer.register(toolWindow.disposable, panel)

        icon = AllIcons.Debugger.Console
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
        this
    }

    companion object {
        const val ID = "SAP Commerce"
        const val CONSOLES_ID = "Consoles"
        const val TS_ID = "Type System"
        const val BS_ID = "Bean System"
    }
}