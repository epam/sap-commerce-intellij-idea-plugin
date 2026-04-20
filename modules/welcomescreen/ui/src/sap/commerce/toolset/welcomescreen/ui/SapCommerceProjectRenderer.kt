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

package sap.commerce.toolset.welcomescreen.ui

import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.welcomescreen.WelcomeScreenUiConstants
import sap.commerce.toolset.welcomescreen.presentation.HostingEnvironment
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import sap.commerce.toolset.welcomescreen.ui.tags.TagLabel
import java.awt.*
import java.io.Serial
import javax.swing.*

internal class SapCommerceProjectRenderer : JPanel(), ListCellRenderer<RecentSapCommerceProject> {

    private val iconLabel = JLabel().apply { verticalAlignment = SwingConstants.TOP }
    private val nameLabel = JLabel().apply { verticalAlignment = SwingConstants.TOP }

    /** Path label with zero preferred/minimum width so BoxLayout can shrink it freely;
     *  JLabel then clips overflowing text with a trailing "…" automatically. */
    private val pathLabel = object : JLabel() {
        override fun getMinimumSize(): Dimension = Dimension(0, super.getMinimumSize().height)
        override fun getPreferredSize(): Dimension = Dimension(0, super.getPreferredSize().height)
    }.apply { foreground = JBColor.GRAY }

    private val branchLabel = JLabel().apply {
        foreground = JBColor.GRAY
        font = JBUI.Fonts.smallFont()
        icon = HybrisIcons.WelcomeTab.VCS_BRANCH
        iconTextGap = JBUI.scale(4)
    }

    /** Version badge — colors swap between resting/hovered; cleared while loading. */
    private val versionLabel = TagLabel()

    /** CCV2 hosting badge — fixed colors, toggled visible/invisible per row. */
    private val hostingCcv2Label = TagLabel(i18n("hybris.welcometab.hosting.environment.ccv2")).apply {
        colors = WelcomeScreenUiConstants.Tags.TAG_COLORS_CCV2
        isVisible = false
    }

    /** On-Premise hosting badge — fixed colors, toggled visible/invisible per row. */
    private val hostingOnPremiseLabel = TagLabel(i18n("hybris.welcometab.hosting.environment.on.premise")).apply {
        colors = WelcomeScreenUiConstants.Tags.TAG_COLORS_ON_PREMISE
        isVisible = false
    }

    private val overflowLabel = JLabel(HybrisIcons.WelcomeTab.ACTION_MORE).apply {
        border = JBUI.Borders.empty(2)
        isOpaque = false
    }

    // Not cached — resolved at paint time so theme switches are reflected immediately.
    private val pillColor: Color
        get() = UIManager.getColor("List.selectionBackground")
            ?: JBUI.CurrentTheme.List.Hover.background(true)

    private var hovered = false

    init {
        layout = BorderLayout(JBUI.scale(ICON_TEXT_GAP), 0)
        border = JBUI.Borders.empty(VERTICAL_PADDING, HORIZONTAL_PADDING)
        isFocusable = false
        isOpaque = false

        // Icon column — top-aligned so the icon's top edge lines up with the project name.
        val iconHolder = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            alignmentY = TOP_ALIGNMENT
            iconLabel.alignmentX = LEFT_ALIGNMENT
            add(iconLabel)
        }

        // Text column — name / path / branch stacked vertically.
        val textPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            alignmentY = TOP_ALIGNMENT
            nameLabel.alignmentX = LEFT_ALIGNMENT
            pathLabel.alignmentX = LEFT_ALIGNMENT
            branchLabel.alignmentX = LEFT_ALIGNMENT
            add(nameLabel)
            add(Box.createVerticalStrut(JBUI.scale(TEXT_LINE_GAP)))
            add(pathLabel)
            add(Box.createVerticalStrut(JBUI.scale(TEXT_LINE_GAP)))
            add(branchLabel)
        }

        // Tags column — version on top, then the two hosting labels (only one visible at a time).
        val tagsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            alignmentY = CENTER_ALIGNMENT
            versionLabel.alignmentX = RIGHT_ALIGNMENT
            hostingCcv2Label.alignmentX = RIGHT_ALIGNMENT
            hostingOnPremiseLabel.alignmentX = RIGHT_ALIGNMENT
            add(versionLabel)
            add(Box.createVerticalStrut(JBUI.scale(TEXT_LINE_GAP)))
            add(hostingCcv2Label)
            add(hostingOnPremiseLabel)
        }

        val rightPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            alignmentY = CENTER_ALIGNMENT
            add(tagsPanel)
            add(Box.createHorizontalStrut(JBUI.scale(8)))
            add(overflowLabel)
        }

        add(iconHolder, BorderLayout.WEST)
        add(textPanel, BorderLayout.CENTER)
        add(rightPanel, BorderLayout.EAST)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            // Always fill the entire cell with the list background to suppress
            // any default selection painting from BasicListUI underneath.
            g2.color = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
            g2.fillRect(0, 0, width, height)

            if (hovered) {
                g2.color = pillColor
                g2.fillRoundRect(
                    JBUI.scale(PILL_HORIZONTAL_INSET), 0,
                    width - 2 * JBUI.scale(PILL_HORIZONTAL_INSET), height,
                    JBUI.scale(PILL_ARC), JBUI.scale(PILL_ARC)
                )
            }
        } finally {
            g2.dispose()
        }
        super.paintComponent(g)
    }

    override fun getListCellRendererComponent(
        list: JList<out RecentSapCommerceProject>,
        value: RecentSapCommerceProject,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val isHovered = (list as? SapCommerceProjectList)?.hoveredIndex == index

        with(value) {
            iconLabel.icon = projectIcon
            nameLabel.text = displayName
            pathLabel.text = locationRelativeToUserHome
            overflowLabel.isVisible = isHovered

            when {
                !isSettingsLoaded -> {
                    versionLabel.icon = HybrisIcons.WelcomeTab.LOADING
                    versionLabel.text = ""
                    versionLabel.colors = null
                    versionLabel.foreground = WelcomeScreenUiConstants.Tags.TAG_COLORS_VERSION.foreground
                }
                else -> {
                    versionLabel.icon = null
                    versionLabel.text = hybrisVersion ?: NOT_AVAILABLE_TEXT
                    versionLabel.colors = if (isHovered) WelcomeScreenUiConstants.Tags.TAG_COLORS_VERSION_HOVERED else WelcomeScreenUiConstants.Tags.TAG_COLORS_VERSION
                }
            }

            branchLabel.text = gitBranch ?: ""
            branchLabel.isVisible = gitBranch != null

            // Show exactly one hosting label; hide the other.
            hostingCcv2Label.isVisible = hostingEnvironment == HostingEnvironment.CCV2
            hostingOnPremiseLabel.isVisible = hostingEnvironment == HostingEnvironment.ON_PREMISE
        }

        hovered = isHovered
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
        private const val NOT_AVAILABLE_TEXT = "n/a"
    }
}

