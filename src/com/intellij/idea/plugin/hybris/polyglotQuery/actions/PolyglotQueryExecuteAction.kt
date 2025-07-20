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
package com.intellij.idea.plugin.hybris.polyglotQuery.actions

import com.intellij.idea.plugin.hybris.actions.ExecuteStatementAction
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.polyglotQuery.editor.PolyglotQuerySplitEditor
import com.intellij.idea.plugin.hybris.polyglotQuery.editor.polyglotQuerySplitEditor
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisPolyglotQueryConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.QueryMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.GroovyExecutionContext
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.ui.AnimatedIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.apache.http.HttpStatus

class PolyglotQueryExecuteAction : ExecuteStatementAction<HybrisPolyglotQueryConsole>(
    PolyglotQueryLanguage,
    HybrisPolyglotQueryConsole::class,
    message("hybris.pgq.actions.execute_query"),
    message("hybris.pgq.actions.execute_query.description"),
    HybrisIcons.Console.Actions.EXECUTE
) {
    override fun update(e: AnActionEvent) {
        super.update(e)

        val queryExecuting = e.polyglotQuerySplitEditor()
            ?.getUserData(KEY_QUERY_EXECUTING)
            ?: false

        e.presentation.isEnabledAndVisible = e.presentation.isEnabledAndVisible
        e.presentation.isEnabled = e.presentation.isEnabledAndVisible && !queryExecuting
        e.presentation.disabledIcon = if (queryExecuting) AnimatedIcon.Default.INSTANCE
        else HybrisIcons.Console.Actions.EXECUTE
    }

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = e.polyglotQuerySplitEditor() ?: return

        if (fileEditor.inEditorParameters) executeParametrizedQuery(e, project, fileEditor, content)
        else executeDirectQuery(e, project, fileEditor, content)
    }

    private fun executeParametrizedQuery(e: AnActionEvent, project: Project, fileEditor: PolyglotQuerySplitEditor, content: String) {
        val missingParameters = fileEditor.queryParameters?.values
            ?.filter { it.sqlValue.isBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ", "missing values for [", "]") { it.name }

        if (missingParameters != null) {
            val result = DefaultExecutionResult(
                statusCode = HttpStatus.SC_BAD_REQUEST,
                errorMessage = missingParameters
            )

            if (fileEditor.inEditorResults) {
                fileEditor.renderExecutionResult(result)
            } else {
                val console = openConsole(project, content) ?: return
                printConsoleExecutionResult(console, fileEditor, result)
            }
            return
        }

        executeParametrizedGroovyQuery(e, project, fileEditor, content)
    }

    private fun executeDirectQuery(e: AnActionEvent, project: Project, fileEditor: PolyglotQuerySplitEditor, content: String) {
        val context = FlexibleSearchExecutionContext(
            content = content,
            queryMode = QueryMode.PolyglotQuery
        )

        if (fileEditor.inEditorResults) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader()

            FlexibleSearchExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                getPKsFromDirectQuery(result)
                    ?.let {
                        executeFlexibleSearchForPKs(fileEditor, project, it) { c, r ->
                            renderInEditorExecutionResult(e, fileEditor, r, c)
                        }
                    }
                    ?: renderInEditorExecutionResult(e, fileEditor, result, coroutineScope)
            }
        } else {
            val console = openConsole(project, content) ?: return

            FlexibleSearchExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                getPKsFromDirectQuery(result)
                    ?.let {
                        executeFlexibleSearchForPKs(fileEditor, project, it) { c, r ->
                            console.print(r)
                        }
                    }
                    ?: console.print(result)

            }
        }
    }

    private fun getPKsFromDirectQuery(result: DefaultExecutionResult): String? = result.output
        ?.takeIf { it.isNotEmpty() }
        ?.replace("\n", ",")
        ?.replace("PK", "")

    private fun executeParametrizedGroovyQuery(e: AnActionEvent, project: Project, fileEditor: PolyglotQuerySplitEditor, content: String) {
        val queryParameters = fileEditor.queryParameters?.values
            ?.filter { it.sqlValue.isNotBlank() }
            ?.joinToString(",\n", "[", "]") { "${it.name} : ${it.sqlValue}" }
            ?.takeIf { it.isNotEmpty() }
            ?: "[:]"
        val textBlock = "\"\"\""
        val context = GroovyExecutionContext(
            content = """
                            import de.hybris.platform.core.model.ItemModel
                            import de.hybris.platform.servicelayer.search.FlexibleSearchService
        
                            def query = $textBlock$content$textBlock
                            def params = $queryParameters
    
                            flexibleSearchService
                                .<ItemModel>search(query, params)
                                .result.collect { it.pk }.join(",")
                        """.trimIndent()
        )

        if (fileEditor.inEditorResults) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader()

            GroovyExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                result.output
                    ?.takeIf { it.isNotEmpty() }
                    ?.let {
                        executeFlexibleSearchForPKs(fileEditor, project, content) { c, r ->
                            renderInEditorExecutionResult(e, fileEditor, r, c)
                        }
                    }
                    ?: renderInEditorExecutionResult(e, fileEditor, result, coroutineScope)
            }
        } else {
            val console = openConsole(project, content) ?: return

            GroovyExecutionClient.getInstance(project).execute(context) { coroutineScope, result ->
                result.output
                    ?.takeIf { it.isNotEmpty() }
                    ?.let {
                        executeFlexibleSearchForPKs(fileEditor, project, content) { c, r ->
                            printConsoleExecutionResult(console, fileEditor, r)
                        }
                    } ?: printConsoleExecutionResult(console, fileEditor, result)
            }
        }
    }

    private fun printConsoleExecutionResult(console: HybrisPolyglotQueryConsole, fileEditor: PolyglotQuerySplitEditor, result: DefaultExecutionResult) {
        console.print(fileEditor.queryParameters?.values)
        console.print(result)
    }

    private fun renderInEditorExecutionResult(
        e: AnActionEvent,
        fileEditor: PolyglotQuerySplitEditor,
        result: DefaultExecutionResult,
        coroutineScope: CoroutineScope
    ) {
        fileEditor.renderExecutionResult(result)
        fileEditor.putUserData(KEY_QUERY_EXECUTING, false)

        coroutineScope.launch {
            readAction { this@PolyglotQueryExecuteAction.update(e) }
        }
    }

    private fun executeFlexibleSearchForPKs(
        fileEditor: PolyglotQuerySplitEditor,
        project: Project,
        pks: String,
        exec: (CoroutineScope, DefaultExecutionResult) -> Unit
    ) {
        val typeCode = fileEditor.getUserData(PolyglotQuerySplitEditor.KEY_TYPE_CODE)
        val fxsContext = FlexibleSearchExecutionContext(
            content = "SELECT * FROM {$typeCode} WHERE {pk} in ($pks)",
        )

        FlexibleSearchExecutionClient.getInstance(project).execute(fxsContext) { co, r ->
            exec.invoke(co, r)
        }
    }

    companion object {
        private val KEY_QUERY_EXECUTING = Key.create<Boolean>("pgq.query.execution.state")
    }
}