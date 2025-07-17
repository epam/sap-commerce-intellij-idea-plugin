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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsoleService
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ExecutionMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionContext
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.ui.AnimatedIcon

class ConsoleImpExValidateAction : AnAction(
    "Validate ImpEx",
    "Validate ImpEx file via remote SAP Commerce instance",
    HybrisIcons.ImpEx.Actions.VALIDATE
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val console = e.project
            ?.service<HybrisConsoleService>()
            ?.getActiveConsole()
            ?: return

        val context = ImpExExecutionContext(
            content = console.content,
            executionMode = ExecutionMode.VALIDATE,
        )

        project.service<ImpExExecutionClient>().execute(
            context = context,
            beforeCallback = { coroutineScope -> console.beforeExecution() },
            resultCallback = { coroutineScope, result -> console.print(result) }
        )
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val activeConsole = HybrisConsoleService.getInstance(project).getActiveConsole()
            ?: return
        val editor = activeConsole.consoleEditor
        val lookup = LookupManager.getActiveLookup(editor)

        e.presentation.isVisible = activeConsole is HybrisImpexConsole
        e.presentation.isEnabled = activeConsole.canExecute() && (lookup == null || !lookup.isCompletion)
        e.presentation.disabledIcon = AnimatedIcon.Default.INSTANCE
    }
}
