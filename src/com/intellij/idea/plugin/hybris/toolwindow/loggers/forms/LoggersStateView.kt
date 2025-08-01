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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.forms

import com.intellij.idea.plugin.hybris.tools.logging.CxLoggerModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
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
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.io.Serial
import javax.swing.JComponent
import javax.swing.JTable

@Service(Service.Level.PROJECT)
class LoggersStateView(
    val project: Project,
    val coroutineScope: CoroutineScope
) {

    fun renderView(loggers: Map<String, CxLoggerModel>, applyView: (CoroutineScope, JComponent) -> Unit) {
        coroutineScope.launch {
            if (project.isDisposed) return@launch

            val view = render(loggers)

            applyView(this, view)
        }
    }

    fun render(loggers: Map<String, CxLoggerModel>) = panel {
        row {
            scrollCell(table(loggers))
                .align(Align.FILL)
        }.resizableRow()
    }


    fun table(loggers: Map<String, CxLoggerModel>): TableView<List<String>> {
        val customCellRenderer = CustomCellRenderer()
        val rows = loggers.values
            .sortedBy { it.name }
            .map { listOf(it.name, it.effectiveLevel) }
            .toMutableList()

        val loggerNameHeader = object : ColumnInfo<List<String>, Any>("Logger") {
            override fun valueOf(item: List<String>?) = item?.firstOrNull()
            override fun isCellEditable(item: List<String>?) = false
            override fun getRenderer(item: List<String>?) = customCellRenderer
        }

        val levelHeader = object : ColumnInfo<List<String>, Any>("Effective Level") {
            override fun valueOf(item: List<String>?) = item?.getOrNull(1)
            override fun isCellEditable(item: List<String>?) = false
            override fun getRenderer(item: List<String>?) = customCellRenderer
        }

        val headers = arrayOf(loggerNameHeader, levelHeader)

        val listTableModel = ListTableModel<List<String>>(*headers)

        listTableModel.addRows(rows)
        val longestString = rows.map { it.first() }
            .withIndex()
            .maxBy { it.value.length }
        val longestStringRowNumber = longestString.index
        val longestStringValue = longestString.value

        return TableView(listTableModel).apply {
            autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
            intercellSpacing = Dimension(0, 0)

            val header = tableHeader
            val renderer = header.defaultRenderer

            //Logger Name Column
            val column = columnModel.getColumn(0)
            val headerValue = column.headerValue
            val component = renderer.getTableCellRendererComponent(this, longestStringValue, false, false, longestStringRowNumber, 0)
            val preferredWidth = component.preferredSize.width + 32
            column.preferredWidth = preferredWidth
            column.minWidth = preferredWidth
            column.maxWidth = preferredWidth
            column.resizable = true

            //Effective Level Column
            val column2 = columnModel.getColumn(1)
            val headerValue2 = column2.headerValue
            val component2 = renderer.getTableCellRendererComponent(this, headerValue2, false, false, -1, 1)
            val preferredWidth2 = component2.preferredSize.width + 32
            column2.preferredWidth = preferredWidth2
            column2.minWidth = preferredWidth2
            column2.maxWidth = preferredWidth2
            column2.resizable = false
        }
    }

    companion object {
        fun getInstance(project: Project): LoggersStateView = project.service()
    }

}

private class CustomCellRenderer : ColoredTableCellRenderer() {
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