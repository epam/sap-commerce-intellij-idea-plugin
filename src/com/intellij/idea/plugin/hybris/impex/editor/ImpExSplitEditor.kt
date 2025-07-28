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

package com.intellij.idea.plugin.hybris.impex.editor

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.psi.ImpexMacroDeclaration
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.execution.impex.ImpExExecutionContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
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
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.ui.OnePixelSplitter
import com.intellij.util.asSafely
import kotlinx.coroutines.*
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import java.io.Serial
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun AnActionEvent.impexSplitEditor() = this.getData(PlatformDataKeys.FILE_EDITOR)
    ?.asSafely<ImpExSplitEditor>()

fun AnActionEvent.impexExecutionContextSettings(fallback: () -> ImpExExecutionContext.Settings) = this.getData(CommonDataKeys.EDITOR)
    ?.getUserData(HybrisConstants.KEY_IMPEX_EXECUTION_SETTINGS)
    ?: fallback()

class ImpExSplitEditor(internal val textEditor: TextEditor, private val project: Project) : UserDataHolderBase(), FileEditor, TextEditor {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770395176190649196L

        private val KEY_PARAMETERS = Key.create<Map<SmartPsiElementPointer<ImpexMacroDeclaration>, ImpExVirtualParameter>>("impex.parameters.key")
        private val KEY_IN_EDITOR_RESULTS = Key.create<Boolean>("impex.in_editor_results.key")
    }

    var inEditorParameters: Boolean
        get() = inEditorParametersView != null
        set(state) {
            if (state) {
                ImpExInEditorParametersView.getInstance(project).renderParameters(this)
            } else {
                virtualParametersDisposable?.apply { Disposer.dispose(this) }
                virtualParametersDisposable = null
                inEditorParametersView = null
            }

            component.requestFocus()
            horizontalSplitter.firstComponent.requestFocus()

            reparseTextEditor()
        }

    var virtualParameters: Map<SmartPsiElementPointer<ImpexMacroDeclaration>, ImpExVirtualParameter>?
        get() = getUserData(KEY_PARAMETERS)
        set(value) = putUserData(KEY_PARAMETERS, value)

    val virtualText: String
        get() = virtualParameters
            ?.let { getParametrizedText(it) }
            ?: getText()

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

    internal var inEditorParametersView: JComponent?
        get() = horizontalSplitter.secondComponent
        set(view) {
            horizontalSplitter.secondComponent = view
        }

    internal var virtualParametersDisposable: Disposable? = null

    private var renderParametersJob: Job? = null
    private var reparseTextEditorJob: Job? = null

    private val horizontalSplitter = OnePixelSplitter(false).apply {
        isShowDividerControls = true
        splitterProportionKey = "$javaClass.horizontalSplitter"
        setHonorComponentsMinimumSize(true)

        firstComponent = textEditor.component
    }

    private val verticalSplitter = OnePixelSplitter(true).apply {
        isShowDividerControls = true
        splitterProportionKey = "$javaClass.verticalSplitter"
        setHonorComponentsMinimumSize(true)

        firstComponent = horizontalSplitter
    }

    private val rootPanel = JPanel(BorderLayout()).apply {
        add(verticalSplitter, BorderLayout.CENTER)
    }

    fun virtualParameter(element: ImpexMacroDeclaration): ImpExVirtualParameter? = virtualParameters
        ?.takeIf { inEditorParameters }
        ?.filter { (key, _) ->
            key.element?.isEquivalentTo(element) ?: false
        }
        ?.map { (_, value) -> value }
        ?.firstOrNull()

    fun resetVirtualParameter(pointer: SmartPsiElementPointer<ImpexMacroDeclaration>) {
        virtualParameters ?: return

        val newVirtualParameters = HashMap(virtualParameters)
        newVirtualParameters.remove(pointer)

        virtualParameters = newVirtualParameters

        ImpExInEditorParametersView.getInstance(project).renderParameters(this)
    }

    fun renderExecutionResult(result: DefaultExecutionResult) = ImpExInEditorResultsView.getInstance(project).resultView(this, result) { coroutineScope, view ->
        coroutineScope.launch {
            edtWriteAction {
                inEditorResultsView = view
            }
        }
    }

    fun showLoader(context: ImpExExecutionContext) {
        inEditorResultsView = ImpExInEditorResultsView.getInstance(project).executingView(context.executionTitle)
    }

    fun refreshParameters(delayMs: Duration = 500.milliseconds) {
        renderParametersJob?.cancel()
        renderParametersJob = CoroutineScope(Dispatchers.Default).launch {
            delay(delayMs)

            if (project.isDisposed || !inEditorParameters) return@launch

            ImpExInEditorParametersView.getInstance(project).renderParameters(this@ImpExSplitEditor)
        }
    }

    /**
     * Reparse PsiFile in the related TextEditor to retrigger inline hints computation
     */
    internal fun reparseTextEditor(delayMs: Duration = 1000.milliseconds) {
        reparseTextEditorJob?.cancel()
        reparseTextEditorJob = CoroutineScope(Dispatchers.Default).launch {
            delay(delayMs)

            if (project.isDisposed) return@launch

            edtWriteAction {
                PsiDocumentManager.getInstance(project).reparseFiles(listOf(file), false)
            }
        }
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
    override fun getName() = "ImpEx Split Editor"
    override fun setState(state: FileEditorState) = textEditor.setState(state)
    override fun isModified() = textEditor.isModified
    override fun isValid() = textEditor.isValid && component.isValid
    override fun dispose() = Disposer.dispose(textEditor)
    override fun getEditor() = textEditor.editor
    override fun canNavigateTo(navigatable: Navigatable) = textEditor.canNavigateTo(navigatable)
    override fun navigateTo(navigatable: Navigatable) = textEditor.navigateTo(navigatable)
    override fun getFile(): VirtualFile? = editor.virtualFile

    private fun getText(): String = editor.selectionModel.selectedText
        .takeIf { selectedText -> selectedText != null && selectedText.trim { it <= ' ' }.isNotEmpty() }
        ?: editor.document.text

    private fun getParametrizedText(virtualParameters: Map<SmartPsiElementPointer<ImpexMacroDeclaration>, ImpExVirtualParameter>): String {
        var text = editor.document.text
        virtualParameters
            .toSortedMap(compareByDescending { it.element?.textRange?.startOffset ?: 0 })
            .forEach { (pointer, virtualParameter) ->
                val element = pointer.element
                if (element != null) {
                    val textRange = element.textRange
                    text = text.replaceRange(textRange.startOffset, textRange.endOffset, virtualParameter.finalText)
                }
            }
        return text;
    }
}
