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

import com.intellij.idea.plugin.hybris.toolwindow.HybrisToolWindowService
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import kotlin.reflect.KClass

@Service(Service.Level.PROJECT)
class HybrisConsoleService(private val project: Project) {

    fun <C : HybrisConsole<*>> findConsole(consoleClass: KClass<C>): C? = HybrisToolWindowService.getInstance(project).findConsolesView()
        ?.findConsole(consoleClass)

    fun <C : HybrisConsole<*>> findConsole(toolWindow: ToolWindow, consoleClass: KClass<C>): C? = HybrisToolWindowService.getInstance(project).findConsolesView(toolWindow)
        ?.findConsole(consoleClass)

    fun setActiveConsole(console: HybrisConsole<*>) {
        HybrisToolWindowService.getInstance(project).findConsolesView()
            ?.setActiveConsole(console)
    }

    fun getActiveConsole() = HybrisToolWindowService.getInstance(project).findConsolesView()
        ?.getActiveConsole()

    fun executeStatement(e: AnActionEvent) {
        HybrisToolWindowService.getInstance(project).findConsolesView()
            ?.execute(e)
    }

    companion object {
        fun getInstance(project: Project): HybrisConsoleService = project.getService(HybrisConsoleService::class.java)
    }
}
