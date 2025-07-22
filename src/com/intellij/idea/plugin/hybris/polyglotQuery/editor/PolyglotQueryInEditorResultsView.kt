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

package com.intellij.idea.plugin.hybris.polyglotQuery.editor

import com.intellij.database.editor.CsvTableFileEditor
import com.intellij.idea.plugin.hybris.editor.InEditorResultsView
import com.intellij.idea.plugin.hybris.grid.GridXSVFormatService
import com.intellij.idea.plugin.hybris.polyglotQuery.PolyglotQueryLanguage
import com.intellij.idea.plugin.hybris.polyglotQuery.file.PolyglotQueryFileType
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.ui.Dsl
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.ScrollPaneConstants

@Service(Service.Level.PROJECT)
class PolyglotQueryInEditorResultsView(
    project: Project,
    coroutineScope: CoroutineScope
) : InEditorResultsView<PolyglotQuerySplitEditor, DefaultExecutionResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: PolyglotQuerySplitEditor, result: DefaultExecutionResult) = when {
        result.hasError -> panelView { it.errorView("An error was encountered while processing the Polyglot Query.", result.errorMessage) }
        result.output?.trim()?.contains("\n") ?: false -> resultsView(fileEditor, result.output)
        else -> panelView { it.noResultsView() }
    }

    suspend fun resultsView(fileEditor: PolyglotQuerySplitEditor, content: String): JComponent {
        val lvf = LightVirtualFile(
            fileEditor.file?.name + "_temp.${PolyglotQueryFileType.defaultExtension}.result.csv",
            PlainTextFileType.INSTANCE,
            content
        )

        val format = GridXSVFormatService.getInstance(project).getFormat(PolyglotQueryLanguage)

        return edtWriteAction {
            CsvTableFileEditor(project, lvf, format).component
        }
    }

    private fun panelView(panelProvider: (Panel) -> Unit) = panel {
        panelProvider.invoke(this)
    }
        .apply { border = JBUI.Borders.empty(5, 16, 10, 16) }
        .let { Dsl.scrollPanel(it, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) }
        .apply {
            minimumSize = Dimension(minimumSize.width, 150)
        }

    companion object {
        fun getInstance(project: Project): PolyglotQueryInEditorResultsView = project.service()
    }
}