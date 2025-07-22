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

package com.intellij.idea.plugin.hybris.acl.editor

import com.intellij.idea.plugin.hybris.editor.InEditorResultsView
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import java.awt.Dimension
import javax.swing.ScrollPaneConstants

@Service(Service.Level.PROJECT)
class AclInEditorResultsView(project: Project, coroutineScope: CoroutineScope) : InEditorResultsView<AclSplitEditor, DefaultExecutionResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: AclSplitEditor, result: DefaultExecutionResult) = panel {
        when {
            result.hasError -> errorView(result.errorMessage ?: "An error was encountered while processing the request.", result.errorDetailMessage)
            result.output != null -> resultsView(result.output)
            else -> noResultsView()
        }
    }
        .apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
        .let { Dsl.scrollPanel(it, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) }
        .apply {
            minimumSize = Dimension(minimumSize.width, 150)
        }

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
        fun getInstance(project: Project): AclInEditorResultsView = project.service()
    }
}