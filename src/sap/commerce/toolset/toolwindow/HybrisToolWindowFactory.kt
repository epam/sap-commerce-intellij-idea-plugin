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
package sap.commerce.toolset.toolwindow

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.ui.BSToolWindow
import sap.commerce.toolset.ccv2.toolwindow.CCv2View
import sap.commerce.toolset.console.toolWindow.HybrisConsolesToolWindow
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.logging.ui.LoggersView
import sap.commerce.toolset.typeSystem.ui.TSView
import sap.commerce.toolset.ui.toolwindow.ContentActivationAware

class HybrisToolWindowFactory(private val coroutineScope: CoroutineScope) : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project, toolWindow: ToolWindow
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            edtWriteAction {
                arrayOf(
                    createTSContent(toolWindow, TSView(project)),
                    createBSContent(toolWindow, BSToolWindow(project)),
                    createConsolesContent(toolWindow, project, HybrisConsolesToolWindow.getInstance(project)),
                    createCCv2CLIContent(toolWindow, project, CCv2View(project)),
                    createLoggersContent(toolWindow, LoggersView(project, coroutineScope))
                ).forEach { toolWindow.contentManager.addContent(it) }

                toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
                    override fun selectionChanged(event: ContentManagerEvent) {
                        event.content.component
                            .asSafely<ContentActivationAware>()
                            ?.onActivated()
                        toolWindow.contentManager.contents
                            .filter { it.displayName != event.content.displayName }
                            .forEach {
                                it.component
                                    .asSafely<ContentActivationAware>()
                                    ?.onDeactivated()
                            }
                    }
                })
            }
        }
    }

    override suspend fun isApplicableAsync(project: Project) = project.isHybrisProject
    override fun shouldBeAvailable(project: Project) = project.isHybrisProject

    private fun createTSContent(toolWindow: ToolWindow, panel: TSView) = with(toolWindow.contentManager.factory.createContent(panel, TSView.ID, true)) {
        Disposer.register(toolWindow.disposable, panel)

        isCloseable = false
        icon = HybrisIcons.TypeSystem.FILE
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)

        this
    }

    private fun createBSContent(toolWindow: ToolWindow, panel: BSToolWindow) = with(toolWindow.contentManager.factory.createContent(panel, BSToolWindow.ID, true)) {
        Disposer.register(toolWindow.disposable, panel)

        isCloseable = false
        icon = HybrisIcons.BeanSystem.FILE
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
        this
    }

    private fun createConsolesContent(toolWindow: ToolWindow, project: Project, panel: HybrisConsolesToolWindow) =
        with(toolWindow.contentManager.factory.createContent(panel, HybrisConsolesToolWindow.ID, true)) {
            Disposer.register(LineStatusTrackerManager.getInstanceImpl(project), toolWindow.disposable)
            Disposer.register(toolWindow.disposable, panel)

            isCloseable = false
            icon = HybrisIcons.Console.DESCRIPTOR
            putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
            this
        }

    private fun createCCv2CLIContent(toolWindow: ToolWindow, project: Project, panel: CCv2View) =
        with(toolWindow.contentManager.factory.createContent(panel, CCv2View.TAB_NAME, true)) {
            Disposer.register(LineStatusTrackerManager.getInstanceImpl(project), toolWindow.disposable)
            Disposer.register(toolWindow.disposable, panel)

            isCloseable = false
            icon = HybrisIcons.CCv2.DESCRIPTOR
            putUserData(ToolWindow.SHOW_CONTENT_ICON, true)

            this
        }

    private fun createLoggersContent(toolWindow: ToolWindow, panel: LoggersView) = with(toolWindow.contentManager.factory.createContent(panel, LoggersView.ID, true)) {
        Disposer.register(toolWindow.disposable, panel)

        isCloseable = false
        icon = HybrisIcons.Log.LOG
        putUserData(ToolWindow.SHOW_CONTENT_ICON, true)

        this
    }
}