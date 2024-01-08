/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.http.AbstractHybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import icons.JetgroovyIcons
import org.jetbrains.plugins.groovy.GroovyLanguage
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class HybrisGroovyConsole(project: Project) : HybrisConsole(project, HybrisConstants.CONSOLE_TITLE_GROOVY, GroovyLanguage) {

    private object MyConsoleRootType : ConsoleRootType("hybris.groovy.shell", null)

    private val commitCheckbox = JBCheckBox("Commit mode")
        .also { it.border = borders10 }
    private val timeoutSpinner = JSpinner(SpinnerNumberModel(AbstractHybrisHacHttpClient.DEFAULT_HAC_TIMEOUT / 1000, 1, 3600, 10))
        .also { it.border = borders5 }

    init {
        isEditable = true

        val panel = JPanel(WrappedFlowLayout(0, 0))
        panel.add(commitCheckbox)
        panel.add(JBLabel("Timeout (seconds):").also { it.border = bordersLabel })
        panel.add(timeoutSpinner)

        add(panel, BorderLayout.NORTH)

        ConsoleHistoryController(MyConsoleRootType, "hybris.groovy.shell", this).install()
    }

    override fun execute(query: String) = HybrisHacHttpClient.getInstance(project).executeGroovyScript(
        project, query, commitCheckbox.isSelected,
        timeoutSpinner.value.toString().toInt() * 1000
    )

    override fun title() = "Groovy Scripting"
    override fun tip() = "Groovy Console"
    override fun icon() = JetgroovyIcons.Groovy.Groovy_16x16

    fun updateCommitMode(commitMode: Boolean) {
        commitCheckbox.isSelected = commitMode
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -3858827004057439840L
    }
}
