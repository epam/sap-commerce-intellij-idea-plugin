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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.table

import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerModel
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.render.RenderingUtil
import com.intellij.ui.table.TableView
import com.intellij.util.asSafely
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.Dimension
import java.io.Serial
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

private const val COLUMN_LOGGER = 1
private const val COLUMN_LEVEL = 0

class LoggersTable : TableView<List<String>> {

    constructor(listTableModel: ListTableModel<List<String>>) : super(listTableModel) {
        autoResizeMode = AUTO_RESIZE_ALL_COLUMNS
        intercellSpacing = Dimension(0, 0)

        val renderer = tableHeader.defaultRenderer
        val rows = listTableModel.items

        //set size for Level Column
        setSize(renderer, columnModel.getColumn(COLUMN_LEVEL).headerValue, COLUMN_LEVEL, -1)

        //set size for Logger Column
        val longestLogger = rows.map { it.last() }
            .withIndex()
            .maxBy { it.value.length }
        setSize(renderer, longestLogger.value, COLUMN_LOGGER, longestLogger.index)
    }

    private fun setSize(
        renderer: TableCellRenderer,
        cellValue: Any,
        columnIndex: Int,
        rowIndex: Int
    ) {
        val column = columnModel.getColumn(columnIndex)
        val component = renderer.getTableCellRendererComponent(this, cellValue, false, false, rowIndex, columnIndex)
        val preferredWidth = component.preferredSize.width + 32
        column.preferredWidth = preferredWidth
        column.minWidth = preferredWidth
        column.maxWidth = preferredWidth
        column.resizable = true
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1210838431719623644L

        fun of(project: Project, loggers: Map<String, CxLoggerModel>, connectionSettings: RemoteConnectionSettings): TableView<List<String>> {
            val customCellRenderer = CustomCellRenderer(project, connectionSettings)
            val listTableModel = ListTableModel<List<String>>(
                LoggerColumnInfo("Effective Level", COLUMN_LEVEL, customCellRenderer),
                LoggerColumnInfo("Logger", COLUMN_LOGGER, customCellRenderer)
            )

            val rows = loggers.values
                .filter { !it.inherited }
                .sortedBy { it.name }
                .map { listOf(it.effectiveLevel, it.name) }
                .toList()
            listTableModel.addRows(rows)

            return LoggersTable(listTableModel)
        }
    }
}

private class CustomCellRenderer(val project: Project, val connectionSettings: RemoteConnectionSettings) : ColoredTableCellRenderer() {

    override fun setToolTipText(text: String?) = Unit

    override fun customizeCellRenderer(table: JTable, value: Any?, selected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
        val stringValue = value?.asSafely<String>() ?: return

        if (column == COLUMN_LOGGER) {
            append(stringValue, SimpleTextAttributes.GRAY_ATTRIBUTES)
            //icon = CxLoggerAccess.getInstance(project).loggers(connectionSettings).get(stringValue).icon
            foreground = RenderingUtil.getForeground(table, selected)
            background = RenderingUtil.getBackground(table, selected)
            alignmentX = RIGHT_ALIGNMENT
        } else {
            append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            foreground = JBColor.lazy { JBUI.CurrentTheme.Table.foreground(selected, hasFocus) }
            background = JBColor(0xFFFFFF, 0x3C3F41)
        }

        border = JBUI.Borders.compound(
            JBUI.Borders.customLine(
                if (hasFocus) JBColor.blue else JBColor.border(),
                if (hasFocus && selected) 1 else 0,
                if (hasFocus && selected) 1 else 0,
                1,
                1
            ),
            JBUI.Borders.empty(3)
        )
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -2610838431719623644L
    }

}

private class LoggerColumnInfo(
    columnName: String,
    private val columnIndex: Int,
    private val customCellRenderer: CustomCellRenderer
) : ColumnInfo<List<String>, Any>(columnName) {

    override fun valueOf(item: List<String>?) = item?.get(columnIndex)
    override fun isCellEditable(item: List<String>?) = false
    override fun getRenderer(item: List<String>?) = customCellRenderer
}