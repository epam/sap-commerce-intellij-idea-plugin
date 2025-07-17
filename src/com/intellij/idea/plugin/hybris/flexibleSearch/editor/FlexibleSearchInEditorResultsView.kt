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

package com.intellij.idea.plugin.hybris.flexibleSearch.editor

import com.intellij.database.editor.CsvTableFileEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.grid.GridXSVFormatService
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Dimension
import javax.swing.JEditorPane
import javax.swing.ScrollPaneConstants

object FlexibleSearchInEditorResultsView {

    fun renderRunningExecution(fileEditor: FlexibleSearchSplitEditor) {
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

    fun renderExecutionResult(project: Project, fileEditor: FlexibleSearchSplitEditor, result: DefaultExecutionResult) {
        if (result.hasError) {
            fileEditor.inEditorResultsView = renderInEditorError(result)
        } else {
            renderInEditorResults(project, fileEditor, result)
        }
    }

    private fun renderInEditorResults(project: Project, fileEditor: FlexibleSearchSplitEditor, result: DefaultExecutionResult) {
        CoroutineScope(Dispatchers.Default).launch {
            if (project.isDisposed) return@launch

            val lvf = LightVirtualFile(
                fileEditor.file?.name + ".fxs.result.csv",
                PlainTextFileType.INSTANCE,
                result.output
            )

            val format = project.service<GridXSVFormatService>().getFormat(FlexibleSearchLanguage)

            edtWriteAction {
                val editor = CsvTableFileEditor(project, lvf, format);
                fileEditor.inEditorResultsView = editor.component
            }
        }
    }

    private fun renderInEditorError(result: DefaultExecutionResult) = panel {
        panel {
            row {
                cell(
                    InlineBanner(
                        "An error was encountered while processing the FlexibleSearch query.",
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
                            text = result.errorMessage
                            isEditable = false
                            isOpaque = false
                            background = null
                            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, java.lang.Boolean.TRUE)
                        }
                    )
                        .align(Align.FILL)
                        .resizableColumn()
                }
            }.topGap(TopGap.SMALL)
        }
    }
        .apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
        .let { Dsl.scrollPanel(it, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) }
        .apply {
            minimumSize = Dimension(minimumSize.width, 150)
        }
}