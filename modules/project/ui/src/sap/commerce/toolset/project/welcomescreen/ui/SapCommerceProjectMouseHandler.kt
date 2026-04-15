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

import com.intellij.codeInsight.hints.presentation.MouseButton
import com.intellij.codeInsight.hints.presentation.mouseButton
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.CollectionListModel
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.welcomescreen.presentation.SapCommerceProject
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent


/**
 * Handles mouse interaction for [SapCommerceProjectList]:
 *
 * - Single left-click on a row opens the project.
 * - Cursor movement updates the list's [SapCommerceProjectList.hoveredIndex].
 * - Cursor leaving the list clears the hover.
 */
internal class SapCommerceProjectMouseHandler(
    private val list: SapCommerceProjectList,
    private val model: CollectionListModel<SapCommerceProject>
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

    private fun openProject(project: SapCommerceProject) {
        CoroutineScope(Dispatchers.Default).launch {
            ProjectManagerEx.getInstanceEx().openProjectAsync(project.path)
        }
    }

    private fun showContextMenu(e: MouseEvent, project: SapCommerceProject) {
        val actionManager = ActionManager.getInstance()
        val group = actionManager.getAction("SapCommerce.WelcomeTab.ProjectContextMenu") as? ActionGroup
            ?: return

        val dataContext = SimpleDataContext.builder()
            .add(ProjectConstants.WelcomeScreen.SAP_COMMERCE_PROJECT_KEY, project)
            .add(PlatformDataKeys.CONTEXT_COMPONENT, list)
            .build()

        JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,                       // title
                group,
                dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true                        // showDisabledActions
            )
            .show(RelativePoint(e))
    }

    override fun mouseMoved(e: MouseEvent) {
        list.hoveredIndex = if (list.isOnRow(e)) list.locationToIndex(e.point) else -1
    }

    override fun mouseExited(e: MouseEvent) {
        list.hoveredIndex = -1
    }

    companion object {
        private const val OVERFLOW_HIT_WIDTH = 36  // overflow icon + padding
    }
}