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

package com.intellij.idea.plugin.hybris.tools.ccv2.ui.tree

import com.intellij.ui.render.RenderingUtil
import com.intellij.util.asSafely
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.ThreeStateCheckBox
import java.awt.Component
import java.io.Serial
import java.util.*
import javax.swing.JCheckBox
import javax.swing.JTable
import javax.swing.event.CellEditorListener
import javax.swing.event.ChangeEvent
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

class CCv2ThreeStateCheckBoxRenderer : ThreeStateCheckBox(), TableCellRenderer, TableCellEditor {

    private val myListeners: MutableList<CellEditorListener> = ContainerUtil.createLockFreeCopyOnWriteList()

    init {
        isThirdStateEnabled = false
        horizontalAlignment = CENTER
        verticalAlignment = CENTER

        addItemListener { stopCellEditing() }
    }

    override fun getTableCellRendererComponent(
        table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
    ): Component? = tune(value, isSelected, table).apply { isOpaque = true }

    override fun getTableCellEditorComponent(
        table: JTable, value: Any?, isSelected: Boolean, row: Int, column: Int
    ): Component? = tune(value, isSelected, table)

    override fun getCellEditorValue(): Any? = if (state != State.DONT_CARE) isSelected
    else null

    override fun isCellEditable(anEvent: EventObject?) = true
    override fun shouldSelectCell(anEvent: EventObject?) = true

    override fun stopCellEditing(): Boolean {
        ChangeEvent(this).apply {
            myListeners.forEach { it.editingStopped(this) }
        }
        return true
    }

    override fun cancelCellEditing() {
        ChangeEvent(this).apply {
            myListeners.forEach { it.editingCanceled(this) }
        }
    }

    override fun addCellEditorListener(l: CellEditorListener) {
        myListeners.add(l)
    }

    override fun removeCellEditorListener(l: CellEditorListener) {
        myListeners.remove(l)
    }

    private fun tune(value: Any?, isSelected: Boolean, table: JTable): JCheckBox {
        setForeground(RenderingUtil.getForeground(table, isSelected))
        setBackground(RenderingUtil.getBackground(table, isSelected))

        value.asSafely<State>()?.let { setState(it) }

        return this
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6532311366248760768L
    }
}