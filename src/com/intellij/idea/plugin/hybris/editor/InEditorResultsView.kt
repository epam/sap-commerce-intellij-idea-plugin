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

package com.intellij.idea.plugin.hybris.editor

import com.intellij.idea.plugin.hybris.tools.remote.execution.ExecutionResult
import com.intellij.openapi.fileEditor.FileEditor
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Boolean
import javax.swing.JComponent
import javax.swing.JEditorPane
import kotlin.String
import kotlin.Unit
import kotlin.apply

abstract class InEditorResultsView<E : FileEditor, R : ExecutionResult>(protected val project: Project, private val coroutineScope: CoroutineScope) {

    fun executingView(message: String = "Executing HTTP Call to SAP Commerce...") = panel {
        panel {
            row {
                cell(
                    InlineBanner(
                        message,
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

    fun resultView(fileEditor: E, result: R, applyView: (CoroutineScope, JComponent) -> Unit) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            val view = render(fileEditor, result)

            applyView(this, view)
        }
    }

    protected abstract suspend fun render(fileEditor: E, result: R): JComponent

    protected fun Panel.noResultsView() {
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

    protected fun Panel.errorView(errorMessage: String, errorDetailMessage: String?) {
        panel {
            row {
                cell(
                    InlineBanner(
                        errorMessage,
                        EditorNotificationPanel.Status.Error,
                    ).showCloseButton(false)
                )
                    .align(Align.FILL)
                    .resizableColumn()
            }.topGap(TopGap.SMALL)
        }
            .customize(UnscaledGaps(16, 16, 16, 16))

        if (errorDetailMessage != null) {
            panel {
                group("Response Details") {
                    row {
                        cell(
                            JEditorPane().apply {
                                text = errorDetailMessage
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
    }
}