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

class FlexibleSearchSimplifiedTableView(model: ListTableModel<List<String>>) : TableView<List<String>>(model) {

    init {
        autoResizeMode = JTable.AUTO_RESIZE_OFF
        intercellSpacing = Dimension(0, 0)
        autoResizeColumnsByHeader()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -5987741975360042095L

        fun of(content: String ): FlexibleSearchSimplifiedTableView {
            val customCellRenderer = CustomCellRenderer()
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
                        override fun getRenderer(item: List<String>?) = customCellRenderer
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

            return FlexibleSearchSimplifiedTableView(listTableModel).apply {
                autoResizeMode = JTable.AUTO_RESIZE_OFF
                intercellSpacing = Dimension(0, 0)
                autoResizeColumnsByHeader()
            }
        }
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