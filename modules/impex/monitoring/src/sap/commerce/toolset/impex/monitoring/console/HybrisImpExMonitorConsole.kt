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

package sap.commerce.toolset.impex.monitoring.console

import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.exec.context.ConsoleAwareExecResult
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.impex.monitoring.exec.ImpExMonitorExecClient
import sap.commerce.toolset.impex.monitoring.exec.context.ImpExMonitorExecContext
import sap.commerce.toolset.impex.monitoring.exec.context.TimeOption
import sap.commerce.toolset.project.settings.ProjectSettings
import java.awt.BorderLayout
import java.io.File
import java.io.Serial
import java.util.concurrent.TimeUnit

class HybrisImpExMonitorConsole(
    project: Project,
    coroutineScope: CoroutineScope
) : HybrisConsole<ImpExMonitorExecContext>(project, "[y] Monitor Console", ImpExLanguage, coroutineScope) {

    private lateinit var timeComboBox: ComboBox<TimeOption>

    init {
        isConsoleEditorEnabled = false

        val myPanel = panel {
            row {
                timeComboBox = comboBox(
                    items = listOf(
                        TimeOption("in the last 5 minutes", 5, TimeUnit.MINUTES),
                        TimeOption("in the last 10 minutes", 10, TimeUnit.MINUTES),
                        TimeOption("in the last 15 minutes", 15, TimeUnit.MINUTES),
                        TimeOption("in the last 30 minutes", 30, TimeUnit.MINUTES),
                        TimeOption("in the last 1 hour", 1, TimeUnit.HOURS)
                    ),
                    renderer = SimpleListCellRenderer.create("...") { cell -> cell.name }
                )
                    .label("Show last:")
                    .component

                label("Data folder: ${obtainDataFolder(project)}")
            }
        }

        add(myPanel, BorderLayout.NORTH)
    }

    override fun icon() = HybrisIcons.MONITORING

    @Deprecated("Resolve DATA_DIRECTORY by property")
    private fun obtainDataFolder(project: Project): String {
        val settings = ProjectSettings.getInstance(project)
        // TODO
        return FileUtil.toCanonicalPath("${project.basePath}${File.separatorChar}${settings.hybrisDirectory}${File.separatorChar}${HybrisConstants.HYBRIS_DATA_DIRECTORY}")
    }

    override fun printResult(result: ConsoleAwareExecResult) {
        clear()
        when {
            result.output != null -> ConsoleViewUtil.printAsFileType(this, text, ImpExFileType)
            else -> {
                val timeOption = timeComboBox.selectedItem as TimeOption
                ConsoleViewUtil.printAsFileType(this, "No imported ImpEx files found ${timeOption.name}.", PlainTextFileType.INSTANCE)
            }
        }
    }

    override fun currentExecutionContext(content: String) = ImpExMonitorExecContext(
        timeOption = timeComboBox.selectedItem as TimeOption,
        workingDir = obtainDataFolder(project),
    )

    override fun title() = "ImpEx Monitor"
    override fun tip() = "Last imported ImpEx files"
    override fun execute() = ImpExMonitorExecClient.getInstance(project).execute(
        context = context,
        beforeCallback = { _ -> beforeExecution() },
        resultCallback = { _, result -> print(result) }
    )

    override fun activeConnection(): ExecConnectionSettingsState? = null

    companion object {
        @Serial
        private const val serialVersionUID: Long = -590295893051058799L
    }

}