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

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.TransactionMode
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.QueryMode
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.sql.psi.SqlLanguage
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import kotlinx.coroutines.CoroutineScope
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

@Service(Service.Level.PROJECT)
class HybrisSQLConsole(project: Project, coroutineScope: CoroutineScope) : HybrisConsole<FlexibleSearchExecutionContext>(
    project,
    HybrisConstants.CONSOLE_TITLE_SQL,
    if (Plugin.DATABASE.isActive()) SqlLanguage.INSTANCE else PlainTextLanguage.INSTANCE,
    coroutineScope
) {

    private val panel = JPanel(WrappedFlowLayout(0, 0))

    private val commitCheckbox = JBCheckBox("Commit mode")
        .also { it.border = borders10 }
    private val maxRowsSpinner = JSpinner(SpinnerNumberModel(200, 1, Integer.MAX_VALUE, 1))
        .also { it.border = borders5 }

    init {
        isEditable = true

        panel.add(commitCheckbox)
        panel.add(JBLabel("Rows:").also { it.border = bordersLabel })
        panel.add(maxRowsSpinner)

        add(panel, BorderLayout.NORTH)
    }

    override fun currentExecutionContext(content: String) = FlexibleSearchExecutionContext(
        content = content,
        maxCount = maxRowsSpinner.value.toString().toInt(),
        transactionMode = if (commitCheckbox.isSelected) TransactionMode.COMMIT else TransactionMode.ROLLBACK,
        queryMode = QueryMode.SQL
    )

    override fun title(): String = "SQL"
    override fun tip(): String = "SQL Console"
    override fun icon(): Icon? = HybrisIcons.FlexibleSearch.SQL

    companion object {
        @Serial
        private val serialVersionUID: Long = -112651125533211607L

        fun getInstance(project: Project): HybrisSQLConsole = project.service()
    }
}