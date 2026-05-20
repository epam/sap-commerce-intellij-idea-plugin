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
import com.intellij.openapi.application.invokeLater
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import sap.commerce.toolset.ui.addListSelectionListener
import sap.commerce.toolset.ui.addMouseListener
import sap.commerce.toolset.ui.addMouseMotionListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent
import java.io.Serial
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
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
 */
internal class CxPropertyList(
    parentDisposable: Disposable,
    private val model: CollectionListModel<CxPropertyPresentation>,
    onEditClicked: (CxPropertyPresentation) -> Unit,
    onDeleteClicked: (CxPropertyPresentation) -> Unit,
) : JBList<CxPropertyPresentation>(model) {

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

    private var editorOverlay: InlinePropertyEditor? = null

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

        val mouseHandler = CxPropertyMouseHandler(this, model, onEditClicked, onDeleteClicked)
        this.addMouseListener(parentDisposable, mouseHandler)
        this.addMouseMotionListener(parentDisposable, mouseHandler)
        cellRenderer = CxPropertyRenderer()
    }

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

    @Suppress("unused")
    fun overlayComponent(): JComponent? = editorOverlay

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6217493082143759241L
    }
}
