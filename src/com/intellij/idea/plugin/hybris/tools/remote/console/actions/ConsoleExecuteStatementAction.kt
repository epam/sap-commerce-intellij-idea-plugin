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
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.*
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.monitor.ImpExMonitorExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.solr.SolrExecutionClient
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ConsoleExecuteStatementAction : AnAction(
    "Execute Current Statement",
    "",
    HybrisIcons.Console.Actions.EXECUTE
) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val console = e.project
            ?.service<HybrisConsoleService>()
            ?.getActiveConsole()
            ?: return

        when (console) {
            is HybrisGroovyConsole -> project.service<GroovyExecutionClient>().execute(
                context = console.context,
                beforeCallback = { coroutineScope -> console.beforeExecution() },
                resultCallback = { coroutineScope, result -> console.print(result) }
            )

            is HybrisImpexConsole -> project.service<ImpExExecutionClient>().execute(
                context = console.context,
                beforeCallback = { coroutineScope -> console.beforeExecution() },
                resultCallback = { coroutineScope, result -> console.print(result) }
            )

            is HybrisPolyglotQueryConsole -> project.service<FlexibleSearchExecutionClient>().execute(
                context = console.context,
                beforeCallback = { coroutineScope -> console.beforeExecution() },
                resultCallback = { coroutineScope, result -> console.print(result) }
            )

            is HybrisFlexibleSearchConsole -> project.service<FlexibleSearchExecutionClient>().execute(
                context = console.context,
                beforeCallback = { coroutineScope -> console.beforeExecution() },
                resultCallback = { coroutineScope, result -> console.print(result) }
            )

            is HybrisSolrSearchConsole -> project.service<SolrExecutionClient>().execute(
                context = console.context,
                beforeCallback = { coroutineScope -> console.beforeExecution() },
                resultCallback = { coroutineScope, result -> console.print(result) }
            )

            is HybrisImpexMonitorConsole -> project.service<ImpExMonitorExecutionClient>().execute(
                context = console.context,
                beforeCallback = { coroutineScope -> console.beforeExecution() },
                resultCallback = { coroutineScope, result -> console.print(result) }
            )

            is HybrisSQLConsole -> project.service<FlexibleSearchExecutionClient>().execute(
                context = console.context,
                beforeCallback = { coroutineScope -> console.beforeExecution() },
                resultCallback = { coroutineScope, result -> console.print(result) }
            )

            else -> throw NotImplementedError("This action cannot be used with the ${console::class.qualifiedName}")
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val consoleService = HybrisConsoleService.getInstance(project)
        val console = consoleService.getActiveConsole() ?: return
        val editor = console.consoleEditor
        val lookup = LookupManager.getActiveLookup(editor)

        e.presentation.isEnabled = console.canExecute() && (lookup == null || !lookup.isCompletion)
        e.presentation.disabledIcon = console.disabledIcon()
    }
}
