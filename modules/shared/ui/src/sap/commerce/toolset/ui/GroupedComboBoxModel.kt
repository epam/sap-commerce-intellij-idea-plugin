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

import java.io.Serial
import javax.swing.AbstractListModel
import javax.swing.ComboBoxModel

class GroupedComboBoxModel(private val items: List<GroupedComboBoxItem>) : AbstractListModel<GroupedComboBoxItem>(), ComboBoxModel<GroupedComboBoxItem> {
    private var selectedItem: GroupedComboBoxItem? = items.find { it is GroupedComboBoxItem.Option }

    override fun getSize() = items.size
    override fun getElementAt(index: Int): GroupedComboBoxItem = items[index]
    override fun getSelectedItem(): Any? = selectedItem
    override fun setSelectedItem(anItem: Any?) {
        if (anItem is GroupedComboBoxItem.Option) {
            selectedItem = anItem
            fireContentsChanged(this, -1, -1)
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 2077395530355235512L
    }
}