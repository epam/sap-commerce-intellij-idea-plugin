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

package sap.commerce.toolset.welcomescreen.ui.tags

import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.io.Serial
import javax.swing.JLabel

/**
 * A [javax.swing.JLabel] that paints a filled rounded-rectangle badge behind its text.
 *
 * Set [colors] to a [TagColors] instance to enable the fill + border; set it
 * to `null` to render as plain text (used while settings are still loading).
 * The label keeps `isOpaque = false` so the parent's pill highlight shows through.
 */
class TagLabel(text: String = "") : JLabel(text) {
    var colors: TagColors? = null

    init {
        font = JBUI.Fonts.smallFont()
        border = JBUI.Borders.empty(2, 8)
        isOpaque = false
    }

    override fun paintComponent(g: Graphics) {
        val c = colors
        if (c != null) {
            val g2 = g.create() as Graphics2D
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = c.background
                g2.fillRoundRect(0, 0, width, height, JBUI.scale(TAG_ARC), JBUI.scale(TAG_ARC))
                g2.color = c.border
                g2.stroke = BasicStroke(JBUI.scale(1).toFloat())
                g2.drawRoundRect(0, 0, width - 1, height - 1, JBUI.scale(TAG_ARC), JBUI.scale(TAG_ARC))
            } finally {
                g2.dispose()
            }
            foreground = c.foreground
        }
        super.paintComponent(g)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 4372309922266020496L

        private const val TAG_ARC = 8
    }

}