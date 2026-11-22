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
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.impex.psi.ImpExValueLine

/**
 * Edits a single cell of a value line in a standalone [mirror] ImpEx file, while the changes land in the [source] document.
 *
 * The mirror is [contextPrefix] (macros and the header) followed by the text of the whole value line and a line break, so the ImpEx language features
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
    missingSeparators: Int = 0,
) {

    private var rowMarker = createRowMarker(rowRange.startOffset, rowRange.endOffset)
    private var isUpdating = false
    private var sourceChange: SourceChange? = null

    // a change of the source replacing the text on both sides of the border of the cell (e.g. a rollback of the line) breaks the locked parts of the mirror
    private var isStructureBroken = false

    // the separators of a missing group are only displayed, they are added to the source along with the first modification of the cell
    private var pendingSeparators = missingSeparators

    private val rowOffset = contextPrefix.length

    // the separator itself is tracked, an empty range does not follow the text inserted right at its offset
    private val groupSeparatorMarker: RangeMarker? = groupSeparatorOffset(cellRange.startOffset)
        ?.takeIf { missingSeparators == 0 }
        ?.let { source.createRangeMarker(it, it + 1) }

    val virtualFile = LightVirtualFile(MIRROR_FILE_NAME, ImpExFileType, contextPrefix + rowText() + SEPARATOR.repeat(missingSeparators) + MIRROR_END)
        .also { file ->
            valueLine?.let { ImpExRowService.getInstance(it.project).attachContext(file, it) }
            file.putUserData(HybrisConstants.Keys.ORIGINAL_FILE, FileDocumentManager.getInstance().getFile(source))
        }

    /**
     * The mirror is a view of the source: undo/redo operate on the source only,
     * otherwise refreshing the mirror during undo is rejected by the platform.
     */
    val mirror: Document = checkNotNull(FileDocumentManager.getInstance().getDocument(virtualFile)) { "Unable to create a document of the row cell" }
        .also { UndoUtil.disableUndoFor(it) }

    // typing right next to the cell is allowed, so the blocks are not greedy towards it
    private val prefixGuard: RangeMarker = mirror
        .createGuardedBlock(0, rowOffset + cellRange.startOffset - rowRange.startOffset + missingSeparators)
        .apply {
            isGreedyToLeft = true
            isGreedyToRight = false
        }

    private val suffixGuard: RangeMarker? = (rowOffset + cellRange.endOffset - rowRange.startOffset + missingSeparators)
        .takeIf { it < mirror.textLength }
        ?.let { mirror.createGuardedBlock(it, mirror.textLength) }
        ?.apply {
            isGreedyToLeft = false
            isGreedyToRight = true
        }

    /**
     * The beginning of the value group of the cell in the source, which is the offset right after the field separator.
     * The caret in front of the separator is on the previous group, as it is right at the end of its text.
     * The end of the value line if there is no such group.
     */
    val sourceGroupOffset: Int
        get() = groupSeparatorMarker
            // the marker collapses when the separator is removed
            ?.takeIf { it.isValid && it.endOffset - it.startOffset == 1 }
            ?.endOffset
            ?: rowMarker.takeIf { it.isValid }?.endOffset
            ?: source.textLength

    val editableRange: TextRange
        get() = TextRange(prefixGuard.endOffset, suffixGuard?.startOffset ?: mirror.textLength)

    val editableText: String
        get() = editableRange.substring(mirror.text)

    /**
     * `false` if a change of the source could not be applied to the mirror as is, so the mirror has to be created again.
     * The mirror may contain the same text as the source, but the cell may not be separated from the rest of the row properly.
     */
    val isConsistent: Boolean
        get() = rowMarker.isValid && !isStructureBroken && mirror.immutableCharSequence.subSequence(rowOffset, mirror.textLength - MIRROR_END.length - pendingSeparators).contentEquals(rowText())

    /**
     * `false` if the value of the cell [cellRange] resolved from the source reaches outside of the editable part of the mirror,
     * e.g. the text has been added after the whitespaces following the value, so the value is longer than it was when the mirror was created.
     * The editable part may be larger than the value, e.g. it also includes the whitespaces typed around the value.
     */
    fun isCovering(cellRange: TextRange): Boolean {
        if (!rowMarker.isValid) return false

        val editable = editableRange
        // the separators of a missing group are only displayed, they are not in the source yet
        val start = rowMarker.startOffset + editable.startOffset - rowOffset - pendingSeparators
        val end = rowMarker.startOffset + editable.endOffset - rowOffset - pendingSeparators

        return cellRange.startOffset >= start && cellRange.endOffset <= end
    }

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
                val editable = editableRange

                SourceChange(
                    rowStartOffset = it.startOffset,
                    isInsideRow = event.offset >= it.startOffset && event.offset + event.oldLength <= it.endOffset,
                    editableStartOffset = editable.startOffset,
                    editableEndOffset = editable.endOffset,
                )
            }
    }

    private fun onSourceChanged(event: DocumentEvent) {
        val change = sourceChange
        sourceChange = null

        if (isUpdating || change == null || !change.isInsideRow || !rowMarker.isValid) return

        val offset = rowOffset + event.offset - change.rowStartOffset
        val end = offset + event.oldLength

        if (event.oldLength > 0 && change.isCrossingBorderOfCell(offset, end)) isStructureBroken = true

        // the mirror is a view of the source, so its refresh must not become a separate undo step nor require a command
        updating {
            CommandProcessor.getInstance().runUndoTransparentAction {
                mirror.replaceString(offset, offset + event.oldLength, event.newFragment)
            }
        }
    }

    private fun onMirrorChanged(event: DocumentEvent) {
        if (isUpdating || !rowMarker.isValid || !source.isWritable || event.offset < rowOffset) return

        if (pendingSeparators > 0) {
            updating { source.insertString(rowMarker.endOffset, SEPARATOR.repeat(pendingSeparators)) }
            pendingSeparators = 0
        }

        val start = rowMarker.startOffset
        val offset = start + event.offset - rowOffset

        updating {
            source.replaceString(offset, offset + event.oldLength, event.newFragment)
        }

        // a replacement covering the whole row may invalidate the marker, the range is known, so it can be restored
        if (!rowMarker.isValid) {
            rowMarker.dispose()
            rowMarker = createRowMarker(start, start + mirror.textLength - MIRROR_END.length - pendingSeparators - rowOffset)
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

    private data class SourceChange(
        val rowStartOffset: Int,
        val isInsideRow: Boolean,
        val editableStartOffset: Int,
        val editableEndOffset: Int,
    ) {
        fun isCrossingBorderOfCell(startOffset: Int, endOffset: Int) = (editableStartOffset in (startOffset + 1)..<endOffset)
            || (editableEndOffset in (startOffset + 1)..<endOffset)
    }

    companion object {
        private const val MIRROR_FILE_NAME = "impexRowCell.impex"
        private const val SEPARATOR = ";"

        // the locked end of the mirror, the text added to the end of the row is placed in front of it, so it stays within the hidden part of the other cells
        private const val MIRROR_END = "\n"
    }
}
