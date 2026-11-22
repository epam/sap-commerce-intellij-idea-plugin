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

import com.intellij.ui.OnePixelSplitter
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * The splitter, which second component can be collapsed into the [CollapsedStrip] along the edge of the splitter.
 *
 * The strip is outside of the splitter, so it keeps its own thickness regardless of the proportion of the splitter.
 * The [collapsed] state is reset together with the second component.
 *
 * @param title the title of the collapsed second component, it is requested when the component is collapsed for the first time
 */
class CollapsibleSplitter(
    vertical: Boolean,
    proportionKey: String,
    private val title: () -> String,
    proportion: Float = 0.5f,
    showDividerControls: Boolean = true,
) : JPanel(BorderLayout()) {

    private val edge = if (vertical) CollapseEdge.BOTTOM else CollapseEdge.RIGHT

    private val splitter = OnePixelSplitter(vertical, proportionKey, proportion).apply {
        isShowDividerControls = showDividerControls
        setHonorComponentsMinimumSize(true)
    }

    private var strip: CollapsedStrip? = null

    var firstComponent: JComponent?
        get() = splitter.firstComponent
        set(value) {
            splitter.firstComponent = value
        }

    var secondComponent: JComponent? = null
        set(value) {
            field = value
            if (value == null) collapsed = false
            updateLayout()
        }

    var collapsed: Boolean = false
        set(value) {
            if (field == value) return

            field = value
            updateLayout()
        }

    init {
        add(splitter, BorderLayout.CENTER)
    }

    /**
     * The hidden second component is not collapsed, there is nothing to show of it.
     */
    fun showSecond(visible: Boolean) {
        secondComponent?.isVisible = visible
        updateLayout()
    }

    private fun updateLayout() {
        val second = secondComponent
        val showStrip = collapsed && second != null && second.isVisible

        splitter.secondComponent = second.takeUnless { showStrip }

        strip?.let { remove(it) }
        if (showStrip) {
            val collapsedStrip = strip ?: CollapsedStrip(title(), edge) { collapsed = false }.also { strip = it }
            add(collapsedStrip, edge.constraint)
        }

        revalidate()
        repaint()
    }
}
