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
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAttribute
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
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

private const val ATTRIBUTES_COLUMN_REDECLARE = "R"
private const val ATTRIBUTES_COLUMN_AUTO_CREATE = "A"
private const val ATTRIBUTES_COLUMN_GENERATE = "G"
private const val ATTRIBUTES_COLUMN_TYPE = "Type"
private const val ATTRIBUTES_COLUMN_DEFAULT_VALUE = "Default value"
private const val ATTRIBUTES_COLUMN_DESCRIPTION = "Description"
private const val ATTRIBUTES_COLUMN_QUALIFIER = "Qualifier"

class TSMetaItemAttributesTable(private val myProject: Project, private val myMeta: TSMetaItem) : JBTable() {

    init {
        val search = TableSpeedSearch(this)

        setShowGrid(false)
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer());

        intercellSpacing = Dimension(0, 0)
        model = createModel()

        getColumn(ATTRIBUTES_COLUMN_QUALIFIER).cellRenderer = Renderer(search)
        getColumn(ATTRIBUTES_COLUMN_DESCRIPTION).cellRenderer = Renderer(search)

        arrayOf(ATTRIBUTES_COLUMN_REDECLARE, ATTRIBUTES_COLUMN_AUTO_CREATE, ATTRIBUTES_COLUMN_GENERATE)
            .forEach { setFixedColumnWidth(getColumn(it), this, it)}
    }

    private fun createModel(): ListTableModel<TSMetaAttribute> = with(ListTableModel<TSMetaAttribute>()) {
        items = myMeta.getAttributes(true)
            .sortedBy { it.name }

        columnInfos = arrayOf(
            createColumn(
                name = ATTRIBUTES_COLUMN_REDECLARE,
                valueProvider = { attr -> attr.retrieveDom()?.redeclare?.value ?: false },
                columnClass = Boolean::class.java,
                tooltip = "Redeclare"
            ),
            createColumn(
                name = ATTRIBUTES_COLUMN_AUTO_CREATE,
                valueProvider = { attr -> attr.retrieveDom()?.autoCreate?.value ?: false },
                columnClass = Boolean::class.java,
                tooltip = "Autocreate"
            ),
            createColumn(
                name = ATTRIBUTES_COLUMN_GENERATE,
                valueProvider = { attr -> attr.retrieveDom()?.generate?.value ?: false },
                columnClass = Boolean::class.java,
                tooltip = "Generate"
            ),
            createColumn(
                name = ATTRIBUTES_COLUMN_QUALIFIER,
                valueProvider = { attr -> attr.retrieveDom()?.qualifier?.stringValue ?: "" }
            ),
            createColumn(
                name = ATTRIBUTES_COLUMN_TYPE,
                valueProvider = { attr -> attr.retrieveDom()?.type?.stringValue ?: "" }
            ),
            createColumn(
                name = ATTRIBUTES_COLUMN_DEFAULT_VALUE,
                valueProvider = { attr -> attr.retrieveDom()?.defaultValue?.stringValue ?: "" }
            ),
            createColumn(
                name = ATTRIBUTES_COLUMN_DESCRIPTION,
                valueProvider = { attr -> attr.retrieveDom()?.description?.xmlTag?.value?.text ?: "" }
            )
        )

        this
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

    private fun createColumn(
        name: String,
        valueProvider: (TSMetaAttribute) -> Any,
        columnClass: Class<*> = String::class.java,
        tooltip: String? = null
    ) = object : ColumnInfo<TSMetaAttribute, Any>(name) {
        override fun valueOf(item: TSMetaAttribute) = valueProvider.invoke(item)
        override fun isCellEditable(item: TSMetaAttribute) = false
        override fun getColumnClass(): Class<*> = columnClass
        override fun getTooltipText(): String? = tooltip
//            override fun setValue(item: TSMetaAttribute, value: String) = property.set(item, value)
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