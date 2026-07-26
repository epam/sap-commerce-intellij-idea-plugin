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

package sap.commerce.toolset.polyglotQuery.editor

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.getOrCreateUserData
import com.intellij.util.asSafely
import kotlinx.coroutines.*
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.typeSystem.meta.TSGlobalMetaModel
import sap.commerce.toolset.typeSystem.meta.event.TSMetaModelChangeListener
import sap.commerce.toolset.ui.editor.SplitEditorBase
import java.io.Serial
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun AnActionEvent.polyglotQuerySplitEditor() = this.getData(PlatformDataKeys.FILE_EDITOR)
    ?.asSafely<PolyglotQuerySplitEditorEx>()

class PolyglotQuerySplitEditorBase(textEditor: TextEditor, project: Project) : SplitEditorBase(textEditor, project), PolyglotQuerySplitEditorEx {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3770395176190649196L

        private val KEY_PARAMETERS = Key.create<Map<String, PolyglotQueryVirtualParameter>>("pgq.parameters.key")
        private val KEY_RETRIEVE_ALL_DATA = Key.create<Boolean>("pgq.retrieve.all.data.key")
    }

    override val inEditorResultsTitle = "Polyglot Query Execution Results"

    override var retrieveAllData: Boolean
        get() = getOrCreateUserData(KEY_RETRIEVE_ALL_DATA) { false }
        set(value) = putUserData(KEY_RETRIEVE_ALL_DATA, value)

    override var virtualParameters: Map<String, PolyglotQueryVirtualParameter>?
        get() = getUserData(KEY_PARAMETERS)
        set(value) = putUserData(KEY_PARAMETERS, value)

    override var csvResultsDisposable: com.intellij.openapi.Disposable? = null

    private var renderParametersJob: Job? = null

    override fun renderInEditorParameters() {
        PolyglotQueryInEditorParametersView.getInstance(project).renderParameters(this)
    }

    override fun renderExecutionResult(result: FlexibleSearchExecResult) = PolyglotQueryInEditorResultsView.getInstance(project).resultView(this, result) { coroutineScope, view ->
        coroutineScope.launch {
            edtWriteAction {
                inEditorResultsView = view
            }
        }
    }

    override fun showLoader(richMessage: String) {
        inEditorResultsView = PolyglotQueryInEditorResultsView.getInstance(project).executingView(richMessage)
    }

    override fun refreshParameters(delayMs: Duration) {
        renderParametersJob?.cancel()
        renderParametersJob = CoroutineScope(Dispatchers.Default).launch {
            delay(delayMs)

            if (project.isDisposed || !inEditorParameters) return@launch

            PolyglotQueryInEditorParametersView.getInstance(project).renderParameters(this@PolyglotQuerySplitEditorBase)
        }
    }

    override fun getName() = "Polyglot Query Split Editor"

    init {
        horizontalSplitter.firstComponent = textEditor.component
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
