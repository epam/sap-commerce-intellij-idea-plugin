/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.ui

import com.intellij.ui.JBColor
import com.intellij.ui.table.JBTable
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import java.io.Serial
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * A read-only key / value preview of the properties a dialog is about to act on.
 *
 * A table rather than a row per property: keys and values line up in their own columns as they do in the tool window
 * the selection came from, and a table builds no Swing component per row - a selection running to thousands of
 * properties would otherwise freeze the dialog before it appeared.
 */
internal class CxPropertiesPreviewTable(properties: List<CxPropertyPresentation>) : JBTable(PropertiesTableModel(properties)) {

    init {
        setShowGrid(false)
        rowSelectionAllowed = false
        tableHeader.reorderingAllowed = false
        columnModel.getColumn(VALUE_COLUMN).cellRenderer = DefaultTableCellRenderer()
            .apply { foreground = JBColor.GRAY }
    }

    private class PropertiesTableModel(private val properties: List<CxPropertyPresentation>) : AbstractTableModel() {

        override fun getRowCount() = properties.size

        override fun getColumnCount() = 2

        override fun getColumnName(column: Int) = if (column == VALUE_COLUMN) "Value" else "Key"

        override fun getValueAt(row: Int, column: Int) = properties[row]
            .let { if (column == VALUE_COLUMN) it.value.ifBlank { EMPTY_VALUE } else it.key }

        companion object {
            @Serial
            private const val serialVersionUID: Long = 4127730593326417502L
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -6305384093212837751L

        private const val VALUE_COLUMN = 1
        private const val EMPTY_VALUE = "<empty>"
    }
}
