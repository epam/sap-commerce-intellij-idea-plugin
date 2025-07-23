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

package com.intellij.idea.plugin.hybris.groovy.editor

import com.intellij.idea.plugin.hybris.editor.InEditorResultsView
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import java.awt.Dimension
import java.lang.Boolean
import javax.swing.JEditorPane
import javax.swing.ScrollPaneConstants
import kotlin.String
import kotlin.apply
import kotlin.let

@Service(Service.Level.PROJECT)
class GroovyInEditorResultsView(project: Project, coroutineScope: CoroutineScope) : InEditorResultsView<GroovySplitEditor, DefaultExecutionResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: GroovySplitEditor, result: DefaultExecutionResult) = panel {
        when {
            result.hasError -> errorView(result.errorMessage ?: "An error was encountered while processing the request.", result.errorDetailMessage)
            result.result != null || result.output != null -> {
                if (result.result != null) group("Result", result.result)
                if (result.output != null) group("Output", result.output)
            }

            else -> noResultsView()
        }
    }
        .apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
        .let { Dsl.scrollPanel(it, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) }
        .apply {
            minimumSize = Dimension(minimumSize.width, 150)
        }

    private fun Panel.group(title: String, text: String) {
        collapsibleGroup(title) {
            row {
                cell(
                    JEditorPane().apply {
                        this.text = text
                        isEditable = false
                        isOpaque = false
                        background = null
                        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE)
                    }
                )
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }.expanded = true
    }

    companion object {
        fun getInstance(project: Project): GroovyInEditorResultsView = project.service()
    }
}