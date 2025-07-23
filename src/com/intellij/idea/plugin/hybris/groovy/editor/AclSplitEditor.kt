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

package com.intellij.idea.plugin.hybris.groovy.editor

import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.getOrCreateUserData
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.ui.OnePixelSplitter
import com.intellij.util.asSafely
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import java.io.Serial
import javax.swing.JComponent
import javax.swing.JPanel

fun AnActionEvent.groovySplitEditor() = this.getData(PlatformDataKeys.FILE_EDITOR)
    ?.asSafely<GroovySplitEditor>()

class GroovySplitEditor(internal val textEditor: TextEditor, private val project: Project) : UserDataHolderBase(), FileEditor, TextEditor {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770445176190649196L

        private val KEY_IN_EDITOR_RESULTS = Key.create<Boolean>("groovy.in_editor_results.key")
    }

    var inEditorResults: Boolean
        get() = getOrCreateUserData(KEY_IN_EDITOR_RESULTS) { true }
        set(state) {
            putUserData(KEY_IN_EDITOR_RESULTS, state)
            verticalSplitter.secondComponent?.isVisible = state
        }

    private var inEditorResultsView: JComponent?
        get() = verticalSplitter.secondComponent
        set(view) {
            verticalSplitter.secondComponent = view
        }

    private val verticalSplitter = OnePixelSplitter(true).apply {
        isShowDividerControls = true
        splitterProportionKey = "$javaClass.verticalSplitter"
        setHonorComponentsMinimumSize(true)

        firstComponent = textEditor.component
    }

    private val rootPanel = JPanel(BorderLayout()).apply {
        add(verticalSplitter, BorderLayout.CENTER)
    }

    fun renderExecutionResult(result: DefaultExecutionResult) = GroovyInEditorResultsView.getInstance(project).resultView(this, result) { coroutineScope, view ->
        coroutineScope.launch {
            edtWriteAction {
                inEditorResultsView = view
            }
        }
    }

    fun showLoader() {
        if (inEditorResultsView == null) return

        inEditorResultsView = GroovyInEditorResultsView.getInstance(project)
            .executingView()
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.addPropertyChangeListener(listener)
        component.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.removePropertyChangeListener(listener)
        component.removePropertyChangeListener(listener)
    }

    override fun getPreferredFocusedComponent(): JComponent? = verticalSplitter.firstComponent

    override fun getComponent() = rootPanel
    override fun getName() = "Groovy Split Editor"
    override fun setState(state: FileEditorState) = textEditor.setState(state)
    override fun isModified() = textEditor.isModified
    override fun isValid() = textEditor.isValid && component.isValid
    override fun dispose() = Disposer.dispose(textEditor)
    override fun getEditor() = textEditor.editor
    override fun canNavigateTo(navigatable: Navigatable) = textEditor.canNavigateTo(navigatable)
    override fun navigateTo(navigatable: Navigatable) = textEditor.navigateTo(navigatable)
    override fun getFile(): VirtualFile? = editor.virtualFile

}
