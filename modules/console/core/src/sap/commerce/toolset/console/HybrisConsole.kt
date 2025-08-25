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

package sap.commerce.toolset.console

import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.console.ConsoleRootType
import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.ide.ActivityTracker
import com.intellij.lang.Language
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager
import com.intellij.ui.AnimatedIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sap.commerce.toolset.exec.context.ConsoleAwareExecResult
import sap.commerce.toolset.exec.context.ExecContext
import sap.commerce.toolset.exec.context.ReplicaContext
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.exec.settings.state.generatedURL
import java.io.Serial
import javax.swing.Icon

abstract class HybrisConsole<E : ExecContext>(
    project: Project, title: String, language: Language,
    private val coroutineScope: CoroutineScope
) : LanguageConsoleImpl(project, title, language) {

    private val consoleId: String = "hybris.console.$title"
    private val consoleRootType = object : ConsoleRootType(consoleId, null) {}
    private val consoleHistoryController = ConsoleHistoryController(consoleRootType, consoleId, this)

    init {
        isEditable = true

        consoleHistoryController.install()

        coroutineScope.launch {
            edtWriteAction {
                printDefaultText()
            }
        }
    }

    val content: String
        get() = currentEditor.document.text
    val context
        get() = currentExecutionContext(content)

    abstract fun currentExecutionContext(content: String): E
    abstract fun title(): String
    abstract fun tip(): String
    abstract fun execute()
    abstract fun activeConnection(): ExecConnectionSettingsState?

    open fun icon(): Icon? = language.associatedFileType?.icon
    open fun disabledIcon(): Icon? = AnimatedIcon.Default.INSTANCE
    open fun onSelection() = Unit
    open fun canExecute(): Boolean = isEditable
    open fun printDefaultText() = setInputText("")

    override fun dispose() {
        LineStatusTrackerManager.getInstance(project).releaseTrackerFor(editorDocument, consoleEditor)
        super.dispose()
    }

    fun beforeExecution() {
        coroutineScope.launch {
            edtWriteAction {
                isEditable = false
                addQueryToHistory()
            }
        }
    }

    fun afterExecution() {
        coroutineScope.launch {
            edtWriteAction { isEditable = true }
            readAction { ActivityTracker.getInstance().inc() }
        }
    }

    fun print(result: ConsoleAwareExecResult, isEditable: Boolean = true) {
        coroutineScope.launch {
            edtWriteAction {
                printResult(result)
                this@HybrisConsole.isEditable = isEditable
            }
        }
    }

    fun addQueryToHistory() {
        // Process input and add to history
        val document = currentEditor.document
        val textForHistory = document.text.trim()
        val range = TextRange(0, document.textLength)

        currentEditor.selectionModel.setSelection(range.startOffset, range.endOffset)
        addToHistory(range, consoleEditor, false)
        printDefaultText()

        if (textForHistory.isNotBlank()) {
            consoleHistoryController.addToHistory(textForHistory)
        }
    }

    protected open fun printResult(result: ConsoleAwareExecResult) {
        printHost(result.replicaContext)
        printPlainText(result)
    }

    protected fun printHost(replicaContext: ReplicaContext?) {
        val activeConnectionSettings = activeConnection() ?: return
        print("[HOST] ", ConsoleViewContentType.SYSTEM_OUTPUT)
        activeConnectionSettings.name
            ?.let { name -> print("($name) ", ConsoleViewContentType.LOG_INFO_OUTPUT) }
        replicaContext
            ?.replicaId
            ?.let { print("($it) ", ConsoleViewContentType.LOG_VERBOSE_OUTPUT) }

        print("${activeConnectionSettings.generatedURL}\n", ConsoleViewContentType.NORMAL_OUTPUT)
    }

    private fun printPlainText(result: ConsoleAwareExecResult) {
        if (result.hasError) {
            print("[ERROR]\n", ConsoleViewContentType.SYSTEM_OUTPUT)
            listOfNotNull(result.errorMessage, result.errorDetailMessage)
                .forEach { print("$it\n", ConsoleViewContentType.ERROR_OUTPUT) }

            return
        }

        printOutput("OUTPUT", result.output)
        printOutput("RESULT", result.result)

        print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
    }

    private fun printOutput(type: String, text: String?) = text?.let {
        print("[$type]\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        print(it, ConsoleViewContentType.NORMAL_OUTPUT)
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -2700270816491881103L
    }

}