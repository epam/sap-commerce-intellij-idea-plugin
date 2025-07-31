/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesView
import com.intellij.idea.plugin.hybris.toolwindow.ccv2.CCv2View
import com.intellij.idea.plugin.hybris.toolwindow.loggers.LoggersView
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.view.BSView
import com.intellij.idea.plugin.hybris.toolwindow.system.type.view.TSView
import com.intellij.idea.plugin.hybris.util.isHybrisProject
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
            createConsolesContent(toolWindow, project, HybrisConsolesView.getInstance(project)),
            createCCv2CLIContent(toolWindow, project, CCv2View(project)),
            createLoggersContent(toolWindow, project, LoggersView(project))
        ).forEach { toolWindow.contentManager.addContent(it) }
    }

    override suspend fun isApplicableAsync(project: Project) = project.isHybrisProject
    override fun shouldBeAvailable(project: Project) = project.isHybrisProject

    private fun createTSContent(toolWindow: ToolWindow, panel: TSView) = with(toolWindow.contentManager.factory.createContent(panel, TS_ID, true)) {
        Disposer.register(toolWindow.disposable, panel)

        isCloseable = false
        icon = HybrisIcons.TypeSystem.FILE
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)

        this
    }

    private fun createBSContent(toolWindow: ToolWindow, panel: BSView) = with(toolWindow.contentManager.factory.createContent(panel, BS_ID, true)) {
        Disposer.register(toolWindow.disposable, panel)

        isCloseable = false
        icon = HybrisIcons.BeanSystem.FILE
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
        this
    }

    private fun createConsolesContent(toolWindow: ToolWindow, project: Project, panel: HybrisConsolesView) =
        with(toolWindow.contentManager.factory.createContent(panel, CONSOLES_ID, true)) {
            Disposer.register(LineStatusTrackerManager.getInstanceImpl(project), toolWindow.disposable)
            Disposer.register(toolWindow.disposable, panel)

            isCloseable = false
            icon = HybrisIcons.Console.DESCRIPTOR
            putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
            this
        }

    private fun createCCv2CLIContent(toolWindow: ToolWindow, project: Project, panel: CCv2View) = with(toolWindow.contentManager.factory.createContent(panel, CCV2, true)) {
        Disposer.register(LineStatusTrackerManager.getInstanceImpl(project), toolWindow.disposable)
        Disposer.register(toolWindow.disposable, panel)

        isCloseable = false
        icon = HybrisIcons.CCv2.DESCRIPTOR
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)

        this
    }

    private fun createLoggersContent(toolWindow: ToolWindow, project: Project, panel: LoggersView) = with(toolWindow.contentManager.factory.createContent(panel, LOGGERS, true)) {
        Disposer.register(toolWindow.disposable, panel)
        isCloseable = false
        icon = HybrisIcons.Log.Level.ALL
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)

        this
    }

    companion object {
        const val ID = "SAP CX"
        const val CONSOLES_ID = "Consoles"
        const val TS_ID = "Type System"
        const val BS_ID = "Bean System"
        const val CCV2 = "CCv2"
        const val LOGGERS = "Loggers"
    }
}