/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import icons.JetgroovyIcons
import org.jetbrains.plugins.groovy.GroovyLanguage
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.border.EmptyBorder

class HybrisGroovyConsole(project: Project) : HybrisConsole(project, HybrisConstants.GROOVY_CONSOLE_TITLE, GroovyLanguage) {

    object MyConsoleRootType : ConsoleRootType("hybris.groovy.shell", null)

    private val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
    private val commitCheckbox = JBCheckBox()
    private val commitLabel = JBLabel("Commit mode: ")

    private val timeoutSpinner = JSpinner(SpinnerNumberModel(
            AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT / 1000, 1, 3600, 10))
    private val timeoutLabel = JBLabel("Timeout (seconds): ")

    init {
        createUI()
        ConsoleHistoryController(MyConsoleRootType, "hybris.groovy.shell", this).install()
    }

    private fun createUI() {
        commitLabel.border = EmptyBorder(5, 10, 5, 3)
        commitCheckbox.border = EmptyBorder(0, 0, 0, 5)
        panel.add(commitLabel)
        panel.add(commitCheckbox)
        add(panel, BorderLayout.NORTH)
        initTimeoutSpinner();
        isEditable = true
    }

    private fun initTimeoutSpinner() {
        commitLabel.border = EmptyBorder(5, 10, 5, 3)
        panel.add(timeoutLabel)
        panel.add(timeoutSpinner)
    }

    override fun execute(query: String): HybrisHttpResult {
        val timeout = Integer.valueOf(timeoutSpinner.value.toString()) * 1000
        return HybrisHacHttpClient.getInstance(project).executeGroovyScript(project, query, commitCheckbox.isSelected, timeout)
    }

    override fun title(): String = "Groovy Scripting"

    override fun tip(): String = "Groovy Console"

    override fun icon(): Icon = JetgroovyIcons.Groovy.Groovy_16x16
}
