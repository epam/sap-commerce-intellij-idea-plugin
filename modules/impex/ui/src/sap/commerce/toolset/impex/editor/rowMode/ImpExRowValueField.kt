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

package sap.commerce.toolset.impex.editor.rowMode

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.ReadonlyFragmentModificationHandler
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.ExpandableEditorSupport
import com.intellij.ui.EditorTextField
import sap.commerce.toolset.impex.file.ImpExFileType
import java.io.Serial

/**
 * Displays and edits only the cell of the [synchronizer], the rest of its file is hidden and locked,
 * but it still provides the context for the ImpEx language features.
 *
 * The field of a single line is expandable as the text fields of the virtual parameters, the line breaks of the value are displayed as a placeholder,
 * the expanded editor of several lines edits the same document.
 */
internal class ImpExRowValueField(
    project: Project,
    internal val synchronizer: ImpExRowCellSynchronizer,
    private val isCellEditable: Boolean,
    private val caretAtEnd: Boolean,
    disposable: Disposable?,
    private val multiline: Boolean = false,
) : EditorTextField(synchronizer.mirror, project, ImpExFileType, !isCellEditable, !multiline) {

    init {
        // the locked part is hidden, so there is nothing to explain when an attempt to modify it is made
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(synchronizer.mirror, ReadonlyFragmentModificationHandler { })
        disposable?.let { setDisposedWith(it) }

        if (!multiline) ExpandableSupport(this)
    }

    override fun createEditor(): EditorEx = super.createEditor().apply {
        settings.isAllowSingleLogicalLineFolding = true
        settings.isUseSoftWraps = multiline
        // the caret of a field is moved by the changes of the other fields, which highlights the identifier under it as a selection,
        // the platform does not do it for the one line editors of the dialogs
        isEmbeddedIntoDialogWrapper = true

        hideLockedParts(this)
        // the caret of the expanded editor is copied from the field
        if (!multiline) caretModel.moveToOffset(if (caretAtEnd) synchronizer.editableRange.endOffset else synchronizer.editableRange.startOffset)

        // the hidden parts are not reachable by the caret and the selection
        caretModel.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val range = synchronizer.editableRange
                val caret = event.caret ?: return
                val offset = caret.offset.coerceIn(range.startOffset, range.endOffset)

                if (offset != caret.offset) caret.moveToOffset(offset)
            }
        })
        selectionModel.addSelectionListener(object : SelectionListener {
            override fun selectionChanged(e: SelectionEvent) {
                val range = synchronizer.editableRange
                if (!selectionModel.hasSelection()) return
                if (selectionModel.selectionStart >= range.startOffset && selectionModel.selectionEnd <= range.endOffset) return

                selectionModel.setSelection(
                    selectionModel.selectionStart.coerceIn(range.startOffset, range.endOffset),
                    selectionModel.selectionEnd.coerceIn(range.startOffset, range.endOffset),
                )
            }
        })
    }

    /**
     * Only the cell is displayed, the rest of the file provides the context for the ImpEx language features.
     * The line breaks of the value are displayed as a placeholder by the editor of a single line.
     */
    private fun hideLockedParts(editor: EditorEx) {
        val range = synchronizer.editableRange
        val model = editor.foldingModel
        val text = editor.document.charsSequence

        model.runBatchFoldingOperation {
            model.clearFoldRegions()

            if (range.startOffset > 0) model.addFoldRegion(0, range.startOffset, "")?.isExpanded = false
            if (range.endOffset < text.length) model.addFoldRegion(range.endOffset, text.length, "")?.isExpanded = false

            if (!multiline) {
                for (offset in range.startOffset until range.endOffset) {
                    if (text[offset] == '\n') model.createFoldRegion(offset, offset + 1, LINE_BREAK_PLACEHOLDER, null, true)
                }
            }
        }
    }

    /**
     * The same as the expandable text fields of the DSL, but the folding has to keep the locked parts hidden,
     * and the expanded editor has to be the field of the several lines as well.
     */
    private class ExpandableSupport(private val valueField: ImpExRowValueField) : ExpandableEditorSupport(valueField) {

        override fun updateFieldFolding(editor: EditorEx) = valueField.hideLockedParts(editor)

        override fun createPopupEditor(field: EditorTextField, text: String): EditorTextField = ImpExRowValueField(
            project = valueField.project ?: error("The project of the field is not available"),
            synchronizer = valueField.synchronizer,
            isCellEditable = valueField.isCellEditable,
            caretAtEnd = false,
            disposable = null,
            multiline = true,
        )
    }

    companion object {
        private const val LINE_BREAK_PLACEHOLDER = " \u23ce "

        @Serial
        private const val serialVersionUID: Long = 2985102871954601724L
    }
}
