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
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.welcomescreen.HybrisProjectSettingsCache
import sap.commerce.toolset.project.welcomescreen.actionSystem.RemoveSapCommerceProjectAction
import sap.commerce.toolset.project.welcomescreen.presentation.RecentSapCommerceProject
import sap.commerce.toolset.ui.addListSelectionListener
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.ListSelectionModel
import kotlin.time.Duration.Companion.milliseconds

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
 * - Collects from [HybrisProjectSettingsCache.settings] and repaints when entries arrive.
 */
@OptIn(FlowPreview::class)
internal class SapCommerceProjectList(
    parentDisposable: Disposable,
    private val model: CollectionListModel<RecentSapCommerceProject>
) : JBList<RecentSapCommerceProject>(model),
    UiDataProvider{

    var hoveredIndex: Int = -1
        set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        selectionBackground = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        border = JBUI.Borders.empty(0, 4)
        selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Tell JBList to drive AnimatedIcon repaints inside the cell renderer.
        putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)

        addListSelectionListener(parentDisposable) {
            if (selectedIndex != -1) invokeLater { clearSelection() }
        }

        val removeAction = ActionManager.getInstance().getAction(RemoveSapCommerceProjectAction.ACTION_ID)
        removeAction?.registerCustomShortcutSet(removeAction.shortcutSet, this, parentDisposable)

        // Subscribe to cache updates. Debouncing collapses bursts of cache fills
        // (typical at startup, when many projects warm up in parallel) into a
        // single repaint instead of one repaint per project.
        scope.launch {
            HybrisProjectSettingsCache.getInstance()
                .settings
                .drop(1)  // skip the initial empty-map emission
                .debounce(50.milliseconds)
                .distinctUntilChanged()
                .collect {
                    withContext(Dispatchers.EDT) { repaint() }
                }
        }

        Disposer.register(parentDisposable) { scope.cancel() }
    }

    override fun uiDataSnapshot(sink: DataSink) {
        if (hoveredIndex >= 0 && hoveredIndex < model.size) {
            sink[ProjectConstants.WelcomeScreen.DATA_KEY_SAP_COMMERCE_PROJECT] = model.getElementAt(hoveredIndex)
        }
    }

    private fun repaintRowForLocation(location: String) {
        for (i in 0 until model.size) {
            if (model.getElementAt(i).location == location) {
                getCellBounds(i, i)?.let { repaint(it) }
                return
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

    fun isOnRow(e: MouseEvent): Boolean = with(locationToIndex(e.point)) { this >= 0 && getCellBounds(this, this)?.contains(e.point) == true }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -8825947558653986606L
    }
}