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

import com.intellij.codeInsight.hints.presentation.MouseButton
import com.intellij.codeInsight.hints.presentation.mouseButton
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.ui.CollectionListModel
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.welcomescreen.WelcomeScreenConstants
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
import java.awt.Cursor
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent


/**
 * Handles mouse interaction for [SapCommerceProjectList]:
 *
 * - Single left-click on a row opens the project.
 * - Cursor movement updates the list's [SapCommerceProjectList.hoveredIndex] and
 *   switches the pointer to [Cursor.HAND_CURSOR] over a row.
 * - Cursor leaving the list clears the hover and restores the default cursor.
 */
internal class SapCommerceProjectMouseHandler(
    private val list: SapCommerceProjectList,
    private val model: CollectionListModel<RecentSapCommerceProject>
) : MouseAdapter() {

    override fun mouseClicked(e: MouseEvent) {
        val index = list.locationToIndex(e.point)
        val bounds = list.getCellBounds(index, index) ?: return
        if (!bounds.contains(e.point)) return
        val project = model.getElementAt(index)

        when {
            e.mouseButton == MouseButton.Right -> showContextMenu(e, project)
            e.mouseButton == MouseButton.Left && isOnOverflow(e, bounds) -> showContextMenu(e, project)
            e.mouseButton == MouseButton.Left && e.clickCount == 1 -> openProject(project)
        }
    }

    private fun isOnOverflow(e: MouseEvent, cellBounds: Rectangle): Boolean {
        // The overflow button sits in the right ~32px of the cell.
        val overflowZoneStart = cellBounds.x + cellBounds.width - JBUI.scale(OVERFLOW_HIT_WIDTH)
        return e.point.x >= overflowZoneStart
    }

    private fun openProject(project: RecentSapCommerceProject) {
        CoroutineScope(Dispatchers.Default).launch {
            ProjectManagerEx.getInstanceEx().openProjectAsync(project.path)
        }
    }

    private fun showContextMenu(e: MouseEvent, project: RecentSapCommerceProject) {
        val group = ActionManager.getInstance().getAction("sap.commerce.toolset.welcomeTab.projectContextMenu") as? ActionGroup
            ?: return

        val dataContext = SimpleDataContext.builder()
            .add(WelcomeScreenConstants.DATA_KEY_SAP_COMMERCE_PROJECT, project)
            .add(PlatformDataKeys.CONTEXT_COMPONENT, list)
            .build()

        // Pin the hover highlight to the row the popup belongs to, so moving
        // the cursor across other rows while the menu is open doesn't shift
        // the highlight and create an action-target mismatch.
        list.hoveredIndex = list.locationToIndex(e.point)
        list.hoverFrozen = true

        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            /* title = */ null,
            /* actionGroup = */ group,
            /* dataContext = */ dataContext,
            /* selectionAidMethod = */ JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            /* showDisabledActions = */ true
        )

        popup.addListener(object : JBPopupListener {
            override fun onClosed(event: LightweightWindowEvent) {
                list.hoverFrozen = false
                // Re-sync hover to the cursor's current location. If the cursor
                // is over a different row now, the highlight follows it; if it
                // left the list entirely, clear it.
                val mousePos = list.mousePosition
                if (mousePos != null) {
                    val index = list.locationToIndex(mousePos)
                    val onRow = index >= 0 && list.getCellBounds(index, index)?.contains(mousePos) == true
                    list.hoveredIndex = if (onRow) index else -1
                    list.cursor = if (onRow) HAND_CURSOR else DEFAULT_CURSOR
                } else {
                    list.hoveredIndex = -1
                    list.cursor = DEFAULT_CURSOR
                }
            }
        })

        popup.show(RelativePoint(e))
    }

    override fun mouseMoved(e: MouseEvent) {
        val onRow = list.isOnRow(e)
        if (!list.hoverFrozen) {
            list.hoveredIndex = if (onRow) list.locationToIndex(e.point) else -1
        }
        list.cursor = if (onRow) HAND_CURSOR else DEFAULT_CURSOR
    }

    override fun mouseExited(e: MouseEvent) {
        if (!list.hoverFrozen) {
            list.hoveredIndex = -1
        }
        list.cursor = DEFAULT_CURSOR
    }

    companion object {
        private const val OVERFLOW_HIT_WIDTH = 36
        private val HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        private val DEFAULT_CURSOR = Cursor.getDefaultCursor()
    }
}