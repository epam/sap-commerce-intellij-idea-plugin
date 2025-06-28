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
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class HybrisConsoleService(private val project: Project) {

    fun findConsole(consoleTitle: String) = HybrisToolWindowService.getInstance(project).findConsolesView()
        ?.findConsole(consoleTitle)

    fun setActiveConsole(console: HybrisConsole) {
        HybrisToolWindowService.getInstance(project).findConsolesView()
            ?.setActiveConsole(console)
    }

    fun getActiveConsole() = HybrisToolWindowService.getInstance(project).findConsolesView()
        ?.getActiveConsole()

    fun validateImpex() {
        HybrisToolWindowService.getInstance(project).findConsolesView()
            ?.validateImpex()
    }

    fun executeStatement() {
        HybrisToolWindowService.getInstance(project).findConsolesView()
            ?.execute()
    }

    companion object {
        fun getInstance(project: Project): HybrisConsoleService = project.getService(HybrisConsoleService::class.java)
    }
}
