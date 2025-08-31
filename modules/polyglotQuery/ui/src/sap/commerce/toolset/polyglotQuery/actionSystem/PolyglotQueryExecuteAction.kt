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
package sap.commerce.toolset.polyglotQuery.actionSystem

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.apache.http.HttpStatus
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchExecutionContextSettings
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.actionSystem.ExecuteStatementAction
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.i18n
import sap.commerce.toolset.polyglotQuery.PolyglotQueryLanguage
import sap.commerce.toolset.polyglotQuery.console.HybrisPolyglotQueryConsole
import sap.commerce.toolset.polyglotQuery.editor.PolyglotQuerySplitEditorEx
import sap.commerce.toolset.polyglotQuery.editor.polyglotQuerySplitEditor
import sap.commerce.toolset.polyglotQuery.file.PolyglotQueryFile
import sap.commerce.toolset.polyglotQuery.psi.PolyglotQueryTypeKeyName
import sap.commerce.toolset.settings.state.TransactionMode

class PolyglotQueryExecuteAction : ExecuteStatementAction<HybrisPolyglotQueryConsole, PolyglotQuerySplitEditorEx>(
    PolyglotQueryLanguage,
    HybrisPolyglotQueryConsole::class,
    i18n("hybris.pgq.actions.execute_query"),
    i18n("hybris.pgq.actions.execute_query.description"),
    HybrisIcons.Console.Actions.EXECUTE
) {

    override fun fileEditor(e: AnActionEvent): PolyglotQuerySplitEditorEx? = e.polyglotQuerySplitEditor()

    override fun actionPerformed(e: AnActionEvent, project: Project, content: String) {
        val fileEditor = fileEditor(e) ?: return
        val itemType = e.getData(CommonDataKeys.PSI_FILE)
            ?.asSafely<PolyglotQueryFile>()
            ?.let { PsiTreeUtil.findChildOfType(it, PolyglotQueryTypeKeyName::class.java) }
            ?.typeName
            ?: "Item"
        val connectionSettings = HacExecConnectionService.getInstance(project).activeConnection
        val executionContextSettings = e.flexibleSearchExecutionContextSettings { FlexibleSearchExecContext.defaultSettings(connectionSettings) }

        if (fileEditor.inEditorParameters) executeParametrizedQuery(project, fileEditor, e, itemType, content, connectionSettings, executionContextSettings)
        else executeDirectQuery(project, fileEditor, e, itemType, content, connectionSettings, executionContextSettings)
    }

    private fun executeParametrizedQuery(
        project: Project,
        fileEditor: PolyglotQuerySplitEditorEx,
        e: AnActionEvent,
        typeCode: String,
        content: String,
        connectionSettings: HacConnectionSettingsState,
        executionContextSettings: FlexibleSearchExecContext.Settings
    ) {
        val missingParameters = fileEditor.virtualParameters?.values
            ?.filter { it.sqlValue.isBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ", "missing values for [", "]") { it.name }

        if (missingParameters != null) {
            val result = FlexibleSearchExecResult(
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

        executeParametrizedGroovyQuery(e, project, fileEditor, typeCode, content, connectionSettings, executionContextSettings)
    }

    private fun executeDirectQuery(
        project: Project,
        fileEditor: PolyglotQuerySplitEditorEx,
        e: AnActionEvent,
        typeCode: String,
        content: String,
        connectionSettings: HacConnectionSettingsState,
        executionContextSettings: FlexibleSearchExecContext.Settings
    ) {
        val context = FlexibleSearchExecContext(
            connection = connectionSettings,
            content = content,
            queryMode = QueryMode.PolyglotQuery,
            settings = executionContextSettings
        )

        if (fileEditor.inEditorResults) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader(context.executionTitle)

            FlexibleSearchExecClient.getInstance(project).execute(context) { coroutineScope, result ->
                val pks = getPKsFromDirectQuery(result)

                if (fileEditor.retrieveAllData && pks != null) executeFlexibleSearchForPKs(project, typeCode, pks, connectionSettings, executionContextSettings) { c, r ->
                    renderInEditorExecutionResult(e, fileEditor, c, r)
                }
                else renderInEditorExecutionResult(e, fileEditor, coroutineScope, result)
            }
        } else {
            val console = openConsole(project, content) ?: return

            FlexibleSearchExecClient.getInstance(project).execute(context) { coroutineScope, result ->
                val pks = getPKsFromDirectQuery(result)

                if (fileEditor.retrieveAllData && pks != null) executeFlexibleSearchForPKs(project, typeCode, pks, connectionSettings, executionContextSettings) { _, r ->
                    console.print(r)
                }
                else console.print(result)
            }
        }
    }

    private fun getPKsFromDirectQuery(result: FlexibleSearchExecResult): String? = result.output
        ?.takeIf { it.isNotEmpty() }
        ?.replace("\n", ",")
        ?.replace("PK", "")
        ?.trim()
        ?.removePrefix(",")
        ?.removeSuffix(",")

    private fun executeParametrizedGroovyQuery(
        e: AnActionEvent,
        project: Project,
        fileEditor: PolyglotQuerySplitEditorEx,
        typeCode: String,
        content: String,
        connectionSettings: HacConnectionSettingsState,
        executionContextSettings: FlexibleSearchExecContext.Settings
    ) {
        val virtualParameters = fileEditor.virtualParameters?.values
            ?.filter { it.sqlValue.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(",\n", "[", "]") { "${it.name} : ${it.sqlValue}" }
            ?: "[:]"
        val textBlock = "\"\"\""
        val scriptOutputLogic = if (fileEditor.retrieveAllData) """
            println flexibleSearchService
                        .<ItemModel>search(query, params)
                        .result.collect { it.pk }.join(",")
        """.trimIndent()
        else """
            println "PK"
            flexibleSearchService
                .<ItemModel>search(query, params)
                .result.forEach { println it.pk }
        """.trimIndent()
        val context = GroovyExecContext(
            connection = connectionSettings,
            content = """
                            import de.hybris.platform.core.model.ItemModel
                            import de.hybris.platform.servicelayer.search.FlexibleSearchService
        
                            def query = $textBlock$content$textBlock
                            def params = $virtualParameters
    
                            $scriptOutputLogic
                        """.trimIndent(),
            transactionMode = TransactionMode.ROLLBACK,
            timeout = executionContextSettings.timeout,
        )

        if (fileEditor.inEditorResults) {
            fileEditor.putUserData(KEY_QUERY_EXECUTING, true)
            fileEditor.showLoader(context.executionTitle)

            GroovyExecClient.getInstance(project).execute(context) { coroutineScope, result ->
                val pks = result.output?.takeIf { it.isNotEmpty() }

                if (fileEditor.retrieveAllData && pks != null) executeFlexibleSearchForPKs(project, typeCode, pks, connectionSettings, executionContextSettings) { c, r ->
                    renderInEditorExecutionResult(e, fileEditor, c, r)
                }
                else {
                    renderInEditorExecutionResult(e, fileEditor, coroutineScope, FlexibleSearchExecResult.from(result))
                }
            }
        } else {
            val console = openConsole(project, content) ?: return

            GroovyExecClient.getInstance(project).execute(context) { _, result ->
                val pks = result.output?.takeIf { it.isNotEmpty() }

                if (fileEditor.retrieveAllData && pks != null) executeFlexibleSearchForPKs(project, typeCode, pks, connectionSettings, executionContextSettings) { _, r ->
                    printConsoleExecutionResult(console, fileEditor, r)
                }
                else printConsoleExecutionResult(console, fileEditor, FlexibleSearchExecResult.from(result))
            }
        }
    }

    private fun printConsoleExecutionResult(console: HybrisPolyglotQueryConsole, fileEditor: PolyglotQuerySplitEditorEx, result: FlexibleSearchExecResult) {
        console.print(fileEditor.virtualParameters?.values)
        console.print(result)
    }

    private fun renderInEditorExecutionResult(
        e: AnActionEvent,
        fileEditor: PolyglotQuerySplitEditorEx,
        coroutineScope: CoroutineScope,
        result: FlexibleSearchExecResult
    ) {
        fileEditor.renderExecutionResult(result)
        fileEditor.putUserData(KEY_QUERY_EXECUTING, false)

        coroutineScope.launch {
            readAction { ActivityTracker.getInstance().inc() }
        }
    }

    private fun executeFlexibleSearchForPKs(
        project: Project, typeCode: String, pks: String,
        connectionSettings: HacConnectionSettingsState,
        executionContextSettings: FlexibleSearchExecContext.Settings,
        exec: (CoroutineScope, FlexibleSearchExecResult) -> Unit
    ) = FlexibleSearchExecClient.getInstance(project)
        .execute(
            FlexibleSearchExecContext(
                connection = connectionSettings,
                content = "SELECT * FROM {$typeCode} WHERE {pk} in ($pks)",
                settings = executionContextSettings
            )
        ) { coroutineScope, result ->
            exec.invoke(coroutineScope, result)
        }

}