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
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class HybrisPolyglotQueryConsole(project: Project) : HybrisConsole(project, HybrisConstants.CONSOLE_TITLE_POLYGLOT_QUERY, PolyglotQueryLanguage) {

    private object MyConsoleRootType : ConsoleRootType(ID, null)

    private val commitCheckbox = JBCheckBox("Commit mode")
        .also { it.border = borders10 }
    private val plainSqlCheckbox = JBCheckBox("Plain SQL")
        .also { it.border = borders10 }
    private val maxRowsSpinner = JSpinner(SpinnerNumberModel(10, 1, 100, 1))
        .also { it.border = borders5 }

    init {
        isEditable = true

        val panel = JPanel(WrappedFlowLayout(0, 0))
        panel.add(commitCheckbox)
        panel.add(plainSqlCheckbox)
        panel.add(JBLabel("Rows (max 100):").also { it.border = bordersLabel })
        panel.add(maxRowsSpinner)

        add(panel, BorderLayout.NORTH)

        ConsoleHistoryController(MyConsoleRootType, ID, this).install()
    }

    override fun execute(query: String) = HybrisHacHttpClient.getInstance(project).executeFlexibleSearch(
        project,
        commitCheckbox.isSelected,
        false,
        maxRowsSpinner.value.toString(),
        query
    )

    override fun title() = "Polyglot Query"
    override fun tip() = "Polyglot Persistence Query Language Console (available only for 1905+)"
    override fun icon() = HybrisIcons.PGQ_FILE

    companion object {
        @Serial
        private val serialVersionUID: Long = -1330953384857131472L
        const val ID = "hybris.polyglot.query.shell"
    }
}
