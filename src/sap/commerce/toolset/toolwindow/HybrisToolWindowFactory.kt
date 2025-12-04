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
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.isHybrisProject
import sap.commerce.toolset.ui.toolwindow.CxToolWindowActivationAware
import sap.commerce.toolset.ui.toolwindow.ToolWindowContentProvider

class HybrisToolWindowFactory(private val coroutineScope: CoroutineScope) : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project, toolWindow: ToolWindow
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            edtWriteAction {
                ToolWindowContentProvider.EP.extensionList
                    .sortedBy { it.order }
                    .map { it.create(project, toolWindow) }
                    .forEach { toolWindow.contentManager.addContent(it) }

                toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
                    override fun selectionChanged(event: ContentManagerEvent) {
                        event.content.component
                            .asSafely<CxToolWindowActivationAware>()
                            ?.onActivated()

                        toolWindow.contentManager.contents
                            .filter { it.displayName != event.content.displayName }
                            .map { it.component }
                            .filterIsInstance<CxToolWindowActivationAware>()
                            .forEach { it.onDeactivated() }
                    }
                })
            }
        }
    }

    override suspend fun isApplicableAsync(project: Project) = project.isHybrisProject
    override fun shouldBeAvailable(project: Project) = project.isHybrisProject

}
