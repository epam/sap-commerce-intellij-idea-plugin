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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.forms

import com.intellij.ide.ui.search.SearchUtil
import com.intellij.idea.plugin.hybris.type.system.meta.MetaType
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAttribute
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.openapi.project.Project
import com.intellij.ui.*
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.Dimension
import java.awt.FontMetrics
import javax.swing.JComboBox
import javax.swing.JTable
import javax.swing.table.TableColumn

class TSMetaItemViewDataSupplier(private val myProject: Project) {

    fun initAttributesTable(table: JBTable, meta: TSMetaItem) = with(table) {
        val search = TableSpeedSearch(table)

        setShowGrid(false)
        setDefaultRenderer(Boolean::class.java, BooleanTableCellRenderer());

        intercellSpacing = Dimension(0, 0)
        model = getListTableModel(meta)

        getColumn("Qualifier").cellRenderer = Renderer(search)
        getColumn("Description").cellRenderer = Renderer(search)

        arrayOf("Redeclare", "Autocreate", "Generate")
            .forEach { setFixedColumnWidth(getColumn(it), table, it)}

        this
    }

    fun initExtends(comboBox: JComboBox<String>, meta: TSMetaItem) = with(comboBox) {
        val dom = meta.retrieveDom()

        model = getExtends(meta)
        selectedItem = dom.extends.stringValue ?: TSMetaItem.IMPLICIT_SUPER_CLASS_NAME

        this
    }

    private fun getExtends(meta: TSMetaItem): CollectionComboBoxModel<String> = with(CollectionComboBoxModel<String>()) {
        TSMetaModelAccess.getInstance(myProject).metaModel.getMetaType<TSMetaItem>(MetaType.META_ITEM).values
            .filter { it != meta }
            .map { it.name }
            .sortedBy { it }
            .forEach { add(it) }

        this
    }

    private fun getListTableModel(meta: TSMetaItem): ListTableModel<TSMetaAttribute> = with(ListTableModel<TSMetaAttribute>()) {
        items = meta.getAttributes(true)
            .sortedBy { it.name }

        columnInfos = arrayOf(
            createColumn(
                name = "Redeclare",
                valueProvider = { attr -> attr.retrieveDom()?.redeclare?.value ?: false },
                columnClass = Boolean::class.java
            ),
            createColumn(
                name = "Autocreate",
                valueProvider = { attr -> attr.retrieveDom()?.autoCreate?.value ?: false },
                columnClass = Boolean::class.java),
            createColumn(
                name = "Generate",
                valueProvider = { attr -> attr.retrieveDom()?.generate?.value ?: false },
                columnClass = Boolean::class.java
            ),
            createColumn(
                name = "Qualifier",
                valueProvider = { attr -> attr.retrieveDom()?.qualifier?.stringValue ?: "" }
            ),
            createColumn(
                name ="Type",
                valueProvider = { attr -> attr.retrieveDom()?.type?.stringValue ?: "" }
            ),
            createColumn(
                name = "Default value",
                valueProvider = { attr -> attr.retrieveDom()?.defaultValue?.stringValue ?: "" }
            ),
            createColumn(
                name = "Description",
                valueProvider = { attr -> attr.retrieveDom()?.description?.xmlTag?.value?.text ?: "" }
            )
        )

        this
    }

    private fun setFixedColumnWidth(column: TableColumn, table : JTable, text: String) = with(column) {
        val fontMetrics: FontMetrics = table.getFontMetrics(table.font)
        val width = fontMetrics.stringWidth(" $text ") + JBUIScale.scale(4)

        preferredWidth = width
        minWidth = width
        maxWidth = width
        resizable = false

        this
    }

    private fun createColumn(
        name : String,
        valueProvider : (TSMetaAttribute) -> Any,
        columnClass : Class<*> = String::class.java
    ) = object : ColumnInfo<TSMetaAttribute, Any>(name) {
            override fun valueOf(item: TSMetaAttribute) = valueProvider.invoke(item)
            override fun isCellEditable(item: TSMetaAttribute) = true
            override fun getColumnClass(): Class<*> = columnClass
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
        fun getInstance(project: Project): TSMetaItemViewDataSupplier {
            return project.getService(TSMetaItemViewDataSupplier::class.java) as TSMetaItemViewDataSupplier
        }
    }
}
