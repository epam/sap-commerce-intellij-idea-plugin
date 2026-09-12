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
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * Mouse interaction for [CxPropertyList]:
 *
 * - Hovering a row updates [CxPropertyList.hoveredIndex] and switches the cursor to a hand
 *   over the action icons.
 * - Left-click on the edit hit zone fires [onEditClicked].
 * - Left-click on the delete hit zone fires [onDeleteClicked].
 * - Clicks anywhere else on the row are ignored.
 */
internal class CxPropertyMouseHandler(
    private val list: CxPropertyList,
    private val model: CollectionListModel<CxPropertyPresentation>,
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
        when {
            isOnDelete(e, bounds) -> onDeleteClicked(property)
            isOnEdit(e, bounds) -> onEditClicked(property)
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

    private fun isOnActionZone(e: MouseEvent): Boolean {
        val index = list.locationToIndex(e.point)
        if (index < 0) return false
        val bounds = list.getCellBounds(index, index) ?: return false
        return isOnEdit(e, bounds) || isOnDelete(e, bounds)
    }

    private fun isOnDelete(e: MouseEvent, cellBounds: Rectangle): Boolean {
        val zoneStart = cellBounds.x + cellBounds.width - JBUI.scale(CxPropertyRenderer.DELETE_HIT_WIDTH)
        return e.point.x >= zoneStart
    }

    private fun isOnEdit(e: MouseEvent, cellBounds: Rectangle): Boolean {
        val deleteZoneStart = cellBounds.x + cellBounds.width - JBUI.scale(CxPropertyRenderer.DELETE_HIT_WIDTH)
        val editZoneStart = deleteZoneStart - JBUI.scale(CxPropertyRenderer.EDIT_HIT_WIDTH)
        return e.point.x in editZoneStart until deleteZoneStart
    }

    companion object {
        private val HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        private val DEFAULT_CURSOR = Cursor.getDefaultCursor()
    }
}
