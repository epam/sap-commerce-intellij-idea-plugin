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

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ListenerUtil.addMouseListener
import com.intellij.ui.ListenerUtil.addMouseMotionListener
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ui.addListSelectionListener
import sap.commerce.toolset.welcomescreen.WelcomeScreenConstants
import sap.commerce.toolset.welcomescreen.actionSystem.RemoveSapCommerceProjectAction
import sap.commerce.toolset.welcomescreen.presentation.RecentSapCommerceProject
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
 * - Drives `AnimatedIcon` repaints in renderers via `ANIMATION_IN_RENDERER_ALLOWED`.
 *
 * Repaints driven by async data arrival (hybris version, hosting environment, git
 * branch) are orchestrated by [SapCommerceWelcomeTab], which subscribes to each
 * project's [com.intellij.openapi.observable.properties.ObservableProperty]s and
 * fires a debounced `repaint()` when any of them change. This list class no longer
 * owns any background work of its own.
 */
internal class SapCommerceProjectList(
    parentDisposable: Disposable,
    private val model: CollectionListModel<RecentSapCommerceProject>
) : JBList<RecentSapCommerceProject>(model),
    UiDataProvider {

    var hoveredIndex: Int = -1
        set(value) {
            if (hoverFrozen) return
            if (field != value) {
                field = value
                repaint()
            }
        }

    /**
     * While `true`, writes to [hoveredIndex] are ignored. Used by
     * [SapCommerceProjectMouseHandler] to pin the hover highlight to the row
     * that owns an open context-menu popup, so moving the cursor over other
     * rows doesn't shift the highlight and make the action target ambiguous.
     */
    var hoverFrozen: Boolean = false

    init {
        background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        selectionBackground = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        border = JBUI.Borders.empty(0, 4)
        selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Start in the "loading" state; SapCommerceWelcomeTab flips this to the
        // empty-state text once the first load completes.
        showLoading()

        // Tell JBList to drive AnimatedIcon repaints inside the cell renderer.
        putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)

        addListSelectionListener(parentDisposable) {
            if (selectedIndex != -1) invokeLater { clearSelection() }
        }

        ActionManager.getInstance().getAction(RemoveSapCommerceProjectAction.ACTION_ID)
            ?.also { it.registerCustomShortcutSet(it.shortcutSet, this, parentDisposable) }

        val mouseHandler = SapCommerceProjectMouseHandler(this, model)
        addMouseListener(this, mouseHandler)
        addMouseMotionListener(this, mouseHandler)
        cellRenderer = SapCommerceProjectRenderer()
    }

    /**
     * Switches the empty-state message to "loading". Shown while the background
     * coroutine in [SapCommerceWelcomeTab] is filtering recent paths and building
     * [RecentSapCommerceProject] instances. Has no visual effect once the model
     * is non-empty — [com.intellij.util.ui.StatusText] only paints when the list
     * has no rows.
     */
    fun showLoading() {
        emptyText.text = i18n("hybris.welcometab.list.loading")
    }

    /**
     * Switches the empty-state message to "no recent projects". Called once
     * the first load completes; if projects are present, the text is hidden
     * automatically by [com.intellij.util.ui.StatusText].
     */
    fun showLoaded() {
        emptyText.text = i18n("hybris.welcometab.list.empty")
    }

    override fun uiDataSnapshot(sink: DataSink) {
        if (hoveredIndex >= 0 && hoveredIndex < model.size) {
            sink[WelcomeScreenConstants.DATA_KEY_SAP_COMMERCE_PROJECT] = model.getElementAt(hoveredIndex)
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

    fun isOnRow(e: MouseEvent): Boolean = with(locationToIndex(e.point)) { this >= 0 && getCellBounds(this, this)?.contains(e.point) == true }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -8825947558653986606L
    }
}