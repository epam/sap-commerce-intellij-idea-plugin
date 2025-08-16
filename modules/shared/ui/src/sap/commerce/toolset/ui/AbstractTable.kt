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

package sap.commerce.toolset.ui

import com.intellij.ide.ui.search.SearchUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.Dimension
import java.awt.Rectangle
import java.io.Serial
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableColumn

@Suppress("UNCHECKED_CAST")
abstract class AbstractTable<Owner : Any, Item>(val myProject: Project) : JBTable() {

    fun init() {
        val search = TableSpeedSearch.installOn(this)

        intercellSpacing = Dimension(0, 0)
        model = createModel()

        setShowGrid(false)
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer())

        getSearchableColumnNames()
            .map { getColumn(it) }
            .forEach { it.cellRenderer = Renderer(search) }

        getFixedWidthColumnNames()
            .forEach { setFixedColumnWidth(getColumn(it), this, it) }
    }

    fun updateModel(owner: Owner) {
        val listTableModel = model as? ListTableModel<Item> ?: return
        listTableModel.items = getItems(owner)

        getAutoWidthColumnNames()
            .forEach { setAutoWidthForColumn(getColumn(it), this, it) }
    }

    fun getItems(): List<Item> = (model as? ListTableModel<Item>)?.items
        ?: emptyList()

    fun getItem(rowIndex: Int): Item? = try {
        (model as? ListTableModel<Item>)?.getItem(rowIndex)
    } catch (x: IndexOutOfBoundsException) {
        null
    }

    fun getCurrentItem(): Item? = getItem(selectedRow)

    fun getCastedModel() = model as? ListTableModel<Item>

    abstract fun select(item: Item)
    protected abstract fun getItems(owner: Owner): MutableList<Item>
    protected abstract fun createModel(): ListTableModel<Item>
    protected open fun getSearchableColumnNames(): List<String> = emptyList()
    protected open fun getFixedWidthColumnNames(): List<String> = emptyList()
    protected open fun getAutoWidthColumnNames(): List<String> = emptyList()

    protected fun selectRowWithValue(value: String?, columnName: String) {
        var row = 0
        val column = getColumn(columnName).modelIndex
        do {
            if (value == getValueAt(row, column)) {
                setRowSelectionInterval(row, row)
                scrollRectToVisible(Rectangle(getCellRect(row, 0, true)))
                break
            }
            row++
        } while (row != rowCount)
    }

    protected fun createColumn(
        name: String,
        valueProvider: (Item) -> Any?,
        columnClass: Class<*> = String::class.java,
        tooltip: String? = null,
        isCellEditable: Boolean = false,
        valueSetter: ((Item, Any?) -> Unit)? = null,
    ) = object : ColumnInfo<Item, Any>(name) {
        override fun valueOf(item: Item) = valueProvider.invoke(item)
        override fun isCellEditable(item: Item) = isCellEditable
        override fun getColumnClass(): Class<*> = columnClass
        override fun getTooltipText(): String? = tooltip
        override fun setValue(item: Item, value: Any?) {
            valueSetter?.invoke(item, value)
        }
    }

    private fun setFixedColumnWidth(column: TableColumn, table: JTable, text: String) = with(column) {
        val newWidth = table
            .getFontMetrics(table.font)
            .stringWidth("$text      ")
            .let { JBUIScale.scale(it) }

        val centerHeaderRenderer = DefaultTableCellRenderer()
        centerHeaderRenderer.setHorizontalAlignment(SwingConstants.CENTER)
        headerRenderer = centerHeaderRenderer
        preferredWidth = newWidth
        minWidth = newWidth
        maxWidth = newWidth
        resizable = false

        this
    }

    private fun setAutoWidthForColumn(column: TableColumn, table: JTable, text: String) = with(column) {
        var newWidth = table
            .getFontMetrics(table.font)
            .stringWidth("$text      ")
            .let { JBUIScale.scale(it) }

        (0 until table.rowCount).forEach {
            val cellWidth = table.prepareRenderer(
                table.getCellRenderer(it, column.modelIndex),
                it, column.modelIndex
            )
                .preferredSize
                .width
                .let { width -> JBUIScale.scale(width) }

            if (cellWidth > newWidth) {
                newWidth = cellWidth
            }
        }
        preferredWidth = newWidth
        minWidth = newWidth
        maxWidth = newWidth

        this
    }

    private class Renderer(private val search: TableSpeedSearch) : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(table: JTable, value: Any?, selected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
            val text = value as? String ?: return
            SearchUtil.appendFragments(search.enteredPrefix, text, SimpleTextAttributes.STYLE_PLAIN, null, null, this)
        }

        companion object {
            @Serial
            private val serialVersionUID: Long = -6158496130634687619L
        }
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -8940844594498853578L
    }
}