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

package com.intellij.idea.plugin.hybris.impex.editor

import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.lang.Boolean
import javax.swing.JEditorPane
import javax.swing.ScrollPaneConstants
import kotlin.String
import kotlin.apply
import kotlin.let

@Service(Service.Level.PROJECT)
class ImpExInEditorResultsView {

    fun renderRunningExecution(fileEditor: ImpExSplitEditor) {
        if (fileEditor.inEditorResultsView == null) return

        fileEditor.inEditorResultsView = panel {
            panel {
                row {
                    cell(
                        InlineBanner(
                            "Executing HTTP Call to SAP Commerce...",
                            EditorNotificationPanel.Status.Info
                        )
                            .showCloseButton(false)
                            .setIcon(AnimatedIcon.Default.INSTANCE)
                    )
                        .align(Align.FILL)
                        .resizableColumn()
                }.topGap(TopGap.SMALL)
            }
                .customize(UnscaledGaps(16, 16, 16, 16))
        }.apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
    }

    fun renderExecutionResult(fileEditor: ImpExSplitEditor, result: DefaultExecutionResult) {
        fileEditor.inEditorResultsView = panel {
            when {
                result.hasError -> renderInEditorError(result)
                result.output != null -> renderInEditorResults(result.output)
                else -> renderInEditorNoResults()
            }
        }
            .apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
            .let { Dsl.scrollPanel(it, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) }
            .apply {
                minimumSize = Dimension(minimumSize.width, 150)
            }
    }

    private fun Panel.renderInEditorResults(output: String) {
        panel {
            row {
                cell(
                    InlineBanner(
                        output,
                        EditorNotificationPanel.Status.Success,
                    ).showCloseButton(false)
                )
                    .align(Align.FILL)
                    .resizableColumn()
            }.topGap(TopGap.SMALL)
        }
            .customize(UnscaledGaps(16, 16, 16, 16))
    }

    private fun Panel.renderInEditorNoResults() {
        panel {
            row {
                cell(
                    InlineBanner(
                        "No results found for given query",
                        EditorNotificationPanel.Status.Info,
                    ).showCloseButton(false)
                )
                    .align(Align.FILL)
                    .resizableColumn()
            }.topGap(TopGap.SMALL)
        }
            .customize(UnscaledGaps(16, 16, 16, 16))
    }

    private fun Panel.renderInEditorError(result: DefaultExecutionResult) {
        panel {
            row {
                cell(
                    InlineBanner(
                        result.errorMessage ?: "An error was encountered while processing the ImpEx .",
                        EditorNotificationPanel.Status.Error,
                    ).showCloseButton(false)
                )
                    .align(Align.FILL)
                    .resizableColumn()
            }.topGap(TopGap.SMALL)
        }
            .customize(UnscaledGaps(16, 16, 16, 16))

        panel {
            group("Response Details") {
                row {
                    cell(
                        JEditorPane().apply {
                            text = result.detailMessage
                            isEditable = false
                            isOpaque = false
                            background = null
                            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE)
                        }
                    )
                        .align(Align.FILL)
                        .resizableColumn()
                }
            }.topGap(TopGap.SMALL)
        }
    }

    companion object {
        fun getInstance(project: Project): ImpExInEditorResultsView = project.service()
    }
}