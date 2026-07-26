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

package sap.commerce.toolset.flexibleSearch.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.util.asSafely
import kotlinx.coroutines.*
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.flexibleSearch.exec.flexibleSearchExecContextSettings
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.typeSystem.meta.event.TSMetaModelChangeListener
import sap.commerce.toolset.ui.editor.SplitEditorBase
import java.io.Serial
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun AnActionEvent.flexibleSearchSplitEditor() = this.getData(PlatformDataKeys.FILE_EDITOR)
    ?.asSafely<FlexibleSearchSplitEditorEx>()

fun AnActionEvent.flexibleSearchExecutionContextSettings(fallback: () -> FlexibleSearchExecContext.Settings) = this.getData(CommonDataKeys.VIRTUAL_FILE)
    ?.flexibleSearchExecContextSettings(fallback)
    ?: fallback()

class FlexibleSearchSplitEditorBase(textEditor: TextEditor, project: Project) : SplitEditorBase(textEditor, project), FlexibleSearchSplitEditorEx {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770395176190649196L
        private val KEY_PARAMETERS = Key.create<Map<String, FlexibleSearchVirtualParameter>>("flexibleSearch.parameters.key")
        private val KEY_LAST_EXEC_RESULT = Key.create<FlexibleSearchExecResult>("flexibleSearch.last_exec_result.key")
    }

    override var virtualParameters: Map<String, FlexibleSearchVirtualParameter>?
        get() = getUserData(KEY_PARAMETERS)
        set(value) = putUserData(KEY_PARAMETERS, value)

    override val virtualText: String
        get() = virtualParameters
            ?.values
            ?.sortedByDescending { it.name.length }
            ?.let { parameters ->
                var updatedContent = getText()
                parameters.forEach {
                    updatedContent = updatedContent.replace("?${it.name}", it.sqlValue)
                }
                return@let updatedContent
            }
            ?: getText()

    override var lastExecResult: FlexibleSearchExecResult?
        get() = getUserData(KEY_LAST_EXEC_RESULT)
        set(value) = putUserData(KEY_LAST_EXEC_RESULT, value)

    override var csvResultsDisposable: Disposable? = null

    private var renderParametersJob: Job? = null

    override fun renderInEditorParameters() {
        FlexibleSearchInEditorParametersView.getInstance(project).renderParameters(this)
    }

    override fun renderExecutionResult(result: FlexibleSearchExecResult) {
        lastExecResult = result
        FlexibleSearchInEditorResultsView.getInstance(project).resultView(this, result) { coroutineScope, view ->
            coroutineScope.launch {
                edtWriteAction {
                    inEditorResultsView = view
                }
            }
        }
    }

    override fun clearExecutionResult() {
        lastExecResult = null
        inEditorResultsView = null
    }

    override fun showLoader(context: FlexibleSearchExecContext) {
        inEditorResultsView = FlexibleSearchInEditorResultsView.getInstance(project).executingView(context.executionTitle)
    }

    override fun refreshParameters(delayMs: Duration) {
        renderParametersJob?.cancel()
        renderParametersJob = CoroutineScope(Dispatchers.Default).launch {
            delay(delayMs)

            if (project.isDisposed || !inEditorParameters) return@launch

            FlexibleSearchInEditorParametersView.getInstance(project).renderParameters(this@FlexibleSearchSplitEditorBase)
        }
    }

    override fun getName() = "FlexibleSearch Split Editor"

    init {
        verticalSplitter.firstComponent = horizontalSplitter

        with(project.messageBus.connect(this)) {
            subscribe(TSMetaModelChangeListener.TOPIC, object : TSMetaModelChangeListener {
                override fun onChanged(globalMetaModel: TSGlobalMetaModel) {
                    refreshParameters()
                    reparseTextEditor()
                }
            })
        }
    }
}
