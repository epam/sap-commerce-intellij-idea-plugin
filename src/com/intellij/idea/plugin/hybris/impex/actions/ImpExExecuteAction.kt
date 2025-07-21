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
package com.intellij.idea.plugin.hybris.impex.actions

import com.intellij.idea.plugin.hybris.actions.ExecuteStatementAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.editor.impexSplitEditor
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionContext
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.ui.AnimatedIcon
import kotlinx.coroutines.launch

class ImpExExecuteAction : ExecuteStatementAction<HybrisImpexConsole>(
    ImpexLanguage,
    HybrisImpexConsole::class,
    message("hybris.impex.actions.execute_query"),
    message("hybris.impex.actions.execute_query.description"),
    HybrisIcons.Console.Actions.EXECUTE
) {
    override fun update(e: AnActionEvent) {
        super.update(e)

        val queryExecuting = e.impexSplitEditor()
            ?.getUserData(KEY_QUERY_EXECUTING)
            ?: false

        e.presentation.isEnabledAndVisible = e.presentation.isEnabledAndVisible
        e.presentation.isEnabled = e.presentation.isEnabledAndVisible && !queryExecuting
        e.presentation.disabledIcon = if (queryExecuting) AnimatedIcon.Default.INSTANCE
        else HybrisIcons.Console.Actions.EXECUTE
    }

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = e.impexSplitEditor()
        val context = ImpExExecutionContext(
            content = content
        )

        if (fileEditor?.inEditorResults ?: false) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader()

            ImpExExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                fileEditor.renderExecutionResult(result)
                fileEditor.putUserData(KEY_QUERY_EXECUTING, false)

                coroutineScope.launch {
                    readAction { this@ImpExExecuteAction.update(e) }
                }
            }
        } else {
            val console = openConsole(project, content) ?: return

            ImpExExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                console.print(result)
            }
        }
    }

    companion object {
        private val KEY_QUERY_EXECUTING = Key.create<Boolean>("impex.query.execution.state")
    }
}
