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
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class HybrisFlexibleSearchConsole(project: Project) : HybrisConsole(project, HybrisConstants.CONSOLE_TITLE_FLEXIBLE_SEARCH, FlexibleSearchLanguage) {

    object MyConsoleRootType : ConsoleRootType("hybris.flexible.search.shell", null)

    private val panel = JPanel(WrappedFlowLayout(0, 0))

    private val commitCheckbox = JBCheckBox("Commit mode")
        .also { it.border = borders10 }
    private val plainSqlCheckbox = JBCheckBox("Plain SQL")
        .also { it.border = borders10 }
    private val maxRowsSpinner = JSpinner(SpinnerNumberModel(10, 1, 100, 1))
        .also { it.border = borders5 }

    init {
        isEditable = true

        panel.add(commitCheckbox)
        panel.add(plainSqlCheckbox)
        panel.add(JBLabel("Rows (max 100):").also { it.border = bordersLabel })
        panel.add(maxRowsSpinner)

        add(panel, BorderLayout.NORTH)

        ConsoleHistoryController(MyConsoleRootType, "hybris.flexible.search.shell", this).install()
    }

    override fun execute(query: String) = HybrisHacHttpClient.getInstance(project)
        .executeFlexibleSearch(
            project,
            commitCheckbox.isSelected,
            plainSqlCheckbox.isSelected,
            maxRowsSpinner.value.toString(),
            query
        )

    override fun title(): String = "FlexibleSearch"
    override fun tip(): String = "FlexibleSearch Console"
    override fun icon(): Icon = HybrisIcons.FlexibleSearch.FILE

    companion object {
        @Serial
        private val serialVersionUID: Long = -112651125533211607L
    }
}