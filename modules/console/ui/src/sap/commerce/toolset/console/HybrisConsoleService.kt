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
import sap.commerce.toolset.console.ui.HybrisConsolesView
import sap.commerce.toolset.exec.context.ExecutionContext
import kotlin.reflect.KClass

@Service(Service.Level.PROJECT)
class HybrisConsoleService(private val project: Project) {

    fun <C : HybrisConsole<out ExecutionContext>> openConsole(consoleClass: KClass<C>): C? {
        val view = findConsolesView() ?: return null
        val console = view.findConsole(consoleClass) ?: return null
        activateToolWindow()
        activateToolWindowTab()

        view.activeConsole = console

        return console
    }

    fun getActiveConsole() = findConsolesView()
        ?.activeConsole

    fun activateToolWindow() = hybrisToolWindow()
        ?.let {
            invokeLater {
                it.isAvailable = true
                it.activate(null, true)
            }
        }

    private fun activateToolWindowTab() = hybrisToolWindow()
        ?.contentManager
        ?.let { contentManager ->
            contentManager
                .findContent(HybrisConsolesView.Companion.ID)
                ?.let { contentManager.setSelectedContent(it) }
        }

    private fun findConsolesView() = hybrisToolWindow()
        ?.contentManager
        ?.findContent(HybrisConsolesView.Companion.ID)
        ?.component
        ?.asSafely<HybrisConsolesView>()

    private fun hybrisToolWindow() = ToolWindowManager.Companion.getInstance(project).getToolWindow(HybrisConstants.TOOLWINDOW_ID)

    companion object {
        fun getInstance(project: Project): HybrisConsoleService = project.service()
    }
}