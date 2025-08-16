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

import com.intellij.ui.components.JBLabel
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

class GroupedComboBoxRenderer : ListCellRenderer<GroupedComboBoxItem?> {
    private val label = JBLabel()

    override fun getListCellRendererComponent(
        list: JList<out GroupedComboBoxItem>,
        value: GroupedComboBoxItem?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        label.isOpaque = true
        when (value) {
            is GroupedComboBoxItem.Group -> {
                label.text = "-- ${value.label} --"
                label.isEnabled = false
                label.background = list.background
                label.foreground = list.foreground
            }

            is GroupedComboBoxItem.Option -> {
                label.text = value.value
                label.isEnabled = true
                label.background = if (isSelected) list.selectionBackground else list.background
                label.foreground = if (isSelected) list.selectionForeground else list.foreground
            }

            else -> {
                label.text = ""
            }
        }
        return label
    }
}