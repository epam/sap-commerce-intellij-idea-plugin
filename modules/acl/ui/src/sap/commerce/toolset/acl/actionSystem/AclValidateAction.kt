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

package sap.commerce.toolset.acl.actionSystem

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.acl.AclLanguage
import sap.commerce.toolset.acl.editor.AclSplitEditorEx
import sap.commerce.toolset.acl.editor.aclSplitEditor
import sap.commerce.toolset.hac.actionSystem.ExecuteStatementAction
import sap.commerce.toolset.impex.console.ImpExConsole
import sap.commerce.toolset.impex.exec.ImpExExecClient
import sap.commerce.toolset.impex.exec.context.ImpExExecContext

class AclValidateAction : ExecuteStatementAction<ImpExConsole, AclSplitEditorEx>(
    AclLanguage,
    ImpExConsole::class,
    "Validate Access Control Lists",
    "Validate Access Control Lists (user rights) via remote SAP Commerce instance",
    HybrisIcons.Acl.Actions.VALIDATE
) {

    override fun fileEditor(e: AnActionEvent): AclSplitEditorEx? = e.aclSplitEditor()

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = fileEditor(e) ?: return
        val context = ImpExExecContext(
            content = content,
            executionMode = ImpExExecContext.ExecutionMode.VALIDATE,
            dialect = ImpExExecContext.Dialect.ACL,
            settings = ImpExExecContext.DEFAULT_SETTINGS
        )

        if (fileEditor.inEditorResults) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader(context)

            ImpExExecClient.getInstance(project).execute(context) { coroutineScope, result ->
                fileEditor.renderExecutionResult(result)
                fileEditor.putUserData(KEY_QUERY_EXECUTING, false)

                coroutineScope.launch {
                    readAction { ActivityTracker.getInstance().inc() }
                }
            }
        } else {
            val console = openConsole(project, content) ?: return

            ImpExExecClient.getInstance(project).execute(context) { coroutineScope, result ->
                console.print(result)
            }
        }
    }
}