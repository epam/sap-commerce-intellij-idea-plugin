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

package sap.commerce.toolset.impex.actionSystem

import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnimatedIcon
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.console.HybrisConsoleService
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.impex.console.ImpExConsole
import sap.commerce.toolset.impex.exec.ImpExExecClient
import sap.commerce.toolset.impex.exec.context.ImpExExecContext

class ConsoleImpExValidateAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val console = HybrisConsoleService.getInstance(project).getActiveConsole()
            ?: return
        val connectionSettings = HacExecConnectionService.getInstance(project).activeConnection

        val context = ImpExExecContext(
            content = console.content,
            executionMode = ImpExExecContext.ExecutionMode.VALIDATE,
            settings = ImpExExecContext.defaultSettings(connectionSettings)
        )

        ImpExExecClient.getInstance(project).execute(
            context = context,
            beforeCallback = { _ -> console.beforeExecution() },
            resultCallback = { _, result -> console.print(result) }
        )
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return

        val project = e.project ?: return
        val activeConsole = HybrisConsoleService.getInstance(project).getActiveConsole()
            ?: return
        val editor = activeConsole.consoleEditor
        val lookup = LookupManager.getActiveLookup(editor)

        e.presentation.isVisible = activeConsole is ImpExConsole
        e.presentation.isEnabled = activeConsole.canExecute() && (lookup == null || !lookup.isCompletion)
        e.presentation.disabledIcon = AnimatedIcon.Default.INSTANCE
        e.presentation.text = "Validate ImpEx"
        e.presentation.description = "Validate ImpEx file via remote SAP Commerce instance"
        e.presentation.icon = HybrisIcons.ImpEx.Actions.VALIDATE
    }
}