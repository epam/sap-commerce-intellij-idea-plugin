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

package com.intellij.idea.plugin.hybris.tools.remote.console

import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.ui.ConsoleViewContentType.*
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult.HybrisHttpResultBuilder.createResult
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.ReplicaContext
import com.intellij.lang.Language
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Serial
import javax.swing.Icon

abstract class HybrisConsole<E : ExecutionContext>(
    project: Project,
    title: String,
    language: Language,
) : LanguageConsoleImpl(project, title, language) {

    protected val borders10 = JBUI.Borders.empty(10)
    protected val borders5 = JBUI.Borders.empty(5, 10)
    protected val bordersLabel = JBUI.Borders.empty(10, 10, 10, 0)

    init {
        isEditable = true
        this.printDefaultText()
    }

    val content: String
        get() = currentEditor.document.text
    val context
        get() = currentExecutionContext(content)
    val icon: Icon?
        get() = language.associatedFileType?.icon

    internal abstract fun currentExecutionContext(content: String): E
    abstract fun title(): String
    abstract fun tip(): String

    open fun onSelection() = Unit
    open fun canExecute(): Boolean = isEditable
    open fun printDefaultText() = setInputText("")

    fun printExecutionResults(coroutineScope: CoroutineScope, result: ExecutionResult) {
        coroutineScope.launch {
            edtWriteAction {
                addQueryToHistory()
                printResults(result)

                isEditable = true
            }
        }
    }

    override fun dispose() {
        LineStatusTrackerManager.getInstance(project).releaseTrackerFor(editorDocument, consoleEditor)
        super.dispose()
    }

    fun addQueryToHistory(): String? {
        val consoleHistoryController = ConsoleHistoryController.getController(this)
            ?: return null
        // Process input and add to history
        val document = currentEditor.document
        val textForHistory = document.text

        val query = document.text
        val range = TextRange(0, document.textLength)

        if (query.isNotEmpty()) {
            currentEditor.selectionModel.setSelection(range.startOffset, range.endOffset)
            addToHistory(range, consoleEditor, false)
            printDefaultText()

            if (!StringUtil.isEmptyOrSpaces(textForHistory)) {
                consoleHistoryController.addToHistory(textForHistory.trim())
            }

            return query
        }
        return null
    }

    @Deprecated("review")
    open fun getQuery(): String? {
        val consoleHistoryController = ConsoleHistoryController.getController(this)
            ?: return null
        // Process input and add to history
        val document = currentEditor.document
        val query = document.text
        val range = TextRange(0, document.textLength)

        if (query.isNotEmpty()) {
            currentEditor.selectionModel.setSelection(range.startOffset, range.endOffset)
            addToHistory(range, consoleEditor, false)
            printDefaultText()

            if (!StringUtil.isEmptyOrSpaces(query)) {
                consoleHistoryController.addToHistory(query.trim())
            }

            return query
        }
        return null
    }

    internal open fun printResults(
        httpResult: ExecutionResult,
        replicaContext: ReplicaContext? = null
    ) {
        printCurrentHost(RemoteConnectionType.Hybris, replicaContext)
        printPlainText(httpResult)
    }

    internal fun printCurrentHost(remoteConnectionType: RemoteConnectionType, replicaContext: ReplicaContext?) {
        val activeConnectionSettings = RemoteConnectionUtil.getActiveRemoteConnectionSettings(project, remoteConnectionType)
        print("[HOST] ", SYSTEM_OUTPUT)
        activeConnectionSettings.displayName
            ?.let { name -> print("($name) ", LOG_INFO_OUTPUT) }
        replicaContext
            ?.replicaCookie
            ?.let { print("($it) ", LOG_INFO_OUTPUT) }

        print("${activeConnectionSettings.generatedURL}\n", NORMAL_OUTPUT)
    }

    internal fun printPlainText(httpResult: ExecutionResult) {
        val result = createResult()
            .errorMessage(httpResult.errorMessage)
            .output(httpResult.output)
            .result(httpResult.result)
            .detailMessage(httpResult.detailMessage)
            .build()
        val detailMessage = result.detailMessage
        val output = result.output
        val res = result.result
        val errorMessage = result.errorMessage

        if (result.hasError()) {
            print("[ERROR] \n", SYSTEM_OUTPUT)
            print("$errorMessage\n$detailMessage\n", ERROR_OUTPUT)
            return
        }
        if (!StringUtil.isEmptyOrSpaces(output)) {
            print("[OUTPUT] \n", SYSTEM_OUTPUT)
            print(output, NORMAL_OUTPUT)
        }
        if (!StringUtil.isEmptyOrSpaces(res)) {
            print("[RESULT] \n", SYSTEM_OUTPUT)
            print(res, NORMAL_OUTPUT)
        }

        print("\n", NORMAL_OUTPUT)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -2700270816491881103L
    }

}


