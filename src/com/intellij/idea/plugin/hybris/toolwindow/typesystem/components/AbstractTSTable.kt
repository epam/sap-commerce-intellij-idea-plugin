/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.components

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
import javax.swing.JTable
import javax.swing.table.TableColumn

abstract class AbstractTSTable<Owner : Any, Item> : JBTable() {

    private lateinit var myProject: Project
    lateinit var myOwner: Owner

    init {
        intercellSpacing = Dimension(0, 0)
    }

    fun init(project: Project, owner: Owner) {
        myProject = project
        myOwner = owner

        val search = TableSpeedSearch(this)

        setShowGrid(false)
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer());

        model = createModel()

        getSearchableColumnNames()
            .map { getColumn(it) }
            .forEach { it.cellRenderer = Renderer(search)}

        getFixedWidthColumnNames()
            .forEach { setFixedColumnWidth(getColumn(it), this, it) }
    }

    protected abstract fun createModel(): ListTableModel<Item>
    protected open fun getSearchableColumnNames(): List<String> = emptyList()
    protected open fun getFixedWidthColumnNames(): List<String> = emptyList()

    protected fun createColumn(
        name: String,
        valueProvider: (Item) -> Any,
        columnClass: Class<*> = String::class.java,
        tooltip: String? = null
    ) = object : ColumnInfo<Item, Any>(name) {
        override fun valueOf(item: Item) = valueProvider.invoke(item)
        override fun isCellEditable(item: Item) = false
        override fun getColumnClass(): Class<*> = columnClass
        override fun getTooltipText(): String? = tooltip
//            override fun setValue(item: TSMetaAttribute, value: String) = property.set(item, value)
    }

    private fun setFixedColumnWidth(column: TableColumn, table : JTable, text: String) = with(column) {
        val width = table
            .getFontMetrics(table.font)
            .stringWidth(" $text ") + JBUIScale.scale(4)

        preferredWidth = width
        minWidth = width
        maxWidth = width
        resizable = false

        this
    }

    private class Renderer(private val search: TableSpeedSearch) : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(table: JTable, value: Any?, selected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
            val text = value as? String ?: return
            SearchUtil.appendFragments(search.enteredPrefix, text, SimpleTextAttributes.STYLE_PLAIN, null, null, this)
        }

        companion object {
            private const val serialVersionUID: Long = -6158496130634687619L
        }
    }

    companion object {
        private const val serialVersionUID: Long = -8940844594498853578L
    }
}