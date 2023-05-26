package com.intellij.idea.plugin.hybris.tools.remote.console.actions.handler


import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.ui.ConsoleViewContentType.*
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexMonitorConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisSolrSearchConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.view.HybrisTabs
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult.HybrisHttpResultBuilder.createResult
import com.intellij.json.JsonFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil


/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class HybrisConsoleExecuteActionHandler(private val project: Project,
                                        private val preserveMarkup: Boolean) {

    private fun setEditorEnabled(console: HybrisConsole, enabled: Boolean) {
        console.consoleEditor.isRendererMode = !enabled
        ApplicationManager.getApplication().invokeLater { console.consoleEditor.component.updateUI() }
    }

    private fun processLine(console: HybrisConsole, query: String) {
        ApplicationManager.getApplication().runReadAction {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Execute HTTP Call ...") {
                override fun run(indicator: ProgressIndicator) {
                    isProcessRunning = true
                    try {
                        setEditorEnabled(console, false)
                        val httpResult = console.execute(query)

                        when (console) {
                            is HybrisImpexMonitorConsole -> {
                                console.clear()
                                printSyntaxText(console, httpResult.output, ImpexFileType.INSTANCE)
                            }
                            is HybrisSolrSearchConsole -> {
                                console.clear()
                                if (httpResult.hasError()) {
                                    printSyntaxText(console, httpResult.errorMessage, PlainTextFileType.INSTANCE)
                                } else {
                                    printSyntaxText(console, httpResult.output, JsonFileType.INSTANCE)
                                }

                            }
                            else -> printPlainText(console, httpResult)
                        }
                    } finally {
                        isProcessRunning = false
                        setEditorEnabled(console, true)
                    }
                }
            })
        }
    }

    private fun printPlainText(console: HybrisConsole, httpResult: HybrisHttpResult?) {
        val result = createResult().errorMessage(httpResult?.errorMessage).output(httpResult?.output).result(httpResult?.result).detailMessage(httpResult?.detailMessage).build()
        val detailMessage = result.detailMessage
        val output = result.output
        val res = result.result
        val errorMessage = result.errorMessage

        if (result.hasError()) {
            console.print("[ERROR] \n", SYSTEM_OUTPUT)
            console.print("$errorMessage\n$detailMessage\n", ERROR_OUTPUT)
            return
        }
        if (!StringUtil.isEmptyOrSpaces(output)) {
            console.print("[OUTPUT] \n", SYSTEM_OUTPUT)
            console.print(output, NORMAL_OUTPUT)
        }
        if (!StringUtil.isEmptyOrSpaces(res)) {
            console.print("[RESULT] \n", SYSTEM_OUTPUT)
            console.print(res, NORMAL_OUTPUT)
        }
    }

    private fun printSyntaxText(console: HybrisConsole, output: String, fileType: FileType) {
        ConsoleViewUtil.printAsFileType(console, output, fileType)
    }

    fun runExecuteAction(tabbedPane: HybrisTabs) {

        val console = tabbedPane.activeConsole()
        val consoleHistoryModel = ConsoleHistoryController.getController(console)

        if (consoleHistoryModel != null) {
            execute(console, consoleHistoryModel)
        }
    }

    private fun execute(console: HybrisConsole,
                        consoleHistoryController: ConsoleHistoryController) {

        // Process input and add to history
        val document = console.currentEditor.document
        val textForHistory = document.text
        console.preProcessors().forEach { processor -> console.setInputText(processor.process(console)) }

        val query = document.text
        val range = TextRange(0, document.textLength)

        if (query.isNotEmpty() || console is HybrisImpexMonitorConsole) {
            console.currentEditor.selectionModel.setSelection(range.startOffset, range.endOffset)
            console.addToHistory(range, console.consoleEditor, preserveMarkup)
            console.printDefaultText()

            if (!StringUtil.isEmptyOrSpaces(textForHistory)) {
                consoleHistoryController.addToHistory(textForHistory.trim())
            }

            processLine(console, query)
        }
    }

    var isProcessRunning: Boolean = false

}
