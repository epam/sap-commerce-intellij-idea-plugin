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

package com.intellij.idea.plugin.hybris.tools.remote.console.impl

import com.intellij.diff.util.DiffUtil.isEditable
import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.console.ConsoleHistoryController.addToHistory
import com.intellij.execution.console.ConsoleRootType
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.TimeOption
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHttpResult
import com.intellij.idea.plugin.hybris.tools.remote.http.groovy.ReplicaContext
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.ImpExMonitorExecutionContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.io.Serial
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

@Service(Service.Level.PROJECT)
class HybrisImpexMonitorConsole(project: Project) : HybrisConsole<ImpExMonitorExecutionContext>(
    project,
    HybrisConstants.CONSOLE_TITLE_IMPEX_MONITOR,
    ImpexLanguage
) {

    private object MyConsoleRootType : ConsoleRootType("hybris.impex.monitor.shell", null)

    private val timeComboBox = ComboBox(
        arrayOf(
            TimeOption("in the last 5 minutes", 5, TimeUnit.MINUTES),
            TimeOption("in the last 10 minutes", 10, TimeUnit.MINUTES),
            TimeOption("in the last 15 minutes", 15, TimeUnit.MINUTES),
            TimeOption("in the last 30 minutes", 30, TimeUnit.MINUTES),
            TimeOption("in the last 1 hour", 1, TimeUnit.HOURS)
        )
    )
        .also { it.renderer = SimpleListCellRenderer.create("...") { cell -> cell.name } }
    private val workingDirLabel = JBLabel("Data folder: ${obtainDataFolder(project)}")
        .also { it.border = bordersLabel }

    init {
        isEditable = true
        isConsoleEditorEnabled = false

        val panel = JPanel()
            .also { it.layout = GridBagLayout() }

        val constraints = GridBagConstraints()
        constraints.weightx = 0.0

        panel.add(JBLabel("Imported ImpEx:").also { it.border = bordersLabel })
        panel.add(timeComboBox, constraints)

        constraints.weightx = 1.0
        constraints.fill = GridBagConstraints.HORIZONTAL

        panel.add(workingDirLabel, constraints)

        add(panel, BorderLayout.NORTH)

        ConsoleHistoryController(MyConsoleRootType, "hybris.impex.monitor.shell", this).install()
    }

    private fun obtainDataFolder(project: Project): String {
        val settings = ProjectSettingsComponent.getInstance(project).state
        return FileUtil.toCanonicalPath("${project.basePath}${File.separatorChar}${settings.hybrisDirectory}${File.separatorChar}${HybrisConstants.HYBRIS_DATA_DIRECTORY}")
    }

    private fun timeOption() = (timeComboBox.selectedItem as TimeOption)
    private fun workingDir() = obtainDataFolder(project)
    override fun execute(context: ImpExMonitorExecutionContext) {
        TODO("Not yet implemented")
    }

    override fun printResults(httpResult: HybrisHttpResult, replicaContext: ReplicaContext?) {
        clear()
        ConsoleViewUtil.printAsFileType(this, httpResult.output, ImpexFileType)
    }

    override fun getQuery(): String? {
        val document = currentEditor.document
        val range = TextRange(0, document.textLength)
        currentEditor.selectionModel.setSelection(range.startOffset, range.endOffset)
        addToHistory(range, consoleEditor, false)
        printDefaultText()
        return null
    }

    override fun execute(query: String, replicaContext: ReplicaContext?) = monitorImpexFiles(timeOption().value, timeOption().unit, workingDir())

    override fun title() = "ImpEx Monitor"
    override fun tip() = "Last imported ImpEx files"
    override fun icon() = HybrisIcons.MONITORING

    private fun monitorImpexFiles(value: Int, unit: TimeUnit, pathToData: String): HybrisHttpResult {
        val resultBuilder = HybrisHttpResult.HybrisHttpResultBuilder.createResult()
        val minutesAgo = LocalDateTime.now().minusMinutes(unit.toMinutes(value.toLong()))
        val out = StringBuilder()
        File(pathToData).walk()
            .filter { file -> file.extension == "bin" }
            .filter { file -> file.lastModified().toLocalDateTime().isAfter(minutesAgo) }
            .sortedBy { it.lastModified() }
            .forEach {
                val header = "# File Path:  ${it.path}\n# file modified: ${it.lastModified().toLocalDateTime()}"
                out.append("\n#" + "-".repeat(header.length - 1) + "\n")
                out.append(header)
                out.append("\n#" + "-".repeat(header.length - 1) + "\n")
                out.append("\n${it.readText()}\n")
            }

        return resultBuilder.httpCode(200)
            .output(out.toString())
            .build()
    }

    private fun Long.toLocalDateTime() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()


    companion object {
        @Serial
        private val serialVersionUID: Long = 4809264328611290133L
    }
}