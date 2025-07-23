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
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class ImpExInEditorResultsView(project: Project, coroutineScope: CoroutineScope) : InEditorResultsView<ImpExSplitEditor, DefaultExecutionResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: ImpExSplitEditor, results: Collection<DefaultExecutionResult>): JComponent = results.firstOrNull()
        .takeIf { results.size == 1 }
        ?.let { result ->
            panelView {
                when {
                    result.hasError -> it.errorView(
                        result.errorMessage ?: "An error was encountered while processing the request.",
                        result.errorDetailMessage
                    )

                    result.output != null -> it.resultsView(result.output)
                    else -> it.noResultsView()
                }
            }
        }
        ?: multiResultsNotSupportedView()

    private fun Panel.resultsView(output: String) {
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

    companion object {
        fun getInstance(project: Project): ImpExInEditorResultsView = project.service()
    }
}