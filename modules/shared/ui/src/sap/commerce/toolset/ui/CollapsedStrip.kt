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

package sap.commerce.toolset.ui

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
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
 * The collapsed panel: the expand button and the [title], which is written vertically for the [CollapseEdge.RIGHT].
 * The whole strip expands the panel.
 */
class CollapsedStrip(title: String, private val edge: CollapseEdge, onExpand: () -> Unit) : JPanel(BorderLayout()) {

    init {
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        val expandAction = object : DumbAwareAction(
            i18n("hybris.editor.expand"),
            i18n("hybris.editor.expand.description", title),
            edge.expandIcon
        ) {
            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun actionPerformed(e: AnActionEvent) = onExpand()
        }
        val expandButton = ActionButton(expandAction, expandAction.templatePresentation.clone(), "CollapsedStrip", ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)

        when (edge) {
            CollapseEdge.RIGHT -> {
                border = JBUI.Borders.customLine(JBColor.border(), 0, 1, 0, 0)
                add(expandButton, BorderLayout.NORTH)
                add(VerticalText(title), BorderLayout.CENTER)
            }

            CollapseEdge.BOTTOM -> {
                border = JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
                add(expandButton, BorderLayout.WEST)
                add(JBLabel(title).apply {
                    font = JBFont.label().asBold()
                    border = JBUI.Borders.emptyLeft(4)
                }, BorderLayout.CENTER)
            }
        }

        // the labels have no listeners, so the clicks on them reach the strip
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e)) onExpand()
            }
        })
    }

    override fun getPreferredSize(): Dimension = super.getPreferredSize().apply {
        when (edge) {
            CollapseEdge.RIGHT -> width = JBUI.scale(THICKNESS)
            CollapseEdge.BOTTOM -> height = JBUI.scale(THICKNESS)
        }
    }

    /**
     * The text is rotated clockwise, so it reads from the top to the bottom as the titles of the tool windows on the right side of the IDE.
     */
    private class VerticalText(private val text: String) : JComponent() {

        init {
            font = JBFont.label().asBold()
            foreground = UIUtil.getLabelForeground()
            isOpaque = false
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

    companion object {
        private const val THICKNESS = 28
    }
}
