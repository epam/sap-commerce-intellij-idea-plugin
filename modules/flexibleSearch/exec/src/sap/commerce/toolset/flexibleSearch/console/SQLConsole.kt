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

package sap.commerce.toolset.flexibleSearch.console

import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.sql.psi.SqlLanguage
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecClient
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.exec.context.QueryMode
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.settings.state.TransactionMode
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.Icon

class SQLConsole(project: Project, coroutineScope: CoroutineScope) : HybrisConsole<FlexibleSearchExecContext>(
    project,
    "[y] SQL Console",
    if (Plugin.DATABASE.isActive()) SqlLanguage.INSTANCE else PlainTextLanguage.INSTANCE,
    coroutineScope
) {

    private lateinit var commitCheckbox: JBCheckBox
    private lateinit var maxRowsSpinner: JBIntSpinner

    init {
        val myPanel = panel {
            row {
                commitCheckbox = checkBox("Commit mode")
                    .component
                maxRowsSpinner = spinner(1..Int.MAX_VALUE)
                    .label("Rows:")
                    .component
                    .also { it.value = 200 }

            }
        }

        add(myPanel, BorderLayout.NORTH)
    }

    override fun currentExecutionContext(content: String) = FlexibleSearchExecContext(
        content = content,
        transactionMode = if (commitCheckbox.isSelected) TransactionMode.COMMIT else TransactionMode.ROLLBACK,
        queryMode = QueryMode.SQL,
        settings = FlexibleSearchExecContext.defaultSettings(project).modifiable()
            .apply {
                maxCount = maxRowsSpinner.value.toString().toInt()
            }
            .immutable()
    )

    override fun title(): String = "SQL"
    override fun tip(): String = "SQL Console"
    override fun execute() = FlexibleSearchExecClient.getInstance(project).execute(
        context = context,
        beforeCallback = { _ -> beforeExecution() },
        resultCallback = { _, result -> print(result) }
    )

    override fun activeConnection() = HacExecConnectionService.getInstance(project).activeConnection

    override fun icon(): Icon = HybrisIcons.FlexibleSearch.SQL

    companion object {
        @Serial
        private const val serialVersionUID: Long = -112651125533211607L
    }
}