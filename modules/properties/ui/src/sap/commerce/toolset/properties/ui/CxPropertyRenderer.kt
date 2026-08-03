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

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import java.awt.*
import java.io.Serial
import javax.swing.*

/**
 * Cell renderer for the [CxPropertyList] infinite-scroll table.
 *
 * Layout matches the legacy paginated UI: a 50/50 key/value split with edit
 * and delete action icons pinned to the right edge. Long text is clipped with
 * "…" — JLabel does this automatically once the assigned width is below its
 * preferred width, which GridBagLayout produces by giving the action icons
 * fixed widths and letting the key/value columns share the remainder via
 * equal `weightx`.
 *
 * Hovering a row paints a translucent "pill" highlight (delegated to the
 * cell background) so the user can see which row's actions belong to the
 * cursor position. The action icons are always visible; the hover only
 * affects the row background, similar to standard table tools.
 */
internal class CxPropertyRenderer : JPanel(), ListCellRenderer<CxPropertyPresentation> {

    /** Key cell — zero preferred/minimum width so GridBagLayout's equal weights split fairly. */
    private val keyLabel = object : JBLabel() {
        override fun getMinimumSize(): Dimension = Dimension(0, super.getMinimumSize().height)
        override fun getPreferredSize(): Dimension = Dimension(0, super.getPreferredSize().height)
    }

    /** Value cell — zero preferred/minimum width for the same reason as [keyLabel]. */
    private val valueLabel = object : JBLabel() {
        override fun getMinimumSize(): Dimension = Dimension(0, super.getMinimumSize().height)
        override fun getPreferredSize(): Dimension = Dimension(0, super.getPreferredSize().height)
    }.apply {
        foreground = JBColor.GRAY
    }

    private val editLabel = JBLabel(HybrisIcons.Connection.EDIT).apply {
        toolTipText = "Edit property"
        border = JBUI.Borders.empty(2)
    }

    private val deleteLabel = JBLabel(HybrisIcons.Log.Action.DELETE).apply {
        toolTipText = "Delete property"
        border = JBUI.Borders.empty(2)
    }

    // Resolved at paint time so theme switches apply immediately.
    private val pillColor: Color
        get() = UIManager.getColor("List.hoverBackground")
            ?: UIManager.getColor("List.selectionBackground")
            ?: JBUI.CurrentTheme.List.Hover.background(true)

    // Background color the renderer fills behind each cell. Resolved at paint time so it
    // tracks the JList's current background — which the view sets to match the surrounding
    // DialogPanel so the data area doesn't look like a separate gray pane.
    private var rowBackground: Color = UIUtil.getPanelBackground()

    private var hovered = false

    init {
        layout = GridBagLayout()
        border = JBUI.Borders.empty(VERTICAL_PADDING, HORIZONTAL_PADDING)
        isFocusable = false
        isOpaque = false

        val gap = JBUI.scale(COLUMN_GAP)

        add(keyLabel, GridBagConstraints().apply {
            gridx = 0; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.BOTH
            insets = JBUI.insets(0, 0, 0, gap / 2)
        })
        add(valueLabel, GridBagConstraints().apply {
            gridx = 1; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.BOTH
            insets = JBUI.insets(0, gap / 2, 0, JBUI.scale(ACTION_LEFT_INSET))
        })
        add(editLabel, GridBagConstraints().apply {
            gridx = 2; gridy = 0
            weightx = 0.0; weighty = 1.0
            fill = GridBagConstraints.VERTICAL
            insets = JBUI.insets(0, JBUI.scale(ACTION_GAP), 0, 0)
        })
        add(deleteLabel, GridBagConstraints().apply {
            gridx = 3; gridy = 0
            weightx = 0.0; weighty = 1.0
            fill = GridBagConstraints.VERTICAL
            insets = JBUI.insets(0, JBUI.scale(ACTION_GAP), 0, 0)
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            // Repaint the cell bg explicitly so the JBList's default selection paint can't bleed through.
            g2.color = rowBackground
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
        list: JList<out CxPropertyPresentation>,
        property: CxPropertyPresentation,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        val cxList = list.asSafely<CxPropertyList>()
        rowBackground = list.background

        val editing = cxList?.editingKey == property.key
        // When this row is being edited, the inline editor overlay is laid on top, so the
        // cell behind it should be blank — otherwise the underlying text / action icons would
        // bleed through any transparent gap in the overlay.
        hovered = !editing && cxList?.hoveredIndex == index

        keyLabel.text = if (editing) "" else property.key
        keyLabel.toolTipText = if (editing) null else property.key
        valueLabel.text = if (editing) "" else property.value
        valueLabel.toolTipText = if (editing) null else property.value.takeIf { it.isNotBlank() }
        editLabel.isVisible = !editing
        deleteLabel.isVisible = !editing

        return this
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 7124083902112374581L

        private const val VERTICAL_PADDING = 6
        private const val HORIZONTAL_PADDING = 12
        private const val COLUMN_GAP = 8
        private const val ACTION_GAP = 6
        private const val ACTION_LEFT_INSET = 12
        private const val PILL_ARC = 8
        private const val PILL_HORIZONTAL_INSET = 4

        /** Width of the click hit zone for the delete icon, measured from the cell's right edge. */
        const val DELETE_HIT_WIDTH = 28

        /** Width of the click hit zone for the edit icon, measured leftward from [DELETE_HIT_WIDTH]. */
        const val EDIT_HIT_WIDTH = 28
    }
}
