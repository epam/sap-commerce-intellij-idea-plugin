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
import com.intellij.ui.EditorTextField
import sap.commerce.toolset.impex.file.ImpExFileType
import java.io.Serial

/**
 * Displays and edits only the cell of the [synchronizer], the rest of its file is hidden and locked,
 * but it still provides the context for the ImpEx language features.
 */
internal class ImpExRowValueField(
    project: Project,
    private val synchronizer: ImpExRowCellSynchronizer,
    editable: Boolean,
    private val caretAtEnd: Boolean,
    disposable: Disposable,
) : EditorTextField(synchronizer.mirror, project, ImpExFileType, !editable, true) {

    init {
        // the locked part is hidden, so there is nothing to explain when an attempt to modify it is made
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(synchronizer.mirror, ReadonlyFragmentModificationHandler { })
        setDisposedWith(disposable)
    }

    override fun createEditor(): EditorEx = super.createEditor().apply {
        settings.isAllowSingleLogicalLineFolding = true
        // the caret of a field is moved by the changes of the other fields, which highlights the identifier under it as a selection,
        // the platform does not do it for the one line editors of the dialogs
        isEmbeddedIntoDialogWrapper = true

        val editable = synchronizer.editableRange
        foldingModel.runBatchFoldingOperation {
            if (editable.startOffset > 0) foldingModel.addFoldRegion(0, editable.startOffset, "")?.isExpanded = false
            if (editable.endOffset < document.textLength) foldingModel.addFoldRegion(editable.endOffset, document.textLength, "")?.isExpanded = false
        }
        caretModel.moveToOffset(if (caretAtEnd) editable.endOffset else editable.startOffset)

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

    companion object {
        @Serial
        private const val serialVersionUID: Long = 2985102871954601724L
    }
}
