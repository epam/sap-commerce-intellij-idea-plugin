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

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.project.welcomescreen.presentation.SapCommerceProject
import sap.commerce.toolset.ui.addListSelectionListener
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.ListSelectionModel

/**
 * A non-selectable, hover-only list of SAP Commerce projects.
 *
 * - Suppresses persistent selection: any selection change is immediately cleared.
 * - Blocks mouse events that fall outside row bounds so clicks on empty space don't
 *   leak into BasicListUI's selection logic.
 * - Forwards off-row motion events to registered listeners so external hover
 *   tracking can clear its state when the cursor leaves a row.
 * - Exposes [hoveredIndex] as a typed property; changing it repaints the list.
 */
internal class SapCommerceProjectList(
    parentDisposable: Disposable,
    model: CollectionListModel<SapCommerceProject>
) : JBList<SapCommerceProject>(model) {

    var hoveredIndex: Int = -1
        set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

    init {
        background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        selectionBackground = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        border = JBUI.Borders.empty(0, 4)
        selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Never allow a row to stay selected — we drive visuals purely from hover.
        addListSelectionListener(parentDisposable) {
            if (selectedIndex != -1) {
                invokeLater { clearSelection() }
            }
        }
    }

    override fun processMouseEvent(e: MouseEvent) = if (isOnRow(e)) super.processMouseEvent(e) else Unit

    override fun processMouseMotionEvent(e: MouseEvent) = if (isOnRow(e)) {
        super.processMouseMotionEvent(e)
    } else {
        // Deliver manually to registered listeners so hover tracking can
        // react to "cursor left the row area". BasicListUI's own listener
        // also runs here but is a no-op for MOUSE_MOVED without a drag.
        for (listener in mouseMotionListeners) {
            when (e.id) {
                MouseEvent.MOUSE_MOVED -> listener.mouseMoved(e)
                MouseEvent.MOUSE_DRAGGED -> listener.mouseDragged(e)
            }
        }
    }

    fun isOnRow(e: MouseEvent): Boolean = with(locationToIndex(e.point)) {
        this >= 0 && getCellBounds(this, this)?.contains(e.point) == true
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -8825947558653986606L
    }
}