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

package sap.commerce.toolset.groovy.actionSystem

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.exec.context.DefaultExecResult
import sap.commerce.toolset.groovy.console.HybrisGroovyConsole
import sap.commerce.toolset.groovy.editor.GroovySplitEditor
import sap.commerce.toolset.groovy.editor.groovySplitEditor
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.actionSystem.ExecuteStatementAction
import sap.commerce.toolset.settings.state.TransactionMode
import sap.commerce.toolset.settings.yDeveloperSettings

class GroovyExecuteAction : ExecuteStatementAction<HybrisGroovyConsole, GroovySplitEditor>(
    GroovyLanguage,
    HybrisGroovyConsole::class,
    "Execute Groovy Script",
    "Execute Groovy Script on a remote SAP Commerce instance",
    HybrisIcons.Console.Actions.EXECUTE
) {

    override fun fileEditor(e: AnActionEvent): GroovySplitEditor? = e.groovySplitEditor()

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = fileEditor(e) ?: return
        val fileName = e.getData(CommonDataKeys.PSI_FILE)?.name
        val prefix = fileName ?: "script"

        val transactionMode = project.yDeveloperSettings.groovySettings.txMode
        val executionClient = GroovyExecClient.getInstance(project)
        val contexts = executionClient.connectionContext.replicaContexts
            .map {
                GroovyExecContext(
                    executionTitle = "$prefix | ${it.replicaId} | ${GroovyExecContext.DEFAULT_TITLE}",
                    content = content,
                    transactionMode = transactionMode,
                    replicaContext = it
                )
            }
            .takeIf { it.isNotEmpty() }
            ?: listOf(
                GroovyExecContext(
                    executionTitle = "$prefix | ${GroovyExecContext.DEFAULT_TITLE}",
                    content = content,
                    transactionMode = transactionMode
                )
            )

        if (fileEditor.inEditorResults) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader("$prefix | 1 of ${contexts.size} | ${GroovyExecContext.DEFAULT_TITLE}")
            var completed = 0

            executionClient.execute(
                contexts = contexts,
                resultCallback = { _, _ ->
                    completed++
                    fileEditor.showLoader("$prefix | $completed of ${contexts.size} | ${GroovyExecContext.DEFAULT_TITLE}")
                },
                afterCallback = { _, results ->
                    fileEditor.renderExecutionResults(results)
                    fileEditor.putUserData(KEY_QUERY_EXECUTING, false)
                },
                onError = { _, e ->
                    fileEditor.renderExecutionResults(listOf(
                        DefaultExecResult(
                            errorMessage = e.message,
                            errorDetailMessage = e.stackTraceToString()
                        )
                    ))
                    fileEditor.putUserData(KEY_QUERY_EXECUTING, false)
                }
            )
        } else {
            val console = openConsole(project, content) ?: return

            executionClient.execute(
                contexts = contexts,
                resultCallback = { _, result -> console.print(result, false) },
                afterCallback = { _, _ -> console.afterExecution() }
            )
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val project = e.project ?: return

        when (project.yDeveloperSettings.groovySettings.txMode) {
            TransactionMode.ROLLBACK -> {
                e.presentation.icon = HybrisIcons.Console.Actions.EXECUTE_ROLLBACK
                e.presentation.text = "Execute Groovy Script<br/>Commit Mode <strong><font color='#C75450'>OFF</font></strong>"
            }

            TransactionMode.COMMIT -> {
                e.presentation.icon = HybrisIcons.Console.Actions.EXECUTE
                e.presentation.text = "Execute Groovy Script<br/>Commit Mode <strong><font color='#57965C'>ON</font></strong>"
            }
        }
    }

    override fun processContent(e: AnActionEvent, content: String, editor: Editor, project: Project): String {
        val psiFile = CommonDataKeys.PSI_FILE.getData(e.dataContext) ?: return content

        val selectionModel = editor.selectionModel

        var processedContent = content

        if (selectionModel.hasSelection() && psiFile is GroovyFile && !psiFile.importStatements.isEmpty()) {

            val document = editor.document
            val selectionStartLine = document.getLineNumber(selectionModel.selectionStart)
            val selectionEndLine = document.getLineNumber(selectionModel.selectionEnd)

            val missingImports = psiFile.importStatements.filter { import ->
                val importLine = document.getLineNumber(import.textOffset)
                importLine !in selectionStartLine..selectionEndLine
            }

            if (!missingImports.isEmpty()) {
                val importStatements = missingImports.map { it.text }
                val importBlock = importStatements.joinToString(separator = "\n")
                processedContent = "$importBlock\n\n$processedContent"
            }

        }

        processedContent = "/* ${psiFile.name} */\n$processedContent"

        return processedContent
    }
}