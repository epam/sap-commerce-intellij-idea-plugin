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

import com.intellij.codeInsight.hints.presentation.MouseButton
import com.intellij.codeInsight.hints.presentation.mouseButton
import com.intellij.ui.CollectionListModel
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import java.awt.Cursor
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * Mouse interaction for [CxPropertyList]:
 *
 * - Hovering a row updates [CxPropertyList.hoveredIndex] and switches the cursor to a hand
 *   over the action icons.
 * - Left-click on the report hit zone fires [onReportClicked].
 * - Left-click on the edit hit zone fires [onEditClicked].
 * - Left-click on the delete hit zone fires [onDeleteClicked].
 * - Clicks anywhere else on the row are ignored.
 */
internal class CxPropertyMouseHandler(
    private val list: CxPropertyList,
    private val model: CollectionListModel<CxPropertyPresentation>,
    private val onReportClicked: (CxPropertyPresentation) -> Unit,
    private val onEditClicked: (CxPropertyPresentation) -> Unit,
    private val onDeleteClicked: (CxPropertyPresentation) -> Unit,
) : MouseAdapter() {

    override fun mouseClicked(e: MouseEvent) {
        if (e.mouseButton != MouseButton.Left || e.clickCount != 1) return

        val index = list.locationToIndex(e.point)
        if (index < 0 || index >= model.size) return
        val bounds = list.getCellBounds(index, index) ?: return
        if (!bounds.contains(e.point)) return

        val property = model.getElementAt(index)
        when (actionAt(e.point, bounds)) {
            CxPropertyRowAction.DELETE -> onDeleteClicked(property)
            CxPropertyRowAction.EDIT -> onEditClicked(property)
            CxPropertyRowAction.REPORT -> onReportClicked(property)
            null -> Unit
        }
    }

    override fun mouseMoved(e: MouseEvent) {
        val onRow = list.isOnRow(e)
        list.hoveredIndex = if (onRow) list.locationToIndex(e.point) else -1

        list.cursor = if (onRow && isOnActionZone(e)) HAND_CURSOR else DEFAULT_CURSOR
    }

    override fun mouseExited(e: MouseEvent) {
        list.hoveredIndex = -1
        list.cursor = DEFAULT_CURSOR
    }

    private fun isOnActionZone(e: MouseEvent) = actionAt(e) != null

    /** Action icon under [e], or `null` when the cursor is not over one. */
    fun actionAt(e: MouseEvent): CxPropertyRowAction? {
        val index = list.locationToIndex(e.point).takeIf { it >= 0 } ?: return null
        val bounds = list.getCellBounds(index, index) ?: return null

        return actionAt(e.point, bounds)
    }

    /**
     * Walks the icons the row actually shows, outermost first. A hidden icon collapses in the renderer's layout, so
     * the ones behind it move outwards and their zones have to move with them - measuring against the full set would
     * leave the report icon of a non-editable row sitting in what used to be the delete zone.
     */
    private fun actionAt(point: Point, cellBounds: Rectangle): CxPropertyRowAction? {
        val fromRightEdge = cellBounds.x + cellBounds.width - point.x
        if (fromRightEdge < 0) return null

        var zoneEnd = 0
        return CxPropertyRowAction.entries
            .filter { list.editable || !it.remote }
            .find {
                zoneEnd += it.hitWidth
                fromRightEdge <= JBUI.scale(zoneEnd)
            }
    }

    companion object {
        private val HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        private val DEFAULT_CURSOR = Cursor.getDefaultCursor()
    }
}
