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

package com.intellij.idea.plugin.hybris.acl.file.actions

import com.intellij.idea.plugin.hybris.acl.AclLanguage
import com.intellij.idea.plugin.hybris.acl.editor.AclSplitEditor
import com.intellij.idea.plugin.hybris.acl.editor.aclSplitEditor
import com.intellij.idea.plugin.hybris.actions.ExecuteStatementAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ExecutionMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExDialect
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionContext
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch

class AclValidateAction : ExecuteStatementAction<HybrisImpexConsole, AclSplitEditor>(
    AclLanguage,
    HybrisImpexConsole::class,
    "Validate Access Control Lists",
    "Validate Access Control Lists (user rights) via remote SAP Commerce instance",
    HybrisIcons.Acl.Actions.VALIDATE
) {

    override fun fileEditor(e: AnActionEvent): AclSplitEditor? = e.aclSplitEditor()

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = fileEditor(e)
        val context = ImpExExecutionContext(
            content = content,
            executionMode = ExecutionMode.VALIDATE,
            dialect = ImpExDialect.ACL
        )

        if (fileEditor?.inEditorResults ?: false) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader(context)

            ImpExExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                fileEditor.renderExecutionResult(result)
                fileEditor.putUserData(KEY_QUERY_EXECUTING, false)

                coroutineScope.launch {
                    readAction { this@AclValidateAction.update(e) }
                }
            }
        } else {
            val console = openConsole(project, content) ?: return

            ImpExExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                console.print(result)
            }
        }
    }
}