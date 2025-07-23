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
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionResult
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.LightVirtualFile
import kotlinx.coroutines.CoroutineScope
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
class PolyglotQueryInEditorResultsView(
    project: Project,
    coroutineScope: CoroutineScope
) : InEditorResultsView<PolyglotQuerySplitEditor, FlexibleSearchExecutionResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: PolyglotQuerySplitEditor, results: Collection<FlexibleSearchExecutionResult>): JComponent {
        fileEditor.csvResultsDisposable?.dispose()

        return results.firstOrNull()
            .takeIf { results.size == 1 }
            ?.let { result ->
                when {
                    result.hasError -> panelView {
                        it.errorView(
                            "An error was encountered while processing the Polyglot Query.",
                            result.errorMessage
                        )
                    }

                    result.hasDataRows -> resultsView(fileEditor, result.output!!)
                    else -> panelView { it.noResultsView() }
                }
            }
            ?: multiResultsNotSupportedView()
    }

    suspend fun resultsView(fileEditor: PolyglotQuerySplitEditor, content: String): JComponent {
        val lvf = LightVirtualFile(
            fileEditor.file?.name + "_temp.${PolyglotQueryFileType.defaultExtension}.result.csv",
            PlainTextFileType.INSTANCE,
            content
        )

        val format = GridXSVFormatService.getInstance(project).getFormat(PolyglotQueryLanguage)

        return edtWriteAction {
            val newDisposable = Disposer.newDisposable().apply {
                Disposer.register(fileEditor, this)
                fileEditor.csvResultsDisposable = this
            }

            CsvTableFileEditor(project, lvf, format).apply {
                Disposer.register(newDisposable, this)
            }.component
        }
    }

    companion object {
        fun getInstance(project: Project): PolyglotQueryInEditorResultsView = project.service()
    }
}