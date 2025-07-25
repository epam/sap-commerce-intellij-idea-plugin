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
import com.intellij.idea.plugin.hybris.editor.InEditorResultsView
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.idea.plugin.hybris.grid.GridXSVFormatService
import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.idea.plugin.hybris.tools.remote.execution.flexibleSearch.FlexibleSearchExecutionResult
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.render.RenderingUtil
import com.intellij.ui.table.TableView
import com.intellij.util.asSafely
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import kotlinx.coroutines.CoroutineScope
import java.awt.Dimension
import java.io.Serial
import javax.swing.JComponent
import javax.swing.JTable

@Service(Service.Level.PROJECT)
class FlexibleSearchInEditorResultsView(
    project: Project,
    coroutineScope: CoroutineScope
) : InEditorResultsView<FlexibleSearchSplitEditor, FlexibleSearchExecutionResult>(project, coroutineScope) {

    override suspend fun render(fileEditor: FlexibleSearchSplitEditor, results: Collection<FlexibleSearchExecutionResult>): JComponent {
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

    suspend fun resultsView(fileEditor: FlexibleSearchSplitEditor, content: String): JComponent {
        return if (Plugin.GRID.isActive()) csvTableView(fileEditor, content)
        else simpleTableView(fileEditor, content)
    }

    private fun simpleTableView(fileEditor: FlexibleSearchSplitEditor, content: String): JComponent {
        val defaultCellRenderer = object : ColoredTableCellRenderer() {
            @Serial
            private val serialVersionUID: Long = -2610838431719623644L

            override fun setToolTipText(text: String?) = Unit

            override fun customizeCellRenderer(table: JTable, value: Any?, selected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                val stringValue = value?.asSafely<String>() ?: return

                if (column == 0) {
                    append(stringValue, SimpleTextAttributes.GRAY_ATTRIBUTES)
                    foreground = RenderingUtil.getForeground(table, selected)
                    background = RenderingUtil.getBackground(table, selected)
                    alignmentX = RIGHT_ALIGNMENT
                } else {
                    append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    foreground = JBColor.lazy { JBUI.CurrentTheme.Table.foreground(selected, hasFocus) }
                    background = JBColor(0xFFFFFF, 0x3C3F41)
                }

                border = JBUI.Borders.compound(
                    JBUI.Borders.customLine(if (hasFocus) JBColor.blue else JBColor.border(), if (hasFocus && selected) 1 else 0, if (hasFocus && selected) 1 else 0, 1, 1),
                    JBUI.Borders.empty(3)
                )
            }
        }

        val rows = content.trim().split("\n")
        val headerRows = rows.first()
            .split("|")
            .toMutableList()
            .apply { addFirst("") }
        val headers = headerRows
            .mapIndexed { index, columnName ->
                object : ColumnInfo<List<String>, Any>(columnName) {
                    override fun valueOf(item: List<String>?) = item?.getOrNull(index)
                    override fun isCellEditable(item: List<String>?) = index != 0
                    override fun getRenderer(item: List<String>?) = defaultCellRenderer
                }
            }
            .toTypedArray<ColumnInfo<List<String>, Any>>()
        val listTableModel = ListTableModel<List<String>>(*headers)

        val dataRows = rows
            .drop(1)
            .mapIndexed { index, row ->
                row.split("|")
                    .map { it.trim() }
                    .toMutableList()
                    .apply { addFirst("${index + 1}") }
            }

        listTableModel.addRows(dataRows)
        val tableView = TableView(listTableModel).apply {
            autoResizeMode = JTable.AUTO_RESIZE_OFF
            intercellSpacing = Dimension(0, 0)
            autoResizeColumnsByHeader()
        }
        return panel {
            row {
                scrollCell(tableView)
                    .align(Align.FILL)
            }.resizableRow()
        }
    }

    fun JTable.autoResizeColumnsByHeader() {
        val header = tableHeader
        val renderer = header.defaultRenderer

        for (i in 0 until columnCount) {
            val column = columnModel.getColumn(i)
            val headerValue = column.headerValue
            val component = renderer.getTableCellRendererComponent(this, headerValue, false, false, -1, i)
            val preferredWidth = component.preferredSize.width + 32
            column.preferredWidth = preferredWidth
            column.minWidth = preferredWidth
            column.maxWidth = preferredWidth
            column.resizable = i > 0
        }
    }

    private suspend fun csvTableView(fileEditor: FlexibleSearchSplitEditor, content: String): JComponent {
        val lvf = LightVirtualFile(
            fileEditor.file?.name + "_temp.${FlexibleSearchFileType.defaultExtension}.result.csv",
            PlainTextFileType.INSTANCE,
            content
        )

        val format = GridXSVFormatService.getInstance(project).getFormat(FlexibleSearchLanguage)

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
        fun getInstance(project: Project): FlexibleSearchInEditorResultsView = project.service()
    }
}