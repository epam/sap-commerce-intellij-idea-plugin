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

package sap.commerce.toolset.project.welcomescreen.ui

import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.project.welcomescreen.presentation.SapCommerceProject
import java.awt.*
import java.io.Serial
import javax.swing.*

internal class SapCommerceProjectRenderer : JPanel(), ListCellRenderer<SapCommerceProject> {

    private val iconLabel = JLabel().apply { verticalAlignment = SwingConstants.TOP }
    private val nameLabel = JLabel().apply { verticalAlignment = SwingConstants.TOP }
    private val pathLabel = JLabel().apply {
        foreground = JBColor.GRAY
    }

    private val versionLabel = JLabel().apply {
        foreground = JBColor.GRAY
        font = JBUI.Fonts.smallFont()
        border = JBUI.Borders.empty(2, 8)
        isOpaque = false
    }

    private val pillColor: Color = UIManager.getColor("List.selectionBackground")
        ?: JBUI.CurrentTheme.List.Hover.background(true)
    private val tagBorderColor: Color = JBColor.border()

    private var hovered = false
    private var showVersionTagBorder = false

    init {
        layout = BorderLayout(JBUI.scale(ICON_TEXT_GAP), 0)
        border = JBUI.Borders.empty(VERTICAL_PADDING, HORIZONTAL_PADDING)
        isFocusable = false
        isOpaque = false

        // BoxLayout with matching TOP_ALIGNMENT — icon top edge lines up with name top edge.
        val textPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            alignmentY = TOP_ALIGNMENT
            nameLabel.alignmentX = LEFT_ALIGNMENT
            pathLabel.alignmentX = LEFT_ALIGNMENT
            add(nameLabel)
            add(Box.createVerticalStrut(JBUI.scale(TEXT_LINE_GAP)))
            add(pathLabel)
        }

        val iconHolder = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            alignmentY = TOP_ALIGNMENT
            iconLabel.alignmentX = LEFT_ALIGNMENT
            add(iconLabel)
        }

        val rightPanel = JPanel(GridBagLayout()).apply {
            isOpaque = false
            add(versionLabel, GridBagConstraints().apply {
                anchor = GridBagConstraints.CENTER
            })
        }

        add(iconHolder, BorderLayout.WEST)
        add(textPanel, BorderLayout.CENTER)
        add(rightPanel, BorderLayout.EAST)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            // Cover the whole cell with the panel background — hides any default
            // selection paint from BasicListUI so only our rounded pill is visible.
            g2.color = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
            g2.fillRect(0, 0, width, height)

            if (hovered) {
                g2.color = pillColor
                val arc = JBUI.scale(PILL_ARC)
                val inset = JBUI.scale(PILL_HORIZONTAL_INSET)
                g2.fillRoundRect(inset, 0, width - 2 * inset, height, arc, arc)
            }

            if (showVersionTagBorder) {
                val bounds = SwingUtilities.convertRectangle(versionLabel.parent, versionLabel.bounds, this)
                g2.color = tagBorderColor
                g2.stroke = BasicStroke(JBUI.scale(1).toFloat())
                g2.drawRoundRect(
                    bounds.x, bounds.y,
                    bounds.width - 1, bounds.height - 1,
                    JBUI.scale(TAG_ARC), JBUI.scale(TAG_ARC)
                )
            }
        } finally {
            g2.dispose()
        }
        super.paintComponent(g)
    }

    override fun getListCellRendererComponent(
        list: JList<out SapCommerceProject>,
        value: SapCommerceProject,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        with(value) {
            iconLabel.icon = projectIcon
            nameLabel.text = displayName
            pathLabel.text = locationRelativeToUserHome

            when {
                !isSettingsLoaded -> {
                    // Still loading — show spinner, no border.
                    versionLabel.icon = AnimatedIcon.Default.INSTANCE
                    versionLabel.text = ""
                    showVersionTagBorder = false
                }
                hybrisVersion != null -> {
                    // Loaded with value — show version, with border.
                    versionLabel.icon = null
                    versionLabel.text = hybrisVersion
                    showVersionTagBorder = true
                }
                else -> {
                    // Loaded but no value — show "n/a", with border.
                    versionLabel.icon = null
                    versionLabel.text = NOT_AVAILABLE_TEXT
                    showVersionTagBorder = true
                }
            }
        }

        hovered = (list as? SapCommerceProjectList)?.hoveredIndex == index
        return this
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 8104563402887526145L
        private const val ICON_TEXT_GAP = 12
        private const val VERTICAL_PADDING = 10
        private const val HORIZONTAL_PADDING = 20
        private const val TEXT_LINE_GAP = 4
        private const val PILL_ARC = 12
        private const val PILL_HORIZONTAL_INSET = 8
        private const val TAG_ARC = 8
        private const val NOT_AVAILABLE_TEXT = "n/a"

    }
}