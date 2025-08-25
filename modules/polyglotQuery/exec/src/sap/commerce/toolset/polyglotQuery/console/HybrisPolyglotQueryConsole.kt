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

package sap.commerce.toolset.polyglotQuery.console

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.polyglotQuery.PolyglotQueryLanguage
import sap.commerce.toolset.polyglotQuery.editor.PolyglotQueryVirtualParameter
import java.awt.BorderLayout
import java.io.Serial

class HybrisPolyglotQueryConsole(
    project: Project,
    coroutineScope: CoroutineScope
) : HybrisConsole<FlexibleSearchExecContext>(project, "[y] PolyglotQuery", PolyglotQueryLanguage, coroutineScope) {

    private lateinit var maxRowsSpinner: JBIntSpinner

    init {
        val myPanel = panel {
            row {
                maxRowsSpinner = spinner(1..Integer.MAX_VALUE)
                    .label("Max rows:")
                    .component
                    .apply { value = 200 }
            }
        }

        add(myPanel, BorderLayout.NORTH)
    }

    override fun currentExecutionContext(content: String) = FlexibleSearchExecContext(
        content = content,
        queryMode = QueryMode.PolyglotQuery,
        settings = FlexibleSearchExecContext.defaultSettings(activeConnection()).modifiable()
            .apply {
                maxCount = maxRowsSpinner.number
            }
            .immutable()
    )

    fun print(values: Collection<PolyglotQueryVirtualParameter>?) {
        if (values == null) return

        print(" Parameters:\n", ConsoleViewContentType.SYSTEM_OUTPUT)

        values.forEachIndexed { index, param ->
            print("  | ", ConsoleViewContentType.LOG_VERBOSE_OUTPUT)
            print(param.name, ConsoleViewContentType.NORMAL_OUTPUT)
            print(" : ", ConsoleViewContentType.LOG_VERBOSE_OUTPUT)
            print(param.presentationValue, ConsoleViewContentType.USER_INPUT)

            if (index < values.size) print("\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    override fun title() = "Polyglot Query"
    override fun tip() = "Polyglot Persistence Query Language Console (available only for 1905+)"
    override fun execute() = FlexibleSearchExecClient.getInstance(project).execute(
        context = context,
        beforeCallback = { _ -> beforeExecution() },
        resultCallback = { _, result -> print(result) }
    )

    override fun activeConnection() = HacExecConnectionService.getInstance(project).activeConnection

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1330953384857131472L
    }
}