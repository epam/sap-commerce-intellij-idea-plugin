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
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange

/**
 * Keeps a sub-range of the [source] document and a standalone [mirror] document in sync in both directions,
 * so the mirror can be edited by a dedicated editor while the changes land in the original file.
 */
class ImpExValueRangeSynchronizer(
    private val source: Document,
    range: TextRange,
    parentDisposable: Disposable,
) {

    private var marker = createMarker(range.startOffset, range.endOffset)
    private var isUpdating = false

    // the mirror is a view of the source: undo/redo operate on the source only, otherwise refreshing the mirror during undo is rejected by the platform
    val mirror: Document = EditorFactory.getInstance().createDocument(currentText())
        .also { UndoUtil.disableUndoFor(it) }

    init {
        source.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) = onSourceChanged()
        }, parentDisposable)

        mirror.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) = onMirrorChanged(event)
        }, parentDisposable)

        Disposer.register(parentDisposable) { marker.dispose() }
    }

    private fun onSourceChanged() {
        if (isUpdating || !marker.isValid) return

        val text = currentText()
        if (mirror.immutableCharSequence.contentEquals(text)) return

        // the mirror is a view of the source, so its refresh must not become a separate undo step nor require a command
        updating {
            CommandProcessor.getInstance().runUndoTransparentAction {
                mirror.replaceString(0, mirror.textLength, text)
            }
        }
    }

    private fun onMirrorChanged(event: DocumentEvent) {
        if (isUpdating || !marker.isValid || !source.isWritable) return

        val start = marker.startOffset
        val offset = start + event.offset

        updating {
            source.replaceString(offset, offset + event.oldLength, event.newFragment)
        }

        // a replacement covering the whole range may invalidate the marker, the range is known, so it can be restored
        if (!marker.isValid) {
            marker.dispose()
            marker = createMarker(start, start + mirror.textLength)
        }
    }

    private fun currentText(): CharSequence = marker.takeIf { it.isValid }
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

    private fun createMarker(startOffset: Int, endOffset: Int): RangeMarker = source
        .createRangeMarker(startOffset, endOffset)
        .apply {
            isGreedyToLeft = true
            isGreedyToRight = true
        }
}
