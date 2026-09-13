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

import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * The `Key | Value` header a virtualized property list gets instead of a table header.
 *
 * @param reservedWidth room kept clear on the right for the row actions the list draws there, so the header lines up
 * with the rows underneath it rather than with the edge of the viewport.
 */
internal fun propertyColumnHeader(background: Color, reservedWidth: Int): JComponent {
    val gap = JBUI.scale(COLUMN_GAP)
    val reserved = JBUI.scale(reservedWidth)
    val header = JPanel(GridBagLayout()).apply {
        isOpaque = true
        this.background = background
        border = JBUI.Borders.empty(HEADER_VERTICAL_PADDING, HEADER_HORIZONTAL_PADDING)
    }

    header.add(JLabel("Key").apply { font = font.deriveFont(Font.BOLD) }, GridBagConstraints().apply {
        gridx = 0; gridy = 0
        weightx = 0.5; weighty = 1.0
        fill = GridBagConstraints.HORIZONTAL
        anchor = GridBagConstraints.WEST
        insets = JBUI.insets(0, 0, 0, gap / 2)
    })
    header.add(JLabel("Value").apply { font = font.deriveFont(Font.BOLD) }, GridBagConstraints().apply {
        gridx = 1; gridy = 0
        weightx = 0.5; weighty = 1.0
        fill = GridBagConstraints.HORIZONTAL
        anchor = GridBagConstraints.WEST
        insets = JBUI.insets(0, gap / 2, 0, reserved)
    })
    header.add(Box.createHorizontalStrut(reserved), GridBagConstraints().apply {
        gridx = 2; gridy = 0
        weightx = 0.0
        fill = GridBagConstraints.NONE
    })

    return header
}

private const val COLUMN_GAP = 8
private const val HEADER_VERTICAL_PADDING = 6
private const val HEADER_HORIZONTAL_PADDING = 12
