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

package sap.commerce.toolset.console

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.console.ui.CxConsolesToolWindow
import sap.commerce.toolset.exec.context.ExecContext
import kotlin.reflect.KClass

@Service(Service.Level.PROJECT)
class HybrisConsoleService(private val project: Project) {

    fun <C : HybrisConsole<out ExecContext>> openConsole(consoleClass: KClass<C>, onActivation: (C) -> Unit) {
        activateToolWindow() {
            val view = findConsolesView() ?: return@activateToolWindow
            activateToolWindowTab()
            val console = view.findConsole(consoleClass) ?: return@activateToolWindow

            view.activeConsole = console

            onActivation(console)
        }
    }

    fun getActiveConsole() = findConsolesView()
        ?.activeConsole

    fun activateToolWindow(onActivation: () -> Unit = {}) = hybrisToolWindow()
        ?.let {
            invokeLater {
                it.isAvailable = true
                it.activate(onActivation, true)
            }
        }

    fun openInConsole(consoleClass: KClass<out HybrisConsole<out ExecContext>>, content: String) {
        openConsole(consoleClass) {
            it.clear()
            it.setInputText(content)
        }
    }

    private fun activateToolWindowTab() = hybrisToolWindow()
        ?.contentManager
        ?.let { contentManager ->
            contentManager
                .findContent(CxConsolesToolWindow.ID)
                ?.let { contentManager.setSelectedContent(it) }
        }

    private fun findConsolesView() = hybrisToolWindow()
        ?.contentManager
        ?.findContent(CxConsolesToolWindow.ID)
        ?.component
        ?.asSafely<CxConsolesToolWindow>()

    private fun hybrisToolWindow() = ToolWindowManager.getInstance(project).getToolWindow(HybrisConstants.TOOLWINDOW_ID)

    companion object {
        fun getInstance(project: Project): HybrisConsoleService = project.service()
    }
}