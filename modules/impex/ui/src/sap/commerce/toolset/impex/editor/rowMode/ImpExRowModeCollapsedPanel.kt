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

package sap.commerce.toolset.impex.editor.rowMode

import com.intellij.icons.AllIcons
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import sap.commerce.toolset.i18n
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * The collapsed [ImpExRowModePanel]: the expand button and the title written vertically, the whole strip expands the panel.
 */
class ImpExRowModeCollapsedPanel(onExpand: () -> Unit) : JPanel(BorderLayout()) {

    init {
        border = JBUI.Borders.customLine(JBColor.border(), 0, 1, 0, 0)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        val expandAction = object : DumbAwareAction(
            i18n("hybris.impex.editor.row_mode.expand"),
            i18n("hybris.impex.editor.row_mode.expand.description"),
            AllIcons.General.ChevronLeft
        ) {
            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun actionPerformed(e: AnActionEvent) = onExpand()
        }

        add(
            ActionButton(expandAction, expandAction.templatePresentation.clone(), "ImpExRowModeCollapsed", ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE),
            BorderLayout.NORTH
        )
        add(VerticalText(i18n("hybris.impex.editor.row_mode.title")), BorderLayout.CENTER)

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e)) onExpand()
            }
        })
    }

    override fun getPreferredSize(): Dimension = super.getPreferredSize().apply { width = JBUI.scale(28) }

    /**
     * The text is rotated clockwise, so it reads from the top to the bottom as the titles of the tool windows on the right side of the IDE.
     */
    private class VerticalText(private val text: String) : JComponent() {

        init {
            font = JBFont.label().asBold()
            foreground = UIUtil.getLabelForeground()
            isOpaque = false
            // the clicks have to reach the strip
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) = parent.dispatchEvent(SwingUtilities.convertMouseEvent(this@VerticalText, e, parent))
            })
        }

        override fun getPreferredSize(): Dimension {
            val metrics = getFontMetrics(font)

            return Dimension(metrics.height, metrics.stringWidth(text) + JBUI.scale(TOP_GAP))
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g.create() as Graphics2D

            try {
                UISettings.setupAntialiasing(g2)
                g2.font = font
                g2.color = foreground

                val metrics = g2.fontMetrics
                // the glyphs are drawn to the right of the baseline after the rotation
                g2.translate((width - metrics.height) / 2 + metrics.descent, JBUI.scale(TOP_GAP))
                g2.rotate(Math.PI / 2)
                g2.drawString(text, 0, 0)
            } finally {
                g2.dispose()
            }
        }

        companion object {
            private const val TOP_GAP = 8
        }
    }
}
