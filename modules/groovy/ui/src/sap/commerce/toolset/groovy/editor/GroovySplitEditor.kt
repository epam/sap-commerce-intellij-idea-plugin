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

package sap.commerce.toolset.groovy.editor

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.util.asSafely
import kotlinx.coroutines.launch
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.groovy.exec.GroovyExecService
import sap.commerce.toolset.groovy.getSpringContextMode
import sap.commerce.toolset.groovy.setSpringContextMode
import sap.commerce.toolset.ui.editor.SplitEditorBase
import java.io.Serial

fun AnActionEvent.groovySplitEditor() = this.getData(PlatformDataKeys.FILE_EDITOR)
    ?.asSafely<GroovySplitEditorEx>()

class GroovySplitEditorBase(textEditor: TextEditor, project: Project) : SplitEditorBase(textEditor, project), GroovySplitEditorEx {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770445176190649196L
    }

    init {
        textEditor.editor.virtualFile
            ?.let {
                it.setSpringContextMode(it.getSpringContextMode(project))
                GroovyExecService.getInstance(project).initSettings(it)
            }
    }

    override fun showLoader(richMessage: String) {
        inEditorResultsView = GroovyInEditorResultsView.getInstance(project).executingView(richMessage)
    }

    override fun renderExecutionResults(results: Collection<DefaultExecResult>) = GroovyInEditorResultsView.getInstance(project).resultView(this, results) { coroutineScope, view ->
        coroutineScope.launch {
            edtWriteAction {
                inEditorResultsView = view
            }
        }
    }

    override fun getName() = "Groovy Split Editor"
}
