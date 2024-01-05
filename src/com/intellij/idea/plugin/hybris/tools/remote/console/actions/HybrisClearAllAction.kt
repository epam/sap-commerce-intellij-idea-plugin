/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.execution.ExecutionBundle
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAwareAction

class HybrisClearAllAction : DumbAwareAction(
    ExecutionBundle.message("clear.all.from.console.action.name"),
    "Clear the contents of the console",
    HybrisIcons.ACTION_CLEAR_ALL
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val activeConsole = HybrisConsoleService.getInstance(project).getActiveConsole() ?: return

        var enabled = activeConsole.contentSize > 0
        if (!enabled) {
            enabled = e.getData(LangDataKeys.CONSOLE_VIEW) != null
            val editor = e.getData(CommonDataKeys.EDITOR)
            if (editor != null && editor.document.textLength == 0) {
                enabled = false
            }
        }
        e.presentation.isEnabled = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        HybrisConsoleService.getInstance(project).getActiveConsole()
            ?.clear()
    }
}
