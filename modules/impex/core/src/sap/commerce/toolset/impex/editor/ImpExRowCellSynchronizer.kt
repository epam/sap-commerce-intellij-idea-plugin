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

package sap.commerce.toolset.impex.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.UndoUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.testFramework.LightVirtualFile
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.impex.psi.ImpExValueLine

/**
 * Edits a single cell of a value line in a standalone [mirror] ImpEx file, while the changes land in the [source] document.
 *
 * The mirror is [contextPrefix] (macros and the header) followed by the text of the whole value line, so the ImpEx language features
 * (completion, references, highlighting, inspections) behave as in the source file.
 * Only the cell is editable, the rest of the mirror is locked, and the changes made to the source are applied to the mirror as they are.
 */
class ImpExRowCellSynchronizer(
    private val source: Document,
    rowRange: TextRange,
    cellRange: TextRange,
    parentDisposable: Disposable,
    contextPrefix: String = "",
    valueLine: SmartPsiElementPointer<ImpExValueLine>? = null,
) {

    private var rowMarker = createRowMarker(rowRange.startOffset, rowRange.endOffset)
    private var isUpdating = false
    private var sourceChange: SourceChange? = null

    private val rowOffset = contextPrefix.length

    // the separator itself is tracked, an empty range does not follow the text inserted right at its offset
    private val groupSeparatorMarker: RangeMarker? = groupSeparatorOffset(cellRange.startOffset)
        ?.let { source.createRangeMarker(it, it + 1) }

    val virtualFile = LightVirtualFile(MIRROR_FILE_NAME, ImpExFileType, contextPrefix + rowText())
        .also { file ->
            valueLine?.let { ImpExRowContext.attach(file, it) }
            FileDocumentManager.getInstance().getFile(source)?.let { ImpExRowContext.attachSource(file, it) }
        }

    /**
     * The mirror is a view of the source: undo/redo operate on the source only,
     * otherwise refreshing the mirror during undo is rejected by the platform.
     */
    val mirror: Document = checkNotNull(FileDocumentManager.getInstance().getDocument(virtualFile)) { "Unable to create a document of the row cell" }
        .also { UndoUtil.disableUndoFor(it) }

    // typing right next to the cell is allowed, so the blocks are not greedy towards it
    private val prefixGuard: RangeMarker = mirror
        .createGuardedBlock(0, rowOffset + cellRange.startOffset - rowRange.startOffset)
        .apply {
            isGreedyToLeft = true
            isGreedyToRight = false
        }

    private val suffixGuard: RangeMarker? = (rowOffset + cellRange.endOffset - rowRange.startOffset)
        .takeIf { it < mirror.textLength }
        ?.let { mirror.createGuardedBlock(it, mirror.textLength) }
        ?.apply {
            isGreedyToLeft = false
            isGreedyToRight = true
        }

    /**
     * The offset of the value group of the cell in the source, which is the field separator right before the value.
     * The end of the value line if there is no such group.
     */
    val sourceGroupOffset: Int
        get() = groupSeparatorMarker
            // the marker collapses when the separator is removed
            ?.takeIf { it.isValid && it.endOffset - it.startOffset == 1 }
            ?.startOffset
            ?: rowMarker.takeIf { it.isValid }?.endOffset
            ?: source.textLength

    val editableRange: TextRange
        get() = TextRange(prefixGuard.endOffset, suffixGuard?.startOffset ?: mirror.textLength)

    val editableText: String
        get() = editableRange.substring(mirror.text)

    /**
     * `false` if a change of the source could not be applied to the mirror as is, so the mirror has to be created again.
     */
    val isConsistent: Boolean
        get() = rowMarker.isValid && mirror.immutableCharSequence.subSequence(rowOffset, mirror.textLength).contentEquals(rowText())

    init {
        source.addDocumentListener(object : DocumentListener {
            override fun beforeDocumentChange(event: DocumentEvent) = onBeforeSourceChange(event)
            override fun documentChanged(event: DocumentEvent) = onSourceChanged(event)
        }, parentDisposable)

        mirror.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) = onMirrorChanged(event)
        }, parentDisposable)

        Disposer.register(parentDisposable) {
            rowMarker.dispose()
            groupSeparatorMarker?.dispose()
        }
    }

    private fun onBeforeSourceChange(event: DocumentEvent) {
        sourceChange = rowMarker
            .takeIf { !isUpdating && it.isValid }
            ?.let {
                SourceChange(
                    rowStartOffset = it.startOffset,
                    isInsideRow = event.offset >= it.startOffset && event.offset + event.oldLength <= it.endOffset
                )
            }
    }

    private fun onSourceChanged(event: DocumentEvent) {
        val change = sourceChange
        sourceChange = null

        if (isUpdating || change == null || !change.isInsideRow || !rowMarker.isValid) return

        val offset = rowOffset + event.offset - change.rowStartOffset

        // the mirror is a view of the source, so its refresh must not become a separate undo step nor require a command
        updating {
            CommandProcessor.getInstance().runUndoTransparentAction {
                mirror.replaceString(offset, offset + event.oldLength, event.newFragment)
            }
        }
    }

    private fun onMirrorChanged(event: DocumentEvent) {
        if (isUpdating || !rowMarker.isValid || !ImpExRowEditability.isEditable(source) || event.offset < rowOffset) return

        val start = rowMarker.startOffset
        val offset = start + event.offset - rowOffset

        updating {
            source.replaceString(offset, offset + event.oldLength, event.newFragment)
        }

        // a replacement covering the whole row may invalidate the marker, the range is known, so it can be restored
        if (!rowMarker.isValid) {
            rowMarker.dispose()
            rowMarker = createRowMarker(start, start + mirror.textLength - rowOffset)
        }
    }

    private fun groupSeparatorOffset(cellStartOffset: Int): Int? {
        val text = source.immutableCharSequence
        var offset = cellStartOffset

        // only the whitespaces may be between the separator and the value
        while (offset > 0 && text[offset - 1].isWhitespace()) offset--

        return (offset - 1).takeIf { it >= 0 && text[it] == ';' }
    }

    private fun rowText(): CharSequence = rowMarker.takeIf { it.isValid }
        ?.let { source.immutableCharSequence.subSequence(it.startOffset, it.endOffset) }
        ?: ""

    private fun updating(action: () -> Unit) {
        isUpdating = true
        try {
            action()
        } finally {
            isUpdating = false
        }
    }

    private fun createRowMarker(startOffset: Int, endOffset: Int): RangeMarker = source
        .createRangeMarker(startOffset, endOffset)
        .apply {
            isGreedyToLeft = true
            isGreedyToRight = true
        }

    private data class SourceChange(val rowStartOffset: Int, val isInsideRow: Boolean)

    companion object {
        private const val MIRROR_FILE_NAME = "impexRowCell.impex"
    }
}
