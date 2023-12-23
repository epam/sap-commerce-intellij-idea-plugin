/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.console.ConsoleRootType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.TimeOption
import com.intellij.idea.plugin.hybris.tools.remote.http.monitorImpexFiles
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import org.apache.batik.ext.swing.GridBagConstants
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.File
import java.io.Serial
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.JPanel

class HybrisImpexMonitorConsole(project: Project) : HybrisConsole(project, HybrisConstants.CONSOLE_TITLE_IMPEX_MONITOR, ImpexLanguage) {

    object MyConsoleRootType : ConsoleRootType("hybris.impex.monitor.shell", null)

    private val panel = JPanel()
    private val timeComboBox = ComboBox(arrayOf(
            TimeOption("in the last 5 minutes", 5, TimeUnit.MINUTES),
            TimeOption("in the last 10 minutes", 10, TimeUnit.MINUTES),
            TimeOption("in the last 15 minutes", 15, TimeUnit.MINUTES),
            TimeOption("in the last 30 minutes", 30, TimeUnit.MINUTES),
            TimeOption("in the last 1 hour", 1, TimeUnit.HOURS)
    ))
    private val workingDirLabel = JBLabel("Hybris Data Folder: ${obtainDataFolder(project)}")
    private val timeOptionLabel = JBLabel("Imported ImpEx")

    init {
        createUI()
        ConsoleHistoryController(MyConsoleRootType, "hybris.impex.monitor.shell", this).install()
    }

    private fun createUI() {
        timeComboBox.renderer = SimpleListCellRenderer.create("...") { it.name }
        isConsoleEditorEnabled = false
        panel.layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.weightx = 0.0
        timeOptionLabel.border = JBUI.Borders.empty(0, 10, 0, 5)
        panel.add(timeOptionLabel)
        panel.add(timeComboBox, constraints)
        constraints.weightx = 1.0
        constraints.fill = GridBagConstants.HORIZONTAL
        workingDirLabel.border = JBUI.Borders.empty(0, 10)
        panel.add(workingDirLabel, constraints)
        add(panel, BorderLayout.NORTH)
        isEditable = true
    }

    private fun obtainDataFolder(project: Project): String {
        val settings = HybrisProjectSettingsComponent.getInstance(project).state
        return FileUtil.toCanonicalPath("${project.basePath}${File.separatorChar}${settings.hybrisDirectory}${File.separatorChar}${HybrisConstants.HYBRIS_DATA_DIRECTORY}")
    }

    private fun timeOption() = (timeComboBox.selectedItem as TimeOption)
    private fun workingDir() = obtainDataFolder(project)
    override fun execute(query: String) = monitorImpexFiles(timeOption().value, timeOption().unit, workingDir())

    override fun title(): String = "ImpEx Monitor"
    override fun tip(): String = "Last imported ImpEx files"
    override fun icon(): Icon = HybrisIcons.MONITORING

    companion object {
        @Serial
        private val serialVersionUID: Long = 4809264328611290133L
    }
}