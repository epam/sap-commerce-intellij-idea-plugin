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

package sap.commerce.toolset.exec.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.util.whenListChanged
import com.intellij.openapi.project.Project
import com.intellij.ui.AddEditDeleteListPanel
import com.intellij.ui.ListSpeedSearch
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import sap.commerce.toolset.exec.settings.state.ExecConnectionSettingsState
import sap.commerce.toolset.exec.settings.state.presentationName
import java.awt.Component
import java.io.Serial
import javax.swing.*
import javax.swing.event.ListDataEvent

abstract class ConnectionsListPanel<T: ExecConnectionSettingsState.Mutable>(
    protected val project: Project,
    disposable: Disposable?,
    listener: (ListDataEvent) -> Unit
) : AddEditDeleteListPanel<T>(null, emptyList()) {

    private var myListCellRenderer: ListCellRenderer<*>? = null

    init {
        ListSpeedSearch.installOn(myList) { it.name }

        myListModel.whenListChanged(disposable) {
            listener(it)
        }
    }

    abstract fun getIcon(item: T): Icon
    abstract fun newMutable(): T
    abstract fun createDialog(mutable: T): ConnectionSettingsDialog<T>
    abstract fun editDialog(mutable: T): ConnectionSettingsDialog<T>

    override fun findItemToAdd(): T? {
        val mutable = newMutable()
        return if (createDialog(mutable).showAndGet()) mutable
        else null
    }

    override fun editSelectedItem(item: T): T? {
        return if (editDialog(item).showAndGet()) item
        else null
    }

    override fun getListCellRenderer(): ListCellRenderer<*> {
        if (myListCellRenderer == null) {
            myListCellRenderer = object : DefaultListCellRenderer() {

                override fun getListCellRendererComponent(list: JList<*>, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val name = value.asSafely<ExecConnectionSettingsState>()
                        ?.presentationName
                        ?: value.toString()
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    (comp as JComponent).border = JBEmptyBorder(5)
                    icon = getIcon(value as T)
                    text = name

                    return comp
                }

                @Serial
                private val serialVersionUID: Long = -7680459611226925362L
            }
        }
        return myListCellRenderer!!
    }

    var data: List<T>
        get() = myListModel.elements().toList()
        set(itemList) {
            myListModel.clear()
            for (itemToAdd in itemList) {
                super.addElement(itemToAdd)
            }
        }

    companion object {
        @Serial
        private val serialVersionUID: Long = 3757468168722276336L
    }

}