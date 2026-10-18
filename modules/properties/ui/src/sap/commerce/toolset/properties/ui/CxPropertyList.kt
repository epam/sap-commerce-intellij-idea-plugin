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

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.CollectionListModel
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBList
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import sap.commerce.toolset.properties.meta.CxResolvedProperty
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.addListSelectionListener
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addMouseMotionListener
import java.awt.Component
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.ToolTipManager
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

/**
 * Hover-only JBList of properties with support for an in-place row editor.
 *
 * Mirrors the welcome-screen `SapCommerceProjectList` pattern: persistent selection is
 * suppressed (any selection is immediately cleared), hover is tracked via [hoveredIndex],
 * and mouse events that fall outside row bounds are blocked so empty-space clicks don't
 * leak into BasicListUI's selection logic.
 *
 * **Inline editing.** [beginEdit] installs a real Swing component (`editorOverlay`) over the
 * target row using the JList's null-layout. The cell renderer detects [editingKey] and
 * leaves the row blank so the overlay shows through cleanly. The overlay's position is kept
 * in sync with the underlying row through:
 *  - a [ListDataListener] on the model (reposition or cancel when items shift/disappear), and
 *  - a [ComponentAdapter] on the list itself (re-fit the overlay width on resize).
 *
 * Clicks on the edit / delete hit zones invoke [onEditClicked] / [onDeleteClicked]; clicks
 * elsewhere on a row do nothing.
 *
 * **Tooltips.** [getToolTipText] is resolved per row here rather than on the renderer: renderer
 * components are stamped through a `CellRendererPane` instead of joining the component hierarchy,
 * so a tooltip assigned to one of their labels never reaches the `ToolTipManager`.
 *
 * **Context menu.** Right-clicking a row opens [CONTEXT_MENU_GROUP]. Persistent selection is
 * suppressed here, so the row is taken from the click position and handed to the actions through
 * [CxPropertyPresentation.DATA_KEY] by [uiDataSnapshot].
 */
