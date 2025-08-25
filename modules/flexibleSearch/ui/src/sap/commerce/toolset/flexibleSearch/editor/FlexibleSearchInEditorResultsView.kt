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

package sap.commerce.toolset.flexibleSearch.editor

import com.intellij.database.editor.CsvTableFileEditor
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult
import sap.commerce.toolset.flexibleSearch.file.FlexibleSearchFileType
import sap.commerce.toolset.flexibleSearch.xsvFlexibleSearchFormat
import sap.commerce.toolset.ui.editor.InEditorResultsView
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class FlexibleSearchInEditorResultsView(
    project: Project,
    coroutineScope: CoroutineScope
) : InEditorResultsView<FlexibleSearchSplitEditor, FlexibleSearchExecResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: FlexibleSearchSplitEditor, results: Collection<FlexibleSearchExecResult>): JComponent {
        fileEditor.csvResultsDisposable?.dispose()

        return results.firstOrNull()
            .takeIf { results.size == 1 }
            ?.let { result ->
                when {
                    result.hasError -> panelView {
                        it.errorView(
                            "An error was encountered while processing the FlexibleSearch query.",
                            result.errorMessage
                        )
                    }

                    result.hasDataRows -> resultsView(fileEditor, result.output!!)
                    else -> panelView { it.noResultsView() }
                }
            }
            ?: multiResultsNotSupportedView()
    }

    suspend fun resultsView(fileEditor: FlexibleSearchSplitEditor, content: String) = if (Plugin.GRID.isActive()) csvTableView(fileEditor, content)
    else simpleTableView(content)

    private fun simpleTableView(content: String): JComponent = panel {
        row {
            scrollCell(FlexibleSearchSimplifiedTableView.of(content))
                .align(Align.FILL)
        }.resizableRow()
    }

    private suspend fun csvTableView(fileEditor: FlexibleSearchSplitEditor, content: String): JComponent {
        val lvf = LightVirtualFile(
            fileEditor.file?.name + "_temp.${FlexibleSearchFileType.defaultExtension}.result.csv",
            PlainTextFileType.INSTANCE,
            content
        )


        return edtWriteAction {
            val newDisposable = Disposer.newDisposable().apply {
                Disposer.register(fileEditor, this)
                fileEditor.csvResultsDisposable = this
            }

            CsvTableFileEditor(project, lvf, xsvFlexibleSearchFormat()).apply {
                Disposer.register(newDisposable, this)
            }.component
        }
    }

    companion object {
        fun getInstance(project: Project): FlexibleSearchInEditorResultsView = project.service()
    }
}