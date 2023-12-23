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
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.persistence.ui.HybrisConsoleQueryPanel
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.border.EmptyBorder

class HybrisPolyglotQueryConsole(project: Project) : HybrisConsole(project, HybrisConstants.CONSOLE_TITLE_POLYGLOT_QUERY, PolyglotQueryLanguage.instance) {

    object MyConsoleRootType : ConsoleRootType(ID, null)

    private val panel = JPanel(WrappedFlowLayout(0, 0))

    private val commitCheckbox = JBCheckBox()
    private val commitLabel = JBLabel("Commit mode: ")

    private val maxRowsSpinner = JSpinner(SpinnerNumberModel(10, 1, 100, 1))
    private val maxRowsLabel = JBLabel("Rows (max 100): ")

    private val labelInsets = JBUI.insets(0, 10, 0, 1)

    private val queryConsolePanel = HybrisConsoleQueryPanel(project, this, "POLYGLOT_QUERY")

    init {
        createUI()
        ConsoleHistoryController(MyConsoleRootType, ID, this).install()
    }

    private fun createUI() {
        initCommitElements()
        initMaxRowsElements()

        panel.add(queryConsolePanel)
        add(panel, BorderLayout.NORTH)
        isEditable = true
    }

    private fun initCommitElements() {
        commitLabel.border = EmptyBorder(labelInsets)
        panel.add(commitLabel)
        panel.add(commitCheckbox)
    }

    private fun initMaxRowsElements() {
        maxRowsLabel.border = EmptyBorder(labelInsets)
        panel.add(maxRowsLabel)
        panel.add(maxRowsSpinner)
    }

    override fun execute(query: String): HybrisHttpResult {
        return HybrisHacHttpClient.getInstance(project)
            .executeFlexibleSearch(
                project,
                commitCheckbox.isSelected,
                false,
                maxRowsSpinner.value.toString(),
                query
            )
    }

    override fun title(): String = "Polyglot Query"
    override fun tip(): String = "Polyglot Persistence Query Language Console (available only for 1905+)"
    override fun icon(): Icon = HybrisIcons.PGQ_FILE

    companion object {
        @Serial
        private val serialVersionUID: Long = -1330953384857131472L
        const val ID = "hybris.polyglot.query.shell"
    }
}
