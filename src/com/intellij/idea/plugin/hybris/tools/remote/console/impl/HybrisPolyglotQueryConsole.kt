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

import com.intellij.execution.ui.ConsoleViewContentType.*
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.polyglotQuery.editor.PolyglotQueryVirtualParameter
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionContext
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.QueryMode
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import kotlinx.coroutines.CoroutineScope
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

@Service(Service.Level.PROJECT)
class HybrisPolyglotQueryConsole(project: Project, coroutineScope: CoroutineScope) : HybrisConsole<FlexibleSearchExecutionContext>(
    project,
    HybrisConstants.CONSOLE_TITLE_POLYGLOT_QUERY,
    PolyglotQueryLanguage,
    coroutineScope
) {

    private val maxRowsSpinner = JSpinner(SpinnerNumberModel(200, 1, Integer.MAX_VALUE, 1))
        .also { it.border = borders5 }

    init {
        isEditable = true

        val panel = JPanel(WrappedFlowLayout(0, 0))
        panel.add(JBLabel("Rows:").also { it.border = bordersLabel })
        panel.add(maxRowsSpinner)

        add(panel, BorderLayout.NORTH)
    }

    override fun currentExecutionContext(content: String) = FlexibleSearchExecutionContext(
        content = content,
        maxCount = maxRowsSpinner.value.toString().toInt(),
        queryMode = QueryMode.PolyglotQuery
    )

    fun print(values: Collection<PolyglotQueryVirtualParameter>?) {
        if (values == null) return

        print(" Parameters:\n", SYSTEM_OUTPUT)

        values.forEachIndexed { index, param ->
            print("  | ", LOG_VERBOSE_OUTPUT)
            print(param.name, NORMAL_OUTPUT)
            print(" : ", LOG_VERBOSE_OUTPUT)
            print(param.presentationValue, USER_INPUT)

            if (index < values.size) print("\n", SYSTEM_OUTPUT)
        }
    }

    override fun title() = "Polyglot Query"
    override fun tip() = "Polyglot Persistence Query Language Console (available only for 1905+)"

    companion object {
        @Serial
        private val serialVersionUID: Long = -1330953384857131472L

        fun getInstance(project: Project): HybrisPolyglotQueryConsole = project.service()
    }
}
