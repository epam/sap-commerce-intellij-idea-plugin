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

package sap.commerce.toolset.impex.editor

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.asSafely
import kotlinx.coroutines.*
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.impex.exec.context.ImpExExecContext
import sap.commerce.toolset.impex.exec.impexExecContextSettings
import sap.commerce.toolset.impex.psi.ImpExMacroDeclaration
import sap.commerce.toolset.ui.editor.SplitEditorBase
import java.io.Serial
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun AnActionEvent.impexSplitEditorEx() = this.getData(PlatformDataKeys.FILE_EDITOR)
    ?.asSafely<ImpExSplitEditorEx>()

fun AnActionEvent.impexExecutionContextSettings(fallback: () -> ImpExExecContext.Settings) = this.getData(CommonDataKeys.VIRTUAL_FILE)
    ?.impexExecContextSettings(fallback)
    ?: fallback()

class ImpExSplitEditorBase(textEditor: TextEditor, project: Project) : SplitEditorBase(textEditor, project), ImpExSplitEditorEx {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770395176190649196L

        private val KEY_PARAMETERS = Key.create<Map<SmartPsiElementPointer<ImpExMacroDeclaration>, ImpExVirtualParameter>>("impex.parameters.key")
    }

    override var virtualParameters: Map<SmartPsiElementPointer<ImpExMacroDeclaration>, ImpExVirtualParameter>?
        get() = getUserData(KEY_PARAMETERS)
        set(value) = putUserData(KEY_PARAMETERS, value)

    override val virtualText: String
        get() = virtualParameters
            ?.let { getParametrizedText(it) }
            ?: getText()

    override fun renderInEditorParameters() {
        ImpExInEditorParametersView.getInstance(project).renderParameters(this)
    }

    override fun virtualParameter(element: ImpExMacroDeclaration): ImpExVirtualParameter? = virtualParameters
        ?.takeIf { inEditorParameters }
        ?.filter { (key, _) ->
            key.element?.isEquivalentTo(element) ?: false
        }
        ?.map { (_, value) -> value }
        ?.firstOrNull()

    override fun resetVirtualParameter(pointer: SmartPsiElementPointer<ImpExMacroDeclaration>) {
        virtualParameters ?: return

        val newVirtualParameters = HashMap(virtualParameters)
        newVirtualParameters.remove(pointer)

        virtualParameters = newVirtualParameters

        ImpExInEditorParametersView.getInstance(project).renderParameters(this)
    }

    override fun renderExecutionResult(result: DefaultExecResult) = ImpExInEditorResultsView.getInstance(project).resultView(this, result) { coroutineScope, view ->
        coroutineScope.launch {
            edtWriteAction {
                inEditorResultsView = view
            }
        }
    }

    override fun showLoader(context: ImpExExecContext) {
        inEditorResultsView = ImpExInEditorResultsView.getInstance(project).executingView(context.executionTitle)
    }

    private var renderParametersJob: Job? = null

    override fun refreshParameters(delayMs: Duration) {
        renderParametersJob?.cancel()
        renderParametersJob = CoroutineScope(Dispatchers.Default).launch {
            delay(delayMs)

            if (project.isDisposed || !inEditorParameters) return@launch

            ImpExInEditorParametersView.getInstance(project).renderParameters(this@ImpExSplitEditorBase)
        }
    }

    override fun getName() = "ImpEx Split Editor"

    init {
        horizontalSplitter.firstComponent = textEditor.component
        verticalSplitter.firstComponent = horizontalSplitter
    }

    private fun getParametrizedText(virtualParameters: Map<SmartPsiElementPointer<ImpExMacroDeclaration>, ImpExVirtualParameter>): String {
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
        return text
    }
}
