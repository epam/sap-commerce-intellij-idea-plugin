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
package com.intellij.idea.plugin.hybris.tools.remote.console

import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisConsolesView
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionContext
import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowFactory
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.asSafely
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
                .findContent(HybrisToolWindowFactory.CONSOLES_ID)
                ?.let { contentManager.setSelectedContent(it) }
        }

    private fun findConsolesView() = hybrisToolWindow()
        ?.contentManager
        ?.findContent(HybrisToolWindowFactory.CONSOLES_ID)
        ?.component
        ?.asSafely<HybrisConsolesView>()

    private fun hybrisToolWindow() = ToolWindowManager.getInstance(project).getToolWindow(HybrisToolWindowFactory.ID)

    companion object {
        fun getInstance(project: Project): HybrisConsoleService = project.getService(HybrisConsoleService::class.java)
    }
}
