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

package sap.commerce.toolset.groovy.console

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.plugins.groovy.GroovyLanguage
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.settings.state.TransactionMode
import java.awt.BorderLayout
import java.io.Serial

class HybrisGroovyConsole(
    project: Project,
    coroutineScope: CoroutineScope
) : HybrisConsole<GroovyExecContext>(project, "[y] Groovy Console", GroovyLanguage, coroutineScope) {

    private lateinit var commitCheckbox: JBCheckBox

    init {
        val myPanel = panel {
            row {
                commitCheckbox = checkBox("Commit mode")
                    .component
            }
        }

        add(myPanel, BorderLayout.NORTH)
    }

    override fun currentExecutionContext(content: String) = GroovyExecContext(
        connection = activeConnection(),
        content = content,
        transactionMode = if (commitCheckbox.isSelected) TransactionMode.COMMIT else TransactionMode.ROLLBACK,
        timeout = activeConnection().timeout,
    )

    override fun title() = "Groovy Scripting"
    override fun tip() = "Groovy Console"
    override fun execute() = GroovyExecClient.getInstance(project).execute(
        context = context,
        beforeCallback = { _ -> beforeExecution() },
        resultCallback = { _, result -> print(result) }
    )

    override fun activeConnection() = HacExecConnectionService.getInstance(project).activeConnection

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3858827004057439840L
    }
}