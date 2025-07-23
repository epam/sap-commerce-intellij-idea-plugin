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
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import kotlinx.coroutines.CoroutineScope
import java.lang.Boolean
import javax.swing.JComponent
import javax.swing.JEditorPane
import kotlin.String
import kotlin.apply
import kotlin.let
import kotlin.takeIf

@Service(Service.Level.PROJECT)
class GroovyInEditorResultsView(project: Project, coroutineScope: CoroutineScope) : InEditorResultsView<GroovySplitEditor, DefaultExecutionResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: GroovySplitEditor, results: Collection<DefaultExecutionResult>): JComponent {
        return results.firstOrNull()
            .takeIf { results.size == 1 }
            ?.let { result ->
                panelView {
                    when {
                        result.hasError -> it.errorView(
                            result.errorMessage ?: "An error was encountered while processing the request.",
                            result.errorDetailMessage
                        )

                        result.result != null || result.output != null -> {
                            if (result.result != null) it.group("Result", result.result)
                            if (result.output != null) it.group("Output", result.output)
                        }

                        else -> it.noResultsView()
                    }
                }
            }
            ?: panelView {
                val resultsWithErrors = results.count { result -> result.hasError }

                if (resultsWithErrors > 0) {
                    it.panel {
                        row {
                            cell(
                                InlineBanner(
                                    """
                                        Groovy script execution resulted to an error on $resultsWithErrors of ${results.size} replicas.<br>
                                        Details of each individual execution result can be found below.
                                        """.trimIndent(),
                                    EditorNotificationPanel.Status.Warning,
                                ).showCloseButton(false)
                            )
                                .align(Align.FILL)
                                .resizableColumn()
                        }.topGap(TopGap.SMALL)
                    }
                        .customize(UnscaledGaps(16, 16, 16, 16))
                }

                results
                    .sortedBy { result -> result.replicaContext?.replicaId }
                    .forEach { result ->
                        it.collapsibleGroup("Replica: ${result.replicaContext?.replicaId ?: ""}") {
                            when {
                                result.hasError -> errorView(
                                    result.errorMessage ?: "An error was encountered while processing the request.",
                                    result.errorDetailMessage
                                )

                                result.result != null || result.output != null -> {
                                    group("Result", result.result)
                                    group("Output", result.output)
                                }

                                else -> noResultsView()
                            }
                        }.expanded = true
                    }
            }
    }

    private fun Panel.group(title: String, text: String?) {
        if (text == null) return

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