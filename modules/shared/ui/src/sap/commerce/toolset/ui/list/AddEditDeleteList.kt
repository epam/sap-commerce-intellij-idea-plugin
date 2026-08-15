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

package sap.commerce.toolset.ui.list

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.util.whenListChanged
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AddEditDeleteListPanel
import com.intellij.ui.ListSpeedSearch
import com.intellij.util.ui.JBEmptyBorder
import sap.commerce.toolset.settings.state.MutableState
import sap.commerce.toolset.settings.state.Mutation
import sap.commerce.toolset.ui.ifOk
import java.awt.Component
import java.io.Serial
import javax.swing.*
import javax.swing.event.ListDataEvent

abstract class AddEditDeleteList<T : MutableState>(
    disposable: Disposable?,
    listener: (ListDataEvent) -> Unit = {},
    title: String? = null,
    initialList: List<T> = emptyList(),
) : AddEditDeleteListPanel<T>(title, initialList) {

    private var reset: Boolean = false
    private var myListCellRenderer: ListCellRenderer<*>? = null

    val elements
        get() = myListModel.elements().toList()
    var modified: Boolean = false
        private set

    init {
        ListSpeedSearch.installOn(myList) { getName(it) }

        myListModel.whenListChanged(disposable) {
            if (reset) return@whenListChanged

            when (it.type) {
                ListDataEvent.CONTENTS_CHANGED -> if (myListModel.get(it.index0).mutation == Mutation.SAVE) modified = true

                ListDataEvent.INTERVAL_ADDED,
                ListDataEvent.INTERVAL_REMOVED -> modified = true
            }

            listener(it)
        }
    }

    abstract fun getName(element: T): String
    abstract fun getIcon(element: T): Icon
    abstract fun newItem(element: T? = null): T
    abstract fun createDialog(item: T): DialogWrapper
    abstract fun editDialog(item: T): DialogWrapper

    override fun findItemToAdd(): T? = with(newItem()) {
        createDialog(this).ifOk { this }
    }

    override fun editSelectedItem(item: T): T? = editDialog(item).ifOk { item }

    override fun getListCellRenderer(): ListCellRenderer<*> {
        if (myListCellRenderer == null) {
            myListCellRenderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(list: JList<*>, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JComponent
                    val t = value as T

                    component.border = JBEmptyBorder(5)
                    icon = getIcon(t)
                    text = getName(t)

                    return component
                }

                @Serial
                private val serialVersionUID: Long = -7680459611226925362L
            }
        }
        return myListCellRenderer!!
    }

    fun reset(data: List<T>) {
        try {
            reset = true
            modified = false

            this.myListModel.clear()

            data.forEach { super.addElement(it) }
        } finally {
            reset = false
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 2136669228774907076L
    }
}