internal class CxPropertyList(
    parentDisposable: Disposable,
    private val model: CollectionListModel<CxPropertyPresentation>,
    onReportClicked: (CxPropertyPresentation) -> Unit,
    onEditClicked: (CxPropertyPresentation) -> Unit,
    onDeleteClicked: (CxPropertyPresentation) -> Unit,
) : JBList<CxPropertyPresentation>(model), UiDataProvider {

    var hoveredIndex: Int = -1
        set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

    /** Key of the property currently being edited inline, or `null` if no edit is active. */
    var editingKey: String? = null
        private set

    /**
     * Properties as the project's own files declare them, keyed by property key. Rows whose remote value disagrees
     * with the one declared here are highlighted by [CxPropertyRenderer] and explained by [getToolTipText]; keys
     * which are absent are left alone, as a property existing only on the remote instance is not a disagreement.
     */
    var localProperties: Map<String, CxResolvedProperty> = emptyMap()
        set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

    private var editorOverlay: InlinePropertyEditor? = null

    /**
     * Row the currently open context menu was invoked on. The list keeps no selection, so without remembering it the
     * row would lose its highlight the moment the cursor moved onto the menu, leaving nothing to say what the menu
     * is about to act on.
     */
    var contextMenuProperty: CxPropertyPresentation? = null
        private set(value) {
            if (field != value) {
                field = value
                repaint()
            }
        }

    private lateinit var mouseHandler: CxPropertyMouseHandler

    init {
        // Match the surrounding DialogPanel background so the data area doesn't look like a
        // separate gray pane. The renderer reads this value at paint time.
        val matchingBg = UIUtil.getPanelBackground()
        background = matchingBg
        selectionBackground = matchingBg
        isOpaque = true
        border = JBUI.Borders.empty(0, 4)
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        emptyText.text = "No properties loaded"
        // Rows are measured up front rather than from the renderer, so a row always has room
        // for the inline editor's text field instead of collapsing to the label height.
        fixedCellHeight = maxOf(CxPropertyRenderer.rowHeight(), InlinePropertyEditor.rowHeight())

        addListSelectionListener(parentDisposable) {
            if (selectedIndex != -1) invokeLater { clearSelection() }
        }

        // Keep the editor lined up with its row when the model mutates (page appended,
        // mutation refresh, filter reset). If the edited key disappears, drop the editor.
        model.addListDataListener(object : ListDataListener {
            override fun intervalAdded(e: ListDataEvent) = repositionEditor()
            override fun intervalRemoved(e: ListDataEvent) = repositionEditor()
            override fun contentsChanged(e: ListDataEvent) = repositionEditor()
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = repositionEditor()
        })

        val mouseHandler = CxPropertyMouseHandler(this, model, onReportClicked, onEditClicked, onDeleteClicked)
        this.mouseHandler = mouseHandler
        this.addMouseListener(parentDisposable, mouseHandler)
        this.addMouseMotionListener(parentDisposable, mouseHandler)
        cellRenderer = CxPropertyRenderer()

        // The tooltip depends on the counterpart rather than on the renderer's labels, so it is composed
        // per row in `getToolTipText` below, and the manager has to be told about the list itself.
        ToolTipManager.sharedInstance().registerComponent(this)

        this.addMouseListener(parentDisposable, object : PopupHandler() {
            override fun invokePopup(comp: Component, x: Int, y: Int) = showContextMenu(comp, x, y)
        })
    }

    override fun uiDataSnapshot(sink: DataSink) {
        sink[CxPropertyPresentation.DATA_KEY] = contextMenuProperty
    }

    private fun showContextMenu(comp: Component, x: Int, y: Int) {
        val point = Point(x, y)
        val index = locationToIndex(point)
            .takeIf { it >= 0 && getCellBounds(it, it)?.contains(point) == true }
            ?: return
        val property = model.items.getOrNull(index) ?: return
        if (property.key == editingKey) return

        val group = ActionManager.getInstance()
            .getAction(CONTEXT_MENU_GROUP)
            .asSafely<ActionGroup>()
            ?: return

        contextMenuProperty = property

        ActionManager.getInstance().createActionPopupMenu(CONTEXT_MENU_PLACE, group)
            .also { it.setTargetComponent(this) }
            .component
            .apply { addPopupMenuListener(ContextMenuHighlightListener()) }
            .show(comp, x, y)
    }

    /**
     * Drops the highlight once the menu is gone. The clearing is deferred, since Swing hides a menu before running
     * the action that was chosen from it.
     */
    private inner class ContextMenuHighlightListener : PopupMenuListener {

        override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) = Unit

        override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent) = clearLater()

        override fun popupMenuCanceled(e: PopupMenuEvent) = clearLater()

        private fun clearLater() = invokeLater { contextMenuProperty = null }
    }

    /**
     * Names the action icon under the cursor, or - anywhere else on the row - explains a highlighted row by spelling
     * out what the project's own property files declare and where. Rows which agree with the remote instance, rows the
     * project does not declare, and the row being edited have nothing to explain.
     */
    override fun getToolTipText(event: MouseEvent): String? {
        if (!isOnRow(event)) return null

        val property = model.items.getOrNull(locationToIndex(event.point)) ?: return null
        if (property.key == editingKey) return null

        mouseHandler.actionAt(event)
            ?.let { return it.tooltip }

        val local = localProperties[property.key]
            ?.takeIf { it.value != property.value }
            ?: return null

        return buildString {
            append("<html><body>")
            append("<p><b>Remote:</b> ").append(escape(property.value)).append("</p>")
            append("<p><b>Project:</b> ").append(escape(local.value)).append("</p>")
            // A raw value only differs once it carries placeholders - showing it then explains the expansion.
            if (local.rawValue != local.value) {
                append("<p><b>Declared as:</b> ").append(escape(local.rawValue)).append("</p>")
            }
            append("<p><b>Declared in:</b> ").append(escape(local.source.presentableName)).append("</p>")
            local.source.path
                ?.let { append("<p><small>").append(escape(it)).append("</small></p>") }
            append("</body></html>")
        }
    }

    private fun escape(value: String) = StringUtil.escapeXmlEntities(value)

    fun beginEdit(
        property: CxPropertyPresentation,
        onApply: (String) -> Unit,
    ) {
        cancelEdit()
        val index = indexOfKey(property.key)
        if (index < 0) return

        val editor = InlinePropertyEditor(
            property = property,
            onApply = { newValue ->
                onApply(newValue)
                // Always close after Apply — if the upsert succeeds and the property is still
                // present, the user can hit pencil again; if it fails, the edit is dropped.
                cancelEdit()
            },
            onCancel = { cancelEdit() },
        )
        editor.background = background

        editingKey = property.key
        editorOverlay = editor
        add(editor)
        repositionEditor()
        revalidate()
        repaint()

        SwingUtilities.invokeLater { editor.focusValueField() }
    }

    fun cancelEdit() {
        val editor = editorOverlay ?: return
        remove(editor)
        editorOverlay = null
        editingKey = null
        revalidate()
        repaint()
    }

    private fun repositionEditor() {
        val key = editingKey ?: return
        val editor = editorOverlay ?: return
        val index = indexOfKey(key)
        if (index < 0) {
            cancelEdit()
            return
        }
        val bounds = getCellBounds(index, index) ?: return
        editor.setBounds(bounds.x, bounds.y, bounds.width, bounds.height)
    }

    private fun indexOfKey(key: String): Int = model.items.indexOfFirst { it.key == key }

    override fun processMouseEvent(e: MouseEvent) = if (isOnRow(e)) super.processMouseEvent(e) else Unit

    override fun processMouseMotionEvent(e: MouseEvent) = if (isOnRow(e)) {
        super.processMouseMotionEvent(e)
    } else {
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
        private const val serialVersionUID: Long = 6217493082143759241L

        private const val CONTEXT_MENU_GROUP = "sap.cx.properties.remote.item.menu"
        private const val CONTEXT_MENU_PLACE = "Sap.Cx.PropertiesList"
    }
}
