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
import com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler.ConsoleExecutionService
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.*
import com.intellij.idea.plugin.hybris.tools.remote.http.GroovyExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.http.PolyglotQueryExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.http.SolrExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.http.flexibleSearch.FlexibleSearchExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.ImpExExecutionContext
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
        val project = e.project ?: return
        val consoleService = project.service<HybrisConsoleService>()
        val console = consoleService.getActiveConsole() ?: return

        when (console) {
            is HybrisGroovyConsole -> console.execute(
                GroovyExecutionContext(console.content)
            )

            is HybrisImpexConsole -> console.execute(
                ImpExExecutionContext(console.content)
            )

            is HybrisPolyglotQueryConsole -> console.execute(
                PolyglotQueryExecutionContext(console.content)
            )

            is HybrisFlexibleSearchConsole -> console.execute(
                FlexibleSearchExecutionContext(console.content)
            )

            is HybrisSolrSearchConsole -> console.execute(
                SolrExecutionContext(console.content)
            )

            else -> throw NotImplementedError("This action cannot be used with the ${console::class.qualifiedName}")
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val consoleService = HybrisConsoleService.getInstance(project)
        val editor = consoleService.getActiveConsole()?.consoleEditor ?: return
        val lookup = LookupManager.getActiveLookup(editor)

        e.presentation.isEnabled = !project.service<ConsoleExecutionService>().isProcessRunning && (lookup == null || !lookup.isCompletion)
        e.presentation.disabledIcon = AnimatedIcon.Default.INSTANCE
    }
}
