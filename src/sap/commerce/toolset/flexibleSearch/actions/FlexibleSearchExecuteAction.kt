/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.flexibleSearch.actions

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import sap.commerce.toolset.HybrisI18NBundleUtils.message
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actions.ExecuteStatementAction
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLanguage
import sap.commerce.toolset.flexibleSearch.editor.FlexibleSearchSplitEditor
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchExecutionContextSettings
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchSplitEditor
import sap.commerce.toolset.flexibleSearch.exec.console.FlexibleSearchConsole
import sap.commerce.toolset.flexibleSearch.exec.remote.FlexibleSearchExecutionClient
import sap.commerce.toolset.flexibleSearch.exec.remote.context.FlexibleSearchExecutionContext

class FlexibleSearchExecuteAction : ExecuteStatementAction<FlexibleSearchConsole, FlexibleSearchSplitEditor>(
    FlexibleSearchLanguage,
    FlexibleSearchConsole::class,
    message("hybris.fxs.actions.execute_query"),
    message("hybris.fxs.actions.execute_query.description"),
    HybrisIcons.Console.Actions.EXECUTE
) {

    override fun fileEditor(e: AnActionEvent): FlexibleSearchSplitEditor? = e.flexibleSearchSplitEditor()

    override fun processContent(e: AnActionEvent, content: String, editor: Editor, project: Project): String = fileEditor(e)
        ?.virtualText
        ?: content

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = fileEditor(e) ?: return
        val settings = e.flexibleSearchExecutionContextSettings { FlexibleSearchExecutionContext.defaultSettings(project) }
        val context = FlexibleSearchExecutionContext(
            content = content,
            settings = settings
        )

        if (fileEditor.inEditorResults) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader(context)

            FlexibleSearchExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                fileEditor.renderExecutionResult(result)
                fileEditor.putUserData(KEY_QUERY_EXECUTING, false)

                coroutineScope.launch {
                    readAction { ActivityTracker.getInstance().inc() }
                }
            }
        } else {
            val console = openConsole(project, content) ?: return

            FlexibleSearchExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                console.print(result)
            }
        }
    }
}
