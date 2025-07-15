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

package com.intellij.idea.plugin.hybris.tools.remote.console.actions

import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler.ConsoleExecuteActionHandler
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.ui.AnimatedIcon

class ConsoleExecuteStatementAction : AnAction(
    "Execute Current Statement",
    "",
    HybrisIcons.Console.Actions.EXECUTE
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val remoteExecutionContext = e.getData(HybrisConstants.DATA_KEY_REPLICA_CONTEXT)
        val project = e.project ?: return

//        project.service<HybrisConsoleService>()
//            .execute(e)

        e.project
            ?.service<ConsoleExecuteActionHandler>()
            ?.runExecuteAction(remoteExecutionContext)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val consoleService = HybrisConsoleService.getInstance(project)
        val activeConsole = consoleService.getActiveConsole() ?: return
        val editor = activeConsole.consoleEditor
        val lookup = LookupManager.getActiveLookup(editor)

        e.presentation.isEnabled = !consoleService.isProcessRunning && (lookup == null || !lookup.isCompletion)
        e.presentation.disabledIcon = AnimatedIcon.Default.INSTANCE
    }
}
