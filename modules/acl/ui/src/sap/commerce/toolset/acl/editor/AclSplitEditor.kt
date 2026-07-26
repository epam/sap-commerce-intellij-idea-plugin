/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.acl.editor

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import kotlinx.coroutines.launch
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.impex.exec.context.ImpExExecContext
import sap.commerce.toolset.ui.editor.SplitEditorBase
import java.io.Serial

fun AnActionEvent.aclSplitEditor() = this.getData(PlatformDataKeys.FILE_EDITOR)
    ?.asSafely<AclSplitEditorEx>()

class AclSplitEditorBase(textEditor: TextEditor, project: Project) : SplitEditorBase(textEditor, project), AclSplitEditorEx {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770445176190649196L
    }

    override val inEditorResultsTitle = "ACL Execution Results"

    override fun showLoader(context: ImpExExecContext) {
        inEditorResultsView = AclInEditorResultsView.getInstance(project).executingView(context.executionTitle)
    }

    override fun renderExecutionResult(result: DefaultExecResult) = AclInEditorResultsView.getInstance(project).resultView(this, result) { coroutineScope, view ->
        coroutineScope.launch {
            edtWriteAction {
                inEditorResultsView = view
            }
        }
    }

    override fun getName() = "Acl Split Editor"
}
